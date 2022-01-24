package com.origamienergy.forgettingmap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ForgettingMap<K, V>{

    private final Map<K, V> entries;
    private final ConcurrentHashMap<K, Integer> accessCounts;

    private final int MAX_ENTRIES;

    public ForgettingMap(int maxEntries) {
        this.MAX_ENTRIES = maxEntries;
        entries = Collections.synchronizedMap(new LinkedHashMap<>(maxEntries, .75f, true));
        accessCounts = new ConcurrentHashMap<>(MAX_ENTRIES);
    }

    public boolean add(K key, V content) {
        synchronized (entries) {
            entries.put(key, content);
            while (this.entries.size() > MAX_ENTRIES)
                removeTopmostUnused();

            if (!accessCounts.containsKey(key))
                accessCounts.put(key, 0);
        }
        return true;
    }

    private void removeTopmostUnused() {
        Set<K> leastUsedKeys = getLeastUsedKeys();
        for (K key: leastUsedKeys){
            this.entries.remove(key);
            this.accessCounts.remove(key);
        }
    }


    private Set<K> getLeastUsedKeys() {
        int minAccessValue = accessCounts.values().stream().min(Integer::compareTo).get();
        Set<K> minValuesSet = accessCounts.entrySet()
                .stream()
                .filter((Map.Entry<K, Integer> entry) -> entry.getValue() == minAccessValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        for (K key : entries.keySet()) {
            if (minValuesSet.contains(key)) {
                return Set.of(key);
            }
        }
        return minValuesSet;
    }

    public V find(K key) {
        int oldValue = 0;
        if (accessCounts.containsKey(key))
            oldValue = accessCounts.get(key);

        accessCounts.put(key, ++oldValue);
        return entries.get(key);
    }

    public int size(){
        return entries.size();
    }

    public Set<K> keySet() {
        return entries.keySet();
    }
}
