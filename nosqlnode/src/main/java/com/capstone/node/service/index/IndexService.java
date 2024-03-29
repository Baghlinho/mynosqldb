package com.capstone.node.service.index;

import com.capstone.node.core.DatabaseManager;
import com.capstone.node.core.MetaData;
import com.capstone.node.service.database.DatabaseService;
import com.capstone.node.handler.index.IndexHandler;
import com.capstone.node.handler.QueryHandler;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class IndexService {

    private static final int INDEX_DEGREE = 100;

    private static IndexService INSTANCE;

    public static IndexService getInstance() throws IOException, ClassNotFoundException {
        if(INSTANCE != null)
            return INSTANCE;
        return INSTANCE = new IndexService(MetaData.getInstance());
    }


    private Set<IndexKey> indexes;
    private File indexesDirectory;

    private IndexService(MetaData metaData) {
        this.indexes = Collections.synchronizedSet(new HashSet<>());
        this.indexesDirectory = Paths.get(metaData.getIndexesDirectory()).toFile();
        if (!indexesDirectory.exists())
            indexesDirectory.mkdirs();
        createInitialIndexes(metaData);
    }

    public List<IndexKey> getIndexesKeys() {
        synchronized (indexes) {
            return indexes.stream().collect(Collectors.toList());
        }
    }

    private void createInitialIndexes(MetaData metaData) {
        DatabaseService databaseService = DatabaseManager.getInstance().getDatabaseService();
        metaData.getIndexesIdentifiers()
                .stream()
                .map(name -> name.split(":"))
                .map(name -> new IndexKey(name[0], name[1]))
                .forEach(key -> {
                    Index index = new BTreeIndex(5, new JsonComparator());
                    databaseService.getDatabase(key.getDatabaseName())
                            .getAllDocuments()
                            .filter(document -> document.has(key.getField()))
                            .forEach(document -> index.add(document.get(key.getField()), document.get("_id").asText()));

                    indexes.add(key);
                    try {
                        saveToFile(key, index);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    public Optional<Index> getIndex(IndexKey key)  {
        if (!containsIndex(key))
            return null;
        return Optional.of(loadIndexFromDisk(key));
    }

    public void createIndex(IndexKey key)  {
        Index index = new BTreeIndex(5, new JsonComparator());
        calculateIndex(key, index);

        indexes.add(key);
        saveToFile(key, index);
        {
            System.out.println("traversing index " + key);
            ((BTreeIndex) index).traverse();
        }
        DatabaseManager manager = DatabaseManager.getInstance();
        manager.lockMetaData();
        // update meta data
        try {
            manager.getConfiguration().getIndexesIdentifiers().add(key.getDatabaseName() + ":" + key.getName());
        } finally {
            manager.saveMetaData();
            manager.unlockMetaData();
        }
    }

    public void deleteIndex(IndexKey key)  {
        indexes.remove(key);
        deleteFile(key);

        DatabaseManager manager = DatabaseManager.getInstance();
        manager.lockMetaData();
        // update meta data
        try {
            manager.getConfiguration().getIndexesIdentifiers().remove(key.getDatabaseName() + ":" + key.getName());
        } finally {
            manager.saveMetaData();
            manager.unlockMetaData();
        }
    }

    public void calculateIndex(IndexKey key, Index index) {
        DatabaseService databaseService = DatabaseManager.getInstance().getDatabaseService();
        databaseService.getDatabase(key.getDatabaseName())
                .getAllDocuments()
                .filter(document -> document.has(key.getField()))
                .forEach(document -> index.add(document.get(key.getField()), document.get("_id").asText()));
    }

    public void saveToFile(IndexKey key, Index index)  {
        try (ObjectOutputStream stream = new ObjectOutputStream(
                new FileOutputStream(indexesDirectory.toPath().resolve(key.getName() + ".dat").toFile()))) {
            stream.writeObject(index);
        } catch(Exception e) {
            System.out.println("Can't write index to file");
            throw new RuntimeException("Can't write index to file");
        }
    }

    public void deleteDatabaseIndices(String databaseName) {
        indexes
                .stream()
                .collect(Collectors.toList())
                .stream()
                .filter(key -> key.getDatabaseName().equals(databaseName))
                .forEach(key -> deleteIndex(key));
    }

    public boolean containsIndex(IndexKey key) {
        return key != null && indexes.contains(key);
    }

    private void deleteFile(IndexKey key) {
        indexesDirectory.toPath().resolve(key.getName() + ".dat").toFile().delete();
    }

    private Index loadIndexFromDisk(IndexKey key)  {
        try (ObjectInputStream stream = new ObjectInputStream(
                new FileInputStream(indexesDirectory.toPath().resolve(key.getName() + ".dat").toFile()))) {
            Index index = (Index) stream.readObject();
            return index;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to load exception from disk");
            throw new RuntimeException("Unable to load exception from disk");
        }
    }

    public QueryHandler getHandler() {
        return new IndexHandler();
    }
}
