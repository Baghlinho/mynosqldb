package com.capstone.node.handler.database;

import com.capstone.node.core.DatabaseManager;
import com.capstone.node.service.database.Database;
import com.capstone.node.service.database.DatabaseService;
import com.capstone.node.handler.QueryHandler;
import com.capstone.node.core.Query;
import com.fasterxml.jackson.databind.JsonNode;

public class RegisterUserHandler extends QueryHandler {

    private final DatabaseService databaseService = DatabaseManager.getInstance().getDatabaseService();

    @Override
    public void handle(Query query) {
        JsonNode user = query.getPayload();
        System.out.println(user.toPrettyString());
        Database users = databaseService.getDatabase("_Users");
        users.addDocument(user.get("_id").asText(), user);
        pass(query);
    }

}
