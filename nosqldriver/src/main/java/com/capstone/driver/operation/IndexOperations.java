package com.capstone.driver.operation;

public interface IndexOperations {
    boolean create(String databaseName, String property);
    boolean delete(String databaseName, String property);
}
