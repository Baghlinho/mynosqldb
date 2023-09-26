package com.capstone.driver.client;

import com.capstone.driver.core.JsonBody;
import com.capstone.driver.core.RequestSender;
import com.capstone.driver.operation.IndexOperations;

import java.net.http.HttpResponse;

public class IndexClient extends RestClient implements IndexOperations {

    public IndexClient(RequestSender requestSender) {
        super(requestSender, "/index/%s/%s/%s");
    }

    @Override
    public boolean create(String databaseName, String property) {
        if(property == null || property.isEmpty())
            throw new IllegalArgumentException("Database property name must be provided");
        HttpResponse<String> response = requestSender.sendPost(
                endpoint.formatted("create", databaseName, property),
                JsonBody.create().toJson()
        );
        return response.statusCode()==200 && response.body().isEmpty();
    }

    @Override
    public boolean delete(String databaseName, String property) {
        if(property == null || property.isEmpty())
            throw new IllegalArgumentException("Database property name must be provided");
        HttpResponse<String> response = requestSender.sendPost(
                endpoint.formatted("delete", databaseName, property),
                JsonBody.create().toJson()
        );
        return response.statusCode()==200 && response.body().equals("Accepted => ");
    }
}
