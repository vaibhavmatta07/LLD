
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// =======================
// Cache Entry
// =======================

class CacheEntry<K, V> {
    K key;
    V value;
    long expiryTime;

    CacheEntry(K key, V value, long ttlMillis) {
        this.key = key;
        this.value = value;
        this.expiryTime = ttlMillis > 0
                ? System.currentTimeMillis() + ttlMillis
                : Long.MAX_VALUE;
    }

    boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}

// =======================
// Doubly Linked List Node
// =======================

class Node<K, V> {
    CacheEntry<K, V> entry;
    Node<K, V> next, prev;

    Node(CacheEntry<K, V> entry) {
        this.entry = entry;
    }
}

// ========================
// Doubly Linked List (LRU)
// ========================

class DoublyLinkedList<K, V> {
    private final Node<K, V> head;
    private final Node<K, V> tail;

    DoublyLinkedList() {
        head = new Node<>(null);
        tail = new Node<>(null);
        head.next = tail;
        tail.prev = head;
    }

    void addFirst(Node<K, V> node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    void remove(Node<K, V> node) {
        if(node == null || node.prev == null || node.next == null) {
            return;
        }
        node.prev.next = node.next;
        node.next.prev = node.prev;
        node.prev = null;
        node.next = null;
    }

    Node<K, V> removeLast() {
        if(tail.prev == head) {
            throw new IllegalStateException("List is Emoty!");
        }

        Node<K, V> node = tail.prev;
        remove(node);
        return node;
    }

    void moveToFront(Node<K, V> node) {
        remove(node);
        addFirst(node);
    }
}

// =======================
// Cache Interface
// =======================

interface Cache<K, V> {
    V get(K key);
    void put(K key, V value);
    void put(K key, V value, long ttlMillis);
}

// =======================
// LRU Cache Implementation
// =======================

class LRUCache<K, V> implements Cache<K, V> {

    private final int capacity;
    private final Map<K, Node<K, V>> map;
    private final DoublyLinkedList<K, V> list;
    private final ReentrantReadWriteLock lock;

    private long hits = 0;
    private long misses = 0;
    private long evictions = 0;

    public LRUCache(int capacity) {
        if(capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be >= 0");
        }
        this.capacity = capacity;
        this.map = new HashMap<>();
        this.list = new DoublyLinkedList<>();
        this.lock = new ReentrantReadWriteLock();
    }

    // =======================
    // GET
    // =======================

    @Override
    public V get(K key) {
        lock.writeLock().lock();
        try {
            Node<K, V> node = map.get(key);
            if(node == null) {
                misses++;
                return null;
            }

            if(node.entry.isExpired()) {
                removeInternal(node);
                misses++;
                return null;
            }

            list.moveToFront(node);
            hits++;
            return node.entry.value;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    // =======================
    // PUT (no TTL)
    // =======================

    @Override
    public void put(K key, V value) {
        put(key, value, -1);
    }

    // =======================
    // PUT (with TTL)
    // =======================

    @Override
    public void put(K key, V value, long ttlMillis) {
        lock.writeLock().lock();
        try {
            if(map.containsKey(key)) {
                Node<K, V> node = map.get(key);
                node.entry.value = value;
                node.entry.expiryTime = ttlMillis > 0
                        ? System.currentTimeMillis() + ttlMillis
                        :Long.MAX_VALUE;
                list.moveToFront(node);
                return;
            }

            if(map.size() >= capacity) {
                Node<K, V> node = list.removeLast();
                if(node != null) {
                    map.remove(node.entry.key);
                    evictions++;
                }
            }

            CacheEntry<K, V> entry = new CacheEntry<>(key, value, ttlMillis);
            Node<K, V> node = new Node<>(entry);

            list.addFirst(node);
            map.put(key, node);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    // =======================
    // INTERNAL REMOVE
    // =======================

    private void removeInternal(Node<K, V> node) {
        list.remove(node);
        map.remove(node.entry.key);
    }

    // =======================
    // METRICS (Nice-to-have)
    // =======================

    public long getHits() { return hits; }
    public long getMisses() { return misses; }
    public long getEvictions() { return evictions; }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {

        LRUCache<String, Integer> cache = new LRUCache<>(3);

        System.out.println("===== Basic Put/Get =====");
        cache.put("a", 1);
        cache.put("b", 2);
        cache.put("c", 3);

        System.out.println("Get(a): " + cache.get("a")); // 1 (moves 'a' to front)

        System.out.println("\n===== LRU Eviction =====");
        cache.put("d", 4); // should evict 'b'

        System.out.println("Get(b): " + cache.get("b")); // null (evicted)
        System.out.println("Get(c): " + cache.get("c")); // 3
        System.out.println("Get(d): " + cache.get("d")); // 4

        System.out.println("\n===== TTL Test =====");
        cache.put("x", 100, 2000); // expires in 2 sec

        System.out.println("Get(x) immediately: " + cache.get("x")); // 100

        Thread.sleep(2500); // wait for expiry

        System.out.println("Get(x) after 2.5 sec: " + cache.get("x")); // null (expired)

        System.out.println("\n===== Update Existing Key =====");
        cache.put("c", 30);
        System.out.println("Get(c): " + cache.get("c")); // 30

        System.out.println("\n===== Metrics =====");
        System.out.println("Hits: " + cache.getHits());
        System.out.println("Misses: " + cache.getMisses());
        System.out.println("Evictions: " + cache.getEvictions());
    }
}

















