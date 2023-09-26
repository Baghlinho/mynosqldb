package com.capstone.node.handler.schema;

import com.capstone.node.core.DatabaseManager;
import com.capstone.node.service.database.DatabaseService;
import com.capstone.node.handler.QueryHandler;
import com.capstone.node.core.Query;
import com.capstone.node.core.QueryType;
import com.capstone.node.service.schema.FileSchemaStorage;
import com.capstone.node.service.schema.SchemaStorage;
import com.capstone.node.service.schema.SchemaValidator;
import com.capstone.node.service.schema.StaticSchemaValidator;

public class CreationHandler extends QueryHandler {

    SchemaStorage storage = new FileSchemaStorage();
    SchemaValidator validator = new StaticSchemaValidator();
    DatabaseService databaseService = DatabaseManager.getInstance().getDatabaseService();

    @Override
    public void handle(Query query) {
        if(query.getQueryType() == QueryType.CreateDatabase)
            createDatabase(query);
        else if(query.getQueryType() == QueryType.DeleteDatabase)
            deleteDatabase(query);
    }

    private void createDatabase(Query query) {
        // check if the system contains the required database
        if(databaseService.containsDatabase(query.getDatabaseName())) {
            query.setStatus(Query.Status.Rejected);
            query.getRequestOutput().append("Database Already Exists");
            return;
        }

        // validate the schema provided by the user
        if(!validator.validateSchema(query.getPayload())) {
            query.setStatus(Query.Status.Rejected);
            query.getRequestOutput().append("Database Schema is Invalid");
            return;
        }

        // saving schema
        storage.saveSchema(query.getPayload(), query.getDatabaseName());
        // pass query to next handler
        pass(query);
    }

    private void deleteDatabase(Query query) {
        if(!databaseService.containsDatabase(query.getDatabaseName())) {
            query.setStatus(Query.Status.Rejected);
            query.getRequestOutput().append("Database doesn't exist");
            return ;
        }
        // delete the schema for the database
        storage.deleteSchema(query.getDatabaseName());
        // pass query to next handler
        pass(query);
    }


}
