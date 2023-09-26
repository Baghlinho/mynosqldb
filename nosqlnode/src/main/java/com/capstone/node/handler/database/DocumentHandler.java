package com.capstone.node.handler.database;

import com.capstone.node.core.DatabaseManager;
import com.capstone.node.core.Entry;
import com.capstone.node.core.MetaData;
import com.capstone.node.core.Node;
import com.capstone.node.service.database.Database;
import com.capstone.node.service.database.DatabaseService;
import com.capstone.node.service.database.IdCreator;
import com.capstone.node.service.database.UUIDIdCreator;
import com.capstone.node.handler.QueryHandler;
import com.capstone.node.core.Query;
import com.capstone.node.core.QueryType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DocumentHandler extends QueryHandler {

    IdCreator idCreator = new UUIDIdCreator();
    ObjectMapper mapper = new ObjectMapper();
    DatabaseManager manager = DatabaseManager.getInstance();
    DatabaseService databaseService = DatabaseManager.getInstance().getDatabaseService();
    int currentNode = DatabaseManager.getInstance().getConfiguration().getNodeId();

    @Override
    public void handle(Query query) {
        if(query.getQueryType() == QueryType.AddDocument)
            addDocument(query);
        else if(query.getQueryType() == QueryType.DeleteDocument)
            deleteDocument(query);
        else
            updateDocument(query);
    }

    private void addDocument(Query query)  {
        JsonNode payload = query.getPayload();
        String documentIndex;
        int affinity;
        // user added a document
        if (query.getOriginator() == Query.Originator.User) { // User case
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> result = mapper.convertValue(payload, new TypeReference<Map<String, Object>>() {});
            documentIndex = idCreator.createId(query);
            affinity = DatabaseUtil.calculateAffinity();
            result.put("_id", documentIndex);
            result.put("_affinity", affinity);
            payload = mapper.valueToTree(result);
            query.setPayload(payload);
            // broadcaster is accessing the system
        } else { // Broadcaster node case
            documentIndex = payload.get("_id").asText();
            affinity = payload.get("_affinity").asInt();
        }
        query.setUsedDocuments(new HashSet<>(Collections.singletonList(documentIndex)));
        if (!query.getDatabaseName().equals("_Users"))
            DatabaseUtil.incrementAffinity(affinity);
        databaseService.getDatabase(query.getDatabaseName()).addDocument(documentIndex, payload);
        query.setUsedDocuments(new HashSet<>(Collections.singletonList(documentIndex)));
        pass(query);
    }

    private void deleteDocument(Query request)  {
        Database database = databaseService.getDatabase(request.getDatabaseName());
        Set<String> usedDocuments = new HashSet<>();

        manager.lockMetaData();
        try {
            MetaData metaData = manager.getConfiguration();
            List<Node> nodes = metaData.getNodes();
            List<Entry<String, Integer>> info = DatabaseUtil.indexRequest(request, database)
                    .stream()
                    .limit(1)
                    .map(database::getDocument)
                    .map(document -> new Entry<>(document.get("_id").asText(), document.get("_affinity").asInt()))
                    .collect(Collectors.toList());
            List<JsonNode> oldData = new ArrayList<>();
            for(Entry<String, Integer> entry: info) {
                String documentIndex = entry.getKey();
                int affinity = entry.getValue();
                oldData.add(database.getDocument(documentIndex));
                database.deleteDocument(documentIndex);
                usedDocuments.add(documentIndex);

                // this node has the affinity
                if(affinity == (currentNode)) {
                    DatabaseManager.getInstance().getConfiguration().decNumDocuments();
                    continue;
                }
                // another node has the affinity
                nodes.stream()
                        .filter(node -> node.getId() == affinity)
                        .findFirst().ifPresent(Node::decNumDocuments);
            }
            if(info.isEmpty())
                request.setStatus(Query.Status.Rejected);

            request.setOldData(mapper.valueToTree(oldData)); // set old data
        } finally {
            manager.saveMetaData();
            manager.unlockMetaData();
        }

        if (request.getStatus() == Query.Status.Rejected) return;
        request.setUsedDocuments(usedDocuments); // set used documents
        pass(request); // broadcast
    }

    private void updateDocument(Query query) {
        Database database = databaseService.getDatabase(query.getDatabaseName());

        if(query.getOriginator() == Query.Originator.SelfUpdate) {  // self update case
            selfUpdate(query, database);
        } else if(query.getOriginator() == Query.Originator.User) { // user query case
            userUpdate(query, database);
        } else if(query.getOriginator() == Query.Originator.Broadcaster) {
            broadcasterUpdate(query, database);
        } else if(query.getOriginator() == Query.Originator.Deferrer) {
            deferrerUpdate(query, database);
        }
    }

    private void deferrerUpdate(Query query, Database database)  {
        JsonNode broadcasterOldData = query.getPayload().get("old");
        JsonNode payload = query.getPayload().get("payload");
        JsonNode myOldData = database.getDocument(broadcasterOldData.get("_id").asText());
        String documentId = myOldData.get("_id").asText();

        query.setHasAffinity(true); // set affinity to true
        query.setOldData(myOldData); // set old data field
        query.setUsedDocuments(Stream.of(documentId).collect(Collectors.toSet())); // set used documents field
        if(broadcasterOldData.equals(myOldData)) {
            database.updateDocument(payload, documentId); // update
            pass(query); // broadcast
        } else {
            query.setStatus(Query.Status.Rejected);
        }
    }

    private void broadcasterUpdate(Query query, Database database)  {
        JsonNode broadcasterOldData = query.getPayload().get("old");
        JsonNode payload = query.getPayload().get("payload");
        JsonNode myOldData = database.getDocument(broadcasterOldData.get("_id").asText());
        String documentId = myOldData.get("_id").asText();

        query.setOldData(myOldData); // set old data field
        query.setUsedDocuments(Stream.of(documentId).collect(Collectors.toSet())); // set used documents field
        if(broadcasterOldData.equals(myOldData)) {
            database.updateDocument(payload, documentId);
        } else {
            query.setStatus(Query.Status.Rejected);
        }
    }

    private void userUpdate(Query query, Database database)  {
        Optional<JsonNode> optional = DatabaseUtil.indexRequest(query, database)
                .stream()
                .limit(1)
                .map(database::getDocument)
                .findFirst();
        // no document to update
        if(!optional.isPresent()) {
            query.setStatus(Query.Status.Rejected);
            return;
        }
        JsonNode oldData = optional.get();
        String documentId = oldData.get("_id").asText();
        int affinity = oldData.get("_affinity").asInt();
        int thisNodeId = manager.getConfiguration().getNodeId();
        query.setOldData(oldData);
        query.setUsedDocuments(Stream.of(documentId).collect(Collectors.toSet()));

        if(thisNodeId == (affinity)) {
            query.setHasAffinity(true); // declare that affinity is set
            database.updateDocument(query.getPayload(), documentId); // update database
        } else {
            query.setStatus(Query.Status.Rejected); // reject to update current node
        }

        pass(query); // broadcast
    }

    private void selfUpdate(Query query, Database database)  {
        JsonNode payload = query.getPayload();
        String documentId = payload.get("_id").asText();
        JsonNode oldData =  database.getDocument(documentId);
        database.updateDocument(payload, documentId);
        query.setOldData(oldData);
        query.setUsedDocuments(Stream.of(documentId).collect(Collectors.toSet()));
        query.setStatus(Query.Status.Accepted);
    }

}
