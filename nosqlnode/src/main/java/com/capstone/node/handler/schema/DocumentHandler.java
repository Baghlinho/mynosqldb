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
import com.fasterxml.jackson.databind.JsonNode;

public class DocumentHandler extends QueryHandler {

    SchemaStorage storage = new FileSchemaStorage();
    SchemaValidator validator = new StaticSchemaValidator();
    DatabaseService databaseService = DatabaseManager.getInstance().getDatabaseService();

    @Override
    public void handle(Query query) {
        if(query.getQueryType() == QueryType.AddDocument)
            addDocument(query);
        else if(query.getQueryType() == QueryType.UpdateDocument)
            updateDocument(query);
    }

    private void addDocument(Query query) {
        // no database exist for this query
        if(!databaseService.containsDatabase(query.getDatabaseName())) {
            query.setStatus(Query.Status.Rejected);
            query.getRequestOutput().append("Database doesn't exist");
            return;
        }

        // a broadcaster node is broadcasting the request
        if(query.getOriginator() != Query.Originator.User) {
            pass(query);
            return;
        }

        // validate document to ensure it conforms to the schema
        JsonNode schema = storage.loadSchema(query.getDatabaseName()).get();
        if(!validator.validateDocument(schema, query.getPayload())) {
            query.setStatus(Query.Status.Rejected);
            query.getRequestOutput().append("Document added doesn't conform to the database schema");
            return;
        }
        // pass the query to the next handler
        pass(query);
    }

    private void updateDocument(Query query) {
        // no database exist for this query
        if(!databaseService.containsDatabase(query.getDatabaseName())) {
            query.setStatus(Query.Status.Rejected);
            query.getRequestOutput().append("Database doesn't exist");
            return;
        }

        // a broadcaster or a deferrer node is broadcasting the request
        if(query.getOriginator() != Query.Originator.User) {
            pass(query);
            return;
        }

        // validate schema
        JsonNode schema = storage.loadSchema(query.getDatabaseName()).get();
        if(!validator.validatePartialDocument(schema, query.getPayload())) {
            query.setStatus(Query.Status.Rejected);
            query.getRequestOutput().append("Document updates don't conform to the database schema");
            return;
        }
        pass(query);
    }


}
