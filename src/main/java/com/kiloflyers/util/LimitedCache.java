package com.kiloflyers.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class LimitedCache<K, V> extends LinkedHashMap<K, V> {
    private final int maxEntries;

    public LimitedCache(int maxEntries) {
        super(maxEntries, 0.75f, true); // true for access-order
        this.maxEntries = maxEntries;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxEntries;
    }

    // Optionally, you can add synchronized methods if concurrent access is required
    public synchronized V putInCache(K key, V value) {
        return super.put(key, value);
    }

    public synchronized V getFromCache(K key) {
        return super.get(key);
    }
}

