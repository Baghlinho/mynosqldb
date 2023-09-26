package com.capstone.driver.operation;

import java.util.List;
import java.util.Map;

public interface DocumentOperations {

    String findAny(String databaseName, Map.Entry<String, Object> filter, List<String> shownProperties);
    String findAny(String databaseName);
    String findAny(String databaseName, Map.Entry<String, Object> filter);
    String findAny(String databaseName, List<String> shownProperties);

    String findAll(String databaseName, Map.Entry<String, Object> filter, List<String> shownProperties);
    String findAll(String databaseName);
    String findAll(String databaseName, Map.Entry<String, Object> filter);
    String findAll(String databaseName, List<String> shownProperties);

    boolean add(String databaseName, Map<String, Object> payload);

    boolean delete(String databaseName, Map.Entry<String, Object> filter);

    boolean update(String databaseName, Map.Entry<String, Object> filter, Map<String, Object> payload);
}
