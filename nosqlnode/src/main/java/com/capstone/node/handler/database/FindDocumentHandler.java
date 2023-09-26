package com.capstone.node.handler.database;

import com.capstone.node.core.DatabaseManager;
import com.capstone.node.service.database.Database;
import com.capstone.node.service.database.DatabaseService;
import com.capstone.node.handler.QueryHandler;
import com.capstone.node.core.Query;
import com.capstone.node.core.QueryType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FindDocumentHandler extends QueryHandler {

    ObjectMapper mapper = new ObjectMapper();
    DatabaseService databaseService = DatabaseManager.getInstance().getDatabaseService();

    @Override
    public void handle(Query query) {
        if(query.getQueryType() == QueryType.FindDocument)
            findDocument(query);
        else
            findDocuments(query);
    }

    private void findDocument(Query query) {
        Database database = databaseService.getDatabase(query.getDatabaseName());
        List<String> documentIndices = DatabaseUtil.indexRequest(query, database)
                .stream()
                .limit(1)
                .collect(Collectors.toList());

        Set<String> usedDocuments = new HashSet<>(documentIndices);
        query.setUsedDocuments(usedDocuments);
        List<JsonNode> documents = database
                .getDocuments(documentIndices)
                .map(document -> DatabaseUtil.filterFields(document, query.getRequiredProperties()))
                .collect(Collectors.toList());
        if (!documents.isEmpty()) {
            ObjectNode document = (ObjectNode) documents.get(0);
            document.remove("_id");
            document.remove("_affinity");
            query.getRequestOutput().append(document);
        }
    }

    private void findDocuments(Query query) {
        Database database = databaseService.getDatabase(query.getDatabaseName());
        Set<String> documentIndices = new HashSet<>(DatabaseUtil.indexRequest(query, database));
        Stream<JsonNode> documents = database.getDocuments(documentIndices);
        query.setUsedDocuments(documentIndices);

        List<JsonNode> list = documents
                .map(document -> DatabaseUtil.filterFields(document, query.getRequiredProperties()))
                .peek(jsonNode -> {
                    ((ObjectNode) jsonNode).remove("_affinity");
                    ((ObjectNode) jsonNode).remove("_id");
                })
                .collect(Collectors.toList());
        query.getRequestOutput().append(mapper.valueToTree(list).toString());
    }
}
