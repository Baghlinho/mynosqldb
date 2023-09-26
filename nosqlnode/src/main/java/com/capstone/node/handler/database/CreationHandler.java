package com.capstone.node.handler.database;

import com.capstone.node.core.DatabaseManager;
import com.capstone.node.service.database.Database;
import com.capstone.node.service.database.DatabaseService;
import com.capstone.node.handler.QueryHandler;
import com.capstone.node.core.Query;
import com.capstone.node.core.QueryType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CreationHandler extends QueryHandler {

    DatabaseService databaseService = DatabaseManager.getInstance().getDatabaseService();

    @Override
    public void handle(Query query) {
        if(query.getQueryType() == QueryType.CreateDatabase)
            createDatabase(query);
        else
            deleteDatabase(query);
    }

    private void createDatabase(Query query) {
        if (databaseService.containsDatabase(query.getDatabaseName())) {
            query.getRequestOutput().append("Database Already Exists");
            query.setStatus(Query.Status.Rejected);
            return;
        }
        databaseService.createDatabase(query.getDatabaseName());
        pass(query);
    }

    private void deleteDatabase(Query request) {
        Database database = databaseService.getDatabase(request.getDatabaseName());
        List<String> ids = database.getAllDocuments()
                .map(document -> document.get("_id").asText())
                .collect(Collectors.toList());
        DatabaseUtil.decrementAffinity(database, ids);
        databaseService.deleteDatabase(request.getDatabaseName());
        pass(request);
    }


}
