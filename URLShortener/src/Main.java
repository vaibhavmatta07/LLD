import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/* =======================
   ENTITY
======================= */
class UrlMapping {
    final String shortCode;
    final String longUrl;
    final long createdAt;
    final long expiryAt;

    UrlMapping(String shortCode, String longUrl, long expiryAt) {
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.createdAt = System.currentTimeMillis();
        this.expiryAt = expiryAt;
    }

    boolean isExpired() {
        return expiryAt > 0 && System.currentTimeMillis() > expiryAt;
    }
}

/* =======================
   CODE GENERATOR
======================= */
interface CodeGenerator {
    String generate();
}

class Base62Generator implements CodeGenerator {
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final AtomicLong counter;

    public Base62Generator(long start) {
        this.counter = new AtomicLong(start);
    }

    public Base62Generator() {
        this(100000);
    }

    @Override
    public String generate() {
        long id = counter.incrementAndGet();
        return encode(id);
    }

    private String encode(long num) {
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            sb.append(CHARS.charAt((int) (num % 62)));
            num /= 62;
        }
        return sb.reverse().toString();
    }
}

/* =======================
   STORAGE LAYER
======================= */
interface URLStore {
    void save(UrlMapping mapping);
    UrlMapping find(String shortCode);
    String findByLongUrl(String longUrl);
}

class InMemoryStore implements URLStore {
    private final ConcurrentHashMap<String, UrlMapping> shortToLong = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> longToShort = new ConcurrentHashMap<>();

    @Override
    public void save(UrlMapping mapping) {
        shortToLong.put(mapping.shortCode, mapping);
        longToShort.put(mapping.longUrl, mapping.shortCode);
    }

    @Override
    public UrlMapping find(String shortCode) {
        return shortToLong.get(shortCode);
    }

    @Override
    public String findByLongUrl(String longUrl) {
        return longToShort.get(longUrl);
    }
}

/* =======================
   CACHE LAYER
======================= */
interface Cache {
    void put(String key, UrlMapping value);
    UrlMapping get(String key);
}

class InMemoryCache implements Cache {
    private final ConcurrentHashMap<String, UrlMapping> cache = new ConcurrentHashMap<>();

    @Override
    public void put(String key, UrlMapping value) {
        cache.put(key, value);
    }

    @Override
    public UrlMapping get(String key) {
        return cache.get(key);
    }
}

/* =======================
   ANALYTICS
======================= */
class ClickStats {
    final String shortCode;
    final long createdAt;
    final AtomicLong clicks = new AtomicLong(0);

    ClickStats(String shortCode) {
        this.shortCode = shortCode;
        this.createdAt = System.currentTimeMillis();
    }

    void recordClick() {
        clicks.incrementAndGet();
    }

    long getClicks() {
        return clicks.get();
    }
}

class AnalyticsService {
    private final ConcurrentHashMap<String, ClickStats> stats = new ConcurrentHashMap<>();

    void init(String shortCode) {
        stats.put(shortCode, new ClickStats(shortCode));
    }

    void record(String shortCode) {
        ClickStats cs = stats.get(shortCode);
        if (cs != null) cs.recordClick();
    }

    ClickStats getStats(String shortCode) {
        return stats.get(shortCode);
    }
}

/* =======================
   URL VALIDATOR
======================= */
class UrlValidator {
    public static void validate(String url) {
        try {
            new URL(url); // throws if invalid
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }
    }
}

/* =======================
   MAIN SERVICE
======================= */
class URLShortenerService {

    private final String baseUrl;
    private final CodeGenerator generator;
    private final URLStore store;
    private final Cache cache;
    private final AnalyticsService analytics;

    public URLShortenerService(String baseUrl,
                               CodeGenerator generator,
                               URLStore store,
                               Cache cache) {
        this.baseUrl = baseUrl;
        this.generator = generator;
        this.store = store;
        this.cache = cache;
        this.analytics = new AnalyticsService();
    }

    public URLShortenerService() {
        this("https://short.ly/",
                new Base62Generator(),
                new InMemoryStore(),
                new InMemoryCache());
    }

    /* =======================
       SHORTEN
    ======================= */
    public String shorten(String longUrl, long ttlMillis) {
        UrlValidator.validate(longUrl);

        // Check existing
        String existing = store.findByLongUrl(longUrl);
        if (existing != null) {
            return baseUrl + existing;
        }

        String code;
        UrlMapping mapping;

        // Collision-safe generation
        do {
            code = generator.generate();
            long expiry = ttlMillis > 0 ? System.currentTimeMillis() + ttlMillis : 0;
            mapping = new UrlMapping(code, longUrl, expiry);
        } while (store.find(code) != null);

        store.save(mapping);
        cache.put(code, mapping);
        analytics.init(code);

        return baseUrl + code;
    }

    /* =======================
       RESOLVE (HOT PATH)
    ======================= */
    public String resolve(String shortCode) {

        // 1. Cache
        UrlMapping mapping = cache.get(shortCode);

        // 2. DB fallback
        if (mapping == null) {
            mapping = store.find(shortCode);
            if (mapping != null) {
                cache.put(shortCode, mapping);
            }
        }

        if (mapping == null || mapping.isExpired()) {
            throw new RuntimeException("URL not found or expired");
        }

        analytics.record(shortCode);
        return mapping.longUrl;
    }

    public ClickStats getStats(String shortCode) {
        return analytics.getStats(shortCode);
    }
}

/* =======================
   DEMO
======================= */
public class Main {
    public static void main(String[] args) {

        URLShortenerService service = new URLShortenerService();

        String shortUrl1 = service.shorten("https://example.com/very/long/url", 0);
        String shortUrl2 = service.shorten("https://google.com/search?q=java", 0);

        System.out.println("Short1: " + shortUrl1);
        System.out.println("Short2: " + shortUrl2);

        String code = shortUrl1.replace("https://short.ly/", "");

        System.out.println("Resolved: " + service.resolve(code));
        service.resolve(code);
        service.resolve(code);

        ClickStats stats = service.getStats(code);
        System.out.println("Clicks: " + stats.getClicks());

        // Expiry test
        String expiring = service.shorten("https://expire.com", 1000);
        String expCode = expiring.replace("https://short.ly/", "");

        try {
            Thread.sleep(1500);
            service.resolve(expCode);
        } catch (Exception e) {
            System.out.println("Expired as expected");
        }
    }
}
