package com.capstone.node.handler.database;

import com.capstone.node.core.DatabaseManager;
import com.capstone.node.core.Entry;
import com.capstone.node.core.MetaData;
import com.capstone.node.core.Node;
import com.capstone.node.service.database.Database;
import com.capstone.node.core.Query;
import com.capstone.node.core.QueryType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DatabaseUtil {

    static DatabaseManager manager = DatabaseManager.getInstance();

    /*
     * a simple affinity calculation scheme
     * that assign the newly created document's affinity to the
     * node with the least number of assigned documents
     */
    public static int calculateAffinity() {
        int nodeId = manager.getConfiguration().getNodeId();
        try {
            MetaData metaData = manager.getConfiguration();
            manager.lockMetaData();
            List<Node> nodes = metaData.getNodes();
            int numDocuments = manager.getConfiguration().getNumDocuments();
            for (Node node : nodes) {
                if (node.getNumDocuments() < numDocuments) {
                    nodeId = node.getId();
                    numDocuments = node.getNumDocuments();
                }
            }
        } finally {
            manager.unlockMetaData();
        }
        return nodeId;
    }

    public static void incrementAffinity(int nodeId)  {
        manager.lockMetaData();
        try {
            MetaData metaData = manager.getConfiguration();
            List<Node> nodes = metaData.getNodes();
            if (nodeId == metaData.getNodeId()) {
                manager.getConfiguration().incNumDocuments();
            } else {
                final int nid = nodeId;
                nodes.stream().filter(node -> node.getId() == nid).findFirst().ifPresent(Node::incNumDocuments);
            }
        } finally {
            manager.saveMetaData();
            manager.unlockMetaData();
        }

    }

    public static void decrementAffinity(Database database, Collection<String> documentIndexes) {
        manager.lockMetaData();
        try {
            MetaData metaData = manager.getConfiguration();
            List<Node> nodes = metaData.getNodes();
            database // update nodes affinity
                    .getDocuments(documentIndexes)
                    .map(document -> document.get("_affinity").asInt())
                    .forEach(affinity ->  {
                        nodes.stream()
                                .filter(node -> node.getId() == affinity)
                                .findFirst().ifPresent(Node::decNumDocuments);
                        if(affinity == metaData.getNodeId())
                            metaData.decNumDocuments();
                    });
//            documentIndexes.stream() // update self affinity
//                    .filter(id -> metaData.getNodeId().equals(id))
//                    .forEach(id -> metaData.decNumDocuments());
        } finally {
            manager.saveMetaData();
            manager.unlockMetaData();
        }
    }


    public static List<String> indexRequest(Query request, Database database)  {
        // broadcaster provides the ids of the documents to be deleted
        if (request.getOriginator() == Query.Originator.Broadcaster && request.getQueryType() == QueryType.DeleteDocument) {
            List<String> ids;
            try {
                ids = new ObjectMapper().treeToValue(request.getPayload().get("_ids"),
                        TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
            } catch(Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Unable to index the request");
            }
            return ids.stream().filter(database::contains).collect(Collectors.toList());
        }

        if (request.getIndex() != null) {
            {
                System.out.println("using index for " + request.getFilterKey());
            }
            return request.getIndex().search(request.getFilterKey().getValue());
        }

        Stream<JsonNode> stream = database.getAllDocuments();
        Entry<String, JsonNode> filterKey = request.getFilterKey();
        if (request.getFilterKey() != null)
            stream = stream
                    .filter(document -> document.has(filterKey.getKey())
                            && document.get(filterKey.getKey()).equals(filterKey.getValue()));
        return stream
                .map(document -> document.get("_id").asText())
                .collect(Collectors.toList());
    }

    public static JsonNode filterFields(JsonNode node, List<String> requiredProperties) {
        if (requiredProperties == null || requiredProperties.isEmpty())
            return node;
        Map<String, Object> properties = new HashMap<>();
        for (String field : requiredProperties) {
            if (!node.has(field))
                continue;
            properties.put(field, node.get(field));
        }

        ObjectMapper mapper = new ObjectMapper();
        return mapper.valueToTree(properties);
    }


}
