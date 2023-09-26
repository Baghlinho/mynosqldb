package com.capstone.node.handler.index;

import com.capstone.node.core.DatabaseManager;
import com.capstone.node.core.Entry;
import com.capstone.node.handler.QueryHandler;
import com.capstone.node.service.index.IndexKey;
import com.capstone.node.service.index.IndexService;
import com.capstone.node.core.Query;
import com.fasterxml.jackson.databind.JsonNode;

public class ReadHandler extends QueryHandler {

    private final IndexService indexService = DatabaseManager.getInstance().getIndexService();

    @Override
    public void handle(Query query) {
        Entry<String, JsonNode> filterKey = query.getFilterKey();
        if (filterKey == null) {
            pass(query);
            return;
        }

        IndexKey key = new IndexKey(query.getDatabaseName(), filterKey.getKey());
        if (indexService.containsIndex(key))
            query.setIndex(indexService.getIndex(key).get());
        pass(query);
    }
}
