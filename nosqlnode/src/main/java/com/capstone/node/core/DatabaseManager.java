package com.capstone.node.core;

import com.capstone.node.handler.QueryHandler;
import com.capstone.node.handler.register.RegisterHandler;
import com.capstone.node.service.cache.CacheService;
import com.capstone.node.service.database.DatabaseService;
import com.capstone.node.service.index.IndexService;
import com.capstone.node.service.lock.LockService;
import com.capstone.node.handler.HandlerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class DatabaseManager {

    private static DatabaseManager INSTANCE;

    private DatabaseManager() { }

    private static final int DEFAULT_CONGESTION_THRESHOLD = 1000;


    public static DatabaseManager getInstance() {
        if (INSTANCE == null)
            initialize();
        return INSTANCE;
    }

    private MetaData metaData;
    private Lock metaDataLock;
    private DatabaseService databaseService;
    private IndexService indexService;
    private CacheService cacheService;
    private LockService lockService;

    private final AtomicInteger numLiveRequests = new AtomicInteger(0);
    private final AtomicInteger redirectNodeIndex = new AtomicInteger(0);
    private int congestionThreshold;

    private HandlerFactory handlersFactory;


    public static void initialize() {
        INSTANCE = new DatabaseManager();
        INSTANCE.metaDataLock = new ReentrantLock();
        try {
            INSTANCE.metaData = MetaData.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Something went wrong while loading meta data");
            System.exit(1);
        }


        try { // the environment variable might be missing or incorrectly formatted
            INSTANCE.congestionThreshold = Integer.parseInt(System.getenv("CONGESTION_THRESHOLD"));
        } catch (Exception e) {
            INSTANCE.congestionThreshold = DEFAULT_CONGESTION_THRESHOLD;
        }

        File savePath = Paths.get(INSTANCE.metaData.getSavePath()).toFile();
        if (!savePath.exists()) {
            savePath.mkdirs();
        }

        try {
            INSTANCE.databaseService = DatabaseService.getInstance();
            INSTANCE.lockService = LockService.getInstance();
            INSTANCE.indexService = IndexService.getInstance();
            INSTANCE.cacheService = CacheService.getInstance();
        } catch (Exception e) {
            System.out.println("Something Went wrong while initializing services");
            System.exit(-1);
        }


        INSTANCE.handlersFactory = new HandlerFactory();

        try {
            if(INSTANCE.getConfiguration().isBootstrap()) {
                INSTANCE.initializeAsBootstrap();
            } else {
                INSTANCE.initializeAsWorker();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Something went wrong while initializing node");
            System.exit(1);
        }

    }



    private void initializeAsWorker() throws JsonProcessingException {
        initializeUsers();
        initializeNodes();
    }

    private void initializeAsBootstrap() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        int numNodes = Integer.parseInt(System.getenv("NUM_NODES") != null ? System.getenv("NUM_NODES") : "2");
        List<Node> nodes = new ArrayList<>();
        for(int i = 1; i <= numNodes; i++)
            nodes.add(new Node(i, "10.1.4.1" + i, 0, i == 1 ? 1 : 0));
        metaData.setNodes(nodes);

        String adminUsername = "admin";
        String adminPassword = "admin";

        JsonNode json = mapper.valueToTree(new User(adminUsername, BCrypt.hashpw(adminPassword, BCrypt.gensalt()), User.Role.Admin, 1));

        Map<String, Object> result = mapper.convertValue(json, new TypeReference<Map<String, Object>>() {});
        result.put("_id", "admin");
        result.put("_affinity", 1);
        json = mapper.valueToTree(result);
        Query request = Query.builder()
                .originator(Query.Originator.Broadcaster)
                .databaseName("_Users")
                .queryType(QueryType.AddDocument)
                .payload(json)
                .build();
        handlersFactory.getHandler(request).handle(request);

        List<JsonNode> users = mapper.readValue("[\n" +
                "    {\"username\": \"firas\", \"password\": \"123\", \"role\": \"Admin\"},\n" +
                "    {\"username\": \"sami\", \"password\": \"123\", \"role\": \"Viewer\"},\n" +
                "    {\"username\": \"john\", \"password\": \"123\", \"role\": \"Editor\"}\n" +
                "]", TypeFactory.defaultInstance().constructCollectionType(List.class, JsonNode.class));
        System.out.println(users.toString());
        QueryHandler handler = new RegisterHandler();
        handler
                .setNext(getIndexService().getHandler())
                .setNext(getDatabaseService().getHandler());

        for(JsonNode user: users) {
            handler.handle(Query.builder()
                    .databaseName("_Users")
                    .originator(Query.Originator.User)
                    .queryType(QueryType.RegisterUser)
                    .payload(user)
                    .build());
        }

        saveMetaData();
    }

    private void initializeUsers() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String bootstrapAddress = getConfiguration().getBootstrapAddress();
        String url = String.format("http://%s:8000/_internal/get_users", bootstrapAddress);
        ResponseEntity<String> response = new RestTemplate().getForEntity(url, String.class);

        List<JsonNode> users = mapper.readValue(response.getBody(), TypeFactory.defaultInstance().constructCollectionType(List.class, JsonNode.class));
        for(JsonNode user: users) {
            Query request = Query.builder()
                    .originator(Query.Originator.Broadcaster)
                    .databaseName("_Users")
                    .queryType(QueryType.AddDocument)
                    .payload(user)
                    .build();
            handlersFactory.getHandler(request).handle(request);
        }
    }

    private void initializeNodes() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String bootstrapAddress = getConfiguration().getBootstrapAddress();
        String url = String.format("http://%s:8000/_internal/get_nodes", bootstrapAddress);
        ResponseEntity<String> response = new RestTemplate().getForEntity(url, String.class);
        List<Node> nodes = mapper.readValue(response.getBody(), TypeFactory.defaultInstance().constructCollectionType(List.class, Node.class));
        metaData.setNodes(nodes.stream().filter(node -> node.getId() != metaData.getNodeId()).collect(Collectors.toList()));
        saveMetaData();
    }

    public int getNumRequests() {
        return numLiveRequests.get();
    }

    public int incrementNumRequests() {
        return numLiveRequests.incrementAndGet();
    }

    public int decrementNumRequests() {
        return numLiveRequests.decrementAndGet();
    }

    public int getCongestionThreshold() {
        return congestionThreshold;
    }

    public int getRedirectNodeIndexAndIncrement() {
        return redirectNodeIndex.getAndSet((redirectNodeIndex.get() + 1) % metaData.getNodes().size());
    }




    public MetaData getConfiguration() {
        return metaData;
    }

    public void lockMetaData() {
        metaDataLock.lock();
    }

    public void unlockMetaData() {
        metaDataLock.unlock();
    }

    public void saveMetaData()  {
        lockMetaData();
        try (ObjectOutputStream stream = new ObjectOutputStream(
                Files.newOutputStream(Paths.get(metaData.getSavePath()).resolve("config.dat").toFile().toPath()))) {
            stream.writeObject(metaData);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Can't save meta data");
        } finally {
            unlockMetaData();
        }
    }

    public DatabaseService getDatabaseService() {
        return databaseService;
    }

    public IndexService getIndexService() {
        return indexService;
    }

    public CacheService getCacheService() {
        return cacheService;
    }

    public LockService getLockService() {
        return lockService;
    }

    public HandlerFactory getHandlersFactory() {
        return handlersFactory;
    }

}
