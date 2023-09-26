package com.capstone.node.handler.index;

import com.capstone.node.core.DatabaseManager;
import com.capstone.node.handler.QueryHandler;
import com.capstone.node.service.index.IndexService;
import com.capstone.node.core.Query;

public class DeleteDatabaseHandler extends QueryHandler {

    IndexService indexService = DatabaseManager.getInstance().getIndexService();

    @Override
    public void handle(Query query) {
        pass(query);
        if (query.getStatus() == Query.Status.Rejected)
            return;
        indexService.deleteDatabaseIndices(query.getDatabaseName());
    }


}
