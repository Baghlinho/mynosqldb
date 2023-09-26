package com.capstone.node.handler.index;

import com.capstone.node.core.DatabaseManager;
import com.capstone.node.handler.QueryHandler;
import com.capstone.node.service.index.Index;
import com.capstone.node.service.index.IndexKey;
import com.capstone.node.service.index.IndexService;
import com.capstone.node.core.Query;

public class RegisterUserHandler extends QueryHandler {

    IndexService indexService = DatabaseManager.getInstance().getIndexService();

    @Override
    public void handle(Query query) {
        // set the index to the name of the user
        IndexKey key = new IndexKey("_Users", "username");
        Index index = indexService.getIndex(key).get();
        query.setIndex(index);

        pass(query);
        index.add(query.getPayload().get("username"), query.getPayload().get("_id").asText());
        indexService.saveToFile(key, index);
    }
}
