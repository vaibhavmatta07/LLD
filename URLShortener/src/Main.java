import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

interface CodeGenerator {
    String generate(String url);
}

class Base62Generator implements CodeGenerator {
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final AtomicLong counter;

    Base62Generator(long start) {
        this.counter = new AtomicLong(start);
    }

    Base62Generator() {
        this(100000L);
    }

    @Override
    public String generate(String url) {
        long num = counter.incrementAndGet();
        return encode(num);
    }

    private String encode(long num) {
        if(num == 0) return String.valueOf(CHARS.charAt(0));
        StringBuilder sb = new StringBuilder();
        while(num > 0) {
            sb.append(CHARS.charAt((int) (num % 62)));
            num /= 62;
        }
        return sb.reverse().toString();
    }
}

class ClickStats {
    final String shortCode;
    int totalClicks;
    final long createdAt;

    ClickStats(String shortCode) {
        this.shortCode = shortCode;
        this.totalClicks = 0;
        this.createdAt = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "ClickStats(" + shortCode + ", clicks = " + totalClicks + ")";
    }
}

class Analytics {
    private final Map<String, ClickStats> data = new HashMap<>();

    void initTracking(String shortCode) {
        data.put(shortCode, new ClickStats(shortCode));
    }

    void recordClick(String shortCode) {
        ClickStats stats = data.get(shortCode);
        if(stats != null) {
            stats.totalClicks++;
        }
    }

    ClickStats getStats(String shortCode) {
        return data.get(shortCode);
    }
}

interface URLStore {
    void save(String shortCode, String longUrl);
    String find(String shortCode);
    String existsByUrl(String longUrl);
}

class InMemoryStore implements URLStore {
    private final Map<String, String> shortToLong = new HashMap<>();
    private final Map<String, String> longToShort = new HashMap<>();

    @Override
    public void save(String shortCode, String longUrl) {
        shortToLong.put(shortCode, longUrl);
        longToShort.put(longUrl, shortCode);
    }

    @Override
    public String find(String shortCode) {
        return shortToLong.get(shortCode);
    }

    @Override
    public String existsByUrl(String longUrl) {
        return longToShort.get(longUrl);
    }
}

class URLShortener {
    private final String baseUrl;
    private final CodeGenerator generator;
    private final URLStore store;
    private final Analytics analytics;

    public URLShortener(String baseUrl, CodeGenerator generator, URLStore store) {
        this.baseUrl = baseUrl;
        this.generator = generator;
        this.store = store;
        this.analytics = new Analytics();
    }

    public URLShortener() {
        this("https://short.ly/", new Base62Generator(), new InMemoryStore());
    }

    public String shorten(String longUrl) {
        if(longUrl == null || (!longUrl.startsWith("https://") && !longUrl.startsWith("http://"))) {
            throw new IllegalArgumentException("Invalid URL");
        }

        String existingUrl = store.existsByUrl(longUrl);
        if(existingUrl != null) {
            return baseUrl + existingUrl;
        }

        String code = generator.generate(longUrl);
        analytics.initTracking(code);
        store.save(code, longUrl);
        return baseUrl + code;
    }

    public String resolve(String shortCode) {
        String longUrl = store.find(shortCode);
        if(longUrl != null) {
            analytics.recordClick(shortCode);
        }
        return longUrl;
    }

    public ClickStats getClickStats(String shortCode) {
        return analytics.getStats(shortCode);
    }
}

public class Main {
    public static void main(String[] args) {
        URLShortener urlShortener = new URLShortener();

        String s1 = urlShortener.shorten("https://example.com/very/long/path/to/resource");
        String s2 = urlShortener.shorten("https://another.com/example/page/long");

        System.out.println("Shortened URL 1: " + s1);
        System.out.println("Shortened URL 2: " + s2);

        String s3 = urlShortener.shorten("https://example.com/very/long/path/to/resource");
        System.out.println("URL same as URL 1: " + s3);

        String code1 = s1.replace("https://short.ly/", "");
        String resolved = urlShortener.resolve(code1);
        System.out.println("Resolved: " + resolved);

        urlShortener.resolve(code1);
        urlShortener.resolve(code1);
        ClickStats stats = urlShortener.getClickStats(code1);
        System.out.println("Stats: " + stats);

        try {
            urlShortener.shorten("not-a-url");
        }
        catch (IllegalArgumentException e) {
            System.out.println("Caught unexpected error: " + e.getMessage());
        }

        System.out.println("All checks passed");
    }
}





















