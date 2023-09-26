package com.capstone.driver;

import com.capstone.driver.client.*;
import com.capstone.driver.core.RequestSender;
import com.capstone.driver.operation.AuthOperations;
import com.capstone.driver.operation.DatabaseOperations;
import com.capstone.driver.operation.DocumentOperations;
import com.capstone.driver.operation.IndexOperations;

public class MyNoSQLDriver {

    private final DocumentOperations documentOperations;
    private final DatabaseOperations databaseOperations;
    private final AuthOperations authOperations;
    private final IndexOperations indexOperations;

    public MyNoSQLDriver () {
        RequestSender requestSender = new RequestSender();
        documentOperations = new DocumentClient(requestSender);
        databaseOperations = new DatabaseClient(requestSender);
        authOperations = new AuthClient(requestSender);
        indexOperations = new IndexClient(requestSender);
    }

    public DocumentOperations document() {
        return documentOperations;
    }

    public AuthOperations auth() {
        return authOperations;
    }

    public IndexOperations index() {
        return indexOperations;
    }

    public DatabaseOperations database() {
        return databaseOperations;
    }

}
