package com.capstone.node.handler.broadcast;

import com.capstone.node.handler.QueryHandler;
import com.capstone.node.core.Query;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleBroadcast extends QueryHandler {

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(Query query) {
        switch (query.getQueryType()) {
            case AddDocument:
                addDocument(query);
                return;
            case DeleteDocument:
                deleteDocument(query);
                return;
            case DeleteDatabase:
                deleteDatabase(query);
                return;
            case CreateDatabase:
                createDatabase(query);
                return;
            case CreateIndex:
                createIndex(query);
                return;
            case DeleteIndex:
                deleteIndex(query);
                return;
            case RegisterUser:
                registerUser(query);
        }
    }

    public void addDocument(Query request) {
        String payload = request.getPayload().toString();
        String databaseName = request.getDatabaseName();
        // async broadcasting

        BroadcastUtils.broadcast("add_document", databaseName, payload);
        request.setStatus(Query.Status.Accepted);
    }

    private void registerUser(Query query) {
        BroadcastUtils.broadcast("add_user", "", query.getPayload().toString());
        query.setStatus(Query.Status.Accepted);
    }

    private void createIndex(Query request) {
        BroadcastUtils.broadcast("create_index", request.getDatabaseName() + "/" + request.getIndexFieldName(), "{}");
        request.setStatus(Query.Status.Accepted);
    }

    private void deleteIndex(Query request) {
        BroadcastUtils.broadcast("delete_index", request.getDatabaseName() + "/" + request.getIndexFieldName(), "{}");
        request.setStatus(Query.Status.Accepted);
    }

    private void createDatabase(Query request) {
        BroadcastUtils.broadcast("create_database", request.getDatabaseName(), request.getPayload().toString());
        request.setStatus(Query.Status.Accepted);
    }

    private void deleteDatabase(Query request) {
        BroadcastUtils.broadcast("delete_database", request.getDatabaseName(), "{}");
        request.setStatus(Query.Status.Accepted);
    }

    public void deleteDocument(Query request) {
        List<String> ids = new ArrayList<>(request.getUsedDocuments());
        String databaseName = request.getDatabaseName();
        Map<String, Object> map = new HashMap<>();
        map.put("_ids", ids);
        JsonNode json = mapper.valueToTree(map);

        // async broadcasting
        BroadcastUtils.broadcast("delete_document", databaseName, json.toString());
        request.setStatus(Query.Status.Accepted);
    }

}
