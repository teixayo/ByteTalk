package me.teixayo.bytetalk.backend.service.search;

import java.util.List;

public interface SearchService {

    static SearchService findBestService() {
//        if(ElasticDBConnection.isConnected()) return new ElasticSearchService();
        return new MemorySearchService();
    }

    List<Long> search(String word);

    void addThroughSearchCache(long messsageId, String word);
}
