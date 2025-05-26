package me.teixayo.bytetalk.backend.database.impl.search;

import java.util.List;

public class ElasticSearchService implements SearchService {


    @Override
    public List<Long> search(String word) {
        return List.of();
    }

    @Override
    public void addThroughSearchCache(long messsageId, String word) {

    }
}
