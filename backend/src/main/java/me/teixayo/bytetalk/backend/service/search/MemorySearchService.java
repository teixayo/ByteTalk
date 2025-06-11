package me.teixayo.bytetalk.backend.service.search;

import java.util.*;

public class MemorySearchService implements SearchService{
    private final NavigableMap<String, List<Long>> index = new TreeMap<>();

    @Override
    public List<Long> search(String word) {
        if (word == null) {
            return Collections.emptyList();
        }
        String key = word.toLowerCase(Locale.ROOT);

        synchronized (index) {
            List<Long> hits = index.get(key);
            return (hits == null)
                    ? Collections.emptyList()
                    : new ArrayList<>(hits);
        }
    }

    @Override
    public void addThroughSearchCache(long messageId, String word) {
        String key = word.toLowerCase();

        synchronized (index) {
            List<Long> hits = index.computeIfAbsent(key, k -> new ArrayList<>());
            if (hits.isEmpty() || hits.getLast() != messageId) {
                hits.add(messageId);
            }
        }
    }
}
