package com.capstone.node.handler.index;

import com.capstone.node.core.DatabaseManager;
import com.capstone.node.service.database.DatabaseService;
import com.capstone.node.handler.QueryHandler;
import com.capstone.node.service.index.IndexKey;
import com.capstone.node.service.index.IndexService;
import com.capstone.node.core.Query;
import com.capstone.node.core.QueryType;

public class CreationHandler extends QueryHandler {

    private IndexService indexService = DatabaseManager.getInstance().getIndexService();
    private DatabaseService databaseService = DatabaseManager.getInstance().getDatabaseService();

    @Override
    public void handle(Query query) {

        if(query.getQueryType() == QueryType.CreateIndex)
            handleCreateIndex(query);
        else
            handleDeleteIndex(query);

    }

    private void handleCreateIndex(Query query) {
        IndexKey key = createKey(query);
        if (indexService.containsIndex(key)) {
            query.setStatus(Query.Status.Rejected);
            query.getRequestOutput().append("Index already exists");
            return;
        }

        if (!databaseService.containsDatabase(query.getDatabaseName())) {
            query.setStatus(Query.Status.Rejected);
            query.getRequestOutput().append("Database Doesn't exist");
            return;
        }

        indexService.createIndex(key);
        pass(query);

        query.setStatus(Query.Status.Accepted);
    }

    private void handleDeleteIndex(Query query) {
        IndexKey key = createKey(query);
        if (!indexService.containsIndex(key)) {
            query.setStatus(Query.Status.Rejected);
            query.getRequestOutput().append("Index doesn't exist");
            return;
        }

        indexService.deleteIndex(key);
        pass(query);

        query.setStatus(Query.Status.Accepted);
    }

    private IndexKey createKey(Query request) {
        return new IndexKey(request.getDatabaseName(), request.getIndexFieldName());
    }
}
