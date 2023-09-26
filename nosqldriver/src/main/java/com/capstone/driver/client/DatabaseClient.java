package com.capstone.driver.client;

import com.capstone.driver.core.JsonBody;
import com.capstone.driver.core.RequestSender;
import com.capstone.driver.core.Schema;
import com.capstone.driver.operation.DatabaseOperations;

import java.net.http.HttpResponse;

public class DatabaseClient extends RestClient implements DatabaseOperations {

    public DatabaseClient(RequestSender requestSender) {
        super(requestSender, "/database/%s/%s");
    }

    @Override
    public boolean create(String databaseName, Schema schema) {
        if(schema == null || schema.isEmpty())
            throw new IllegalArgumentException("Database schema must be provided");
        HttpResponse<String> response = requestSender.sendPost(
                endpoint.formatted("create", databaseName),
                schema.toJson()
        );
        return response.statusCode()==200 && response.body().isEmpty();
    }

    @Override
    public boolean delete(String databaseName) {
        HttpResponse<String> response = requestSender.sendPost(
                endpoint.formatted("delete", databaseName),
                JsonBody.create().toJson()
        );
        return response.statusCode()==200 && response.body().isEmpty();
    }
}
