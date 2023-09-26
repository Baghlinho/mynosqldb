package com.capstone.driver.client;

import com.capstone.driver.core.JsonBody;
import com.capstone.driver.core.RequestSender;
import com.capstone.driver.operation.DocumentOperations;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class DocumentClient extends RestClient implements DocumentOperations {

    public DocumentClient(RequestSender requestSender) {
        super(requestSender, "/document/%s/%s");
    }

    @Override
    public String findAny(String databaseName,
                          Map.Entry<String, Object> filter,
                          List<String> shownProperties) {
        HttpResponse<String> response = requestSender.sendGet(
                endpoint.formatted("find", databaseName),
                JsonBody.create().filter(filter).requiredProperties(shownProperties).toJson()
        );
        if(response.statusCode() == 202)
            return response.body();
        return "Find request unsuccessful";
    }
    @Override
    public String findAny(String databaseName) {
        return findAny(databaseName, null, null);
    }
    @Override
    public String findAny(String databaseName, Map.Entry<String, Object> filter) {
        return findAny(databaseName, filter, null);
    }
    @Override
    public String findAny(String databaseName, List<String> shownProperties) {
        return findAny(databaseName, null, shownProperties);
    }

    @Override
    public String findAll(String databaseName,
                          Map.Entry<String, Object> filter,
                          List<String> shownProperties) {
        HttpResponse<String> response = requestSender.sendGet(
                endpoint.formatted("finds", databaseName),
                JsonBody.create().filter(filter).requiredProperties(shownProperties).toJson()
        );
        if(response.statusCode() == 202)
            return response.body();
        return "Find all request unsuccessful";
    }
    @Override
    public String findAll(String databaseName) {
        return findAll(databaseName, null, null);
    }
    @Override
    public String findAll(String databaseName, Map.Entry<String, Object> filter) {
        return findAll(databaseName, filter, null);
    }
    @Override
    public String findAll(String databaseName, List<String> shownProperties) {
        return findAll(databaseName, null, shownProperties);
    }

    @Override
    public boolean add(String databaseName, Map<String, Object> payload) {
        if(payload == null || payload.isEmpty())
            throw new IllegalArgumentException("Updated document fields payload must be provided");
        HttpResponse<String> response = requestSender.sendPost(
                endpoint.formatted("add", databaseName),
                JsonBody.create().payload(payload).toJson()
        );
        return response.statusCode() == 200 && response.body().isEmpty();
    }

    @Override
    public boolean delete(String databaseName, Map.Entry<String, Object> filter) {
        if(filter == null)
            throw new IllegalArgumentException("Filter key-value pair must be provided");
        HttpResponse<String> response = requestSender.sendPost(endpoint.formatted(
                "delete", databaseName),
                JsonBody.create().filter(filter).toJson()
        );
        return response.statusCode() == 200 && response.body().isEmpty();
    }

    @Override
    public boolean update(String databaseName,
                          Map.Entry<String, Object> filter,
                          Map<String, Object> payload) {
        if(payload == null || payload.isEmpty())
            throw new IllegalArgumentException("Updated document fields payload must be provided");
        if(filter == null ||
                filter.getKey() == null || filter.getKey().isEmpty() ||
                filter.getValue() == null)
            throw new IllegalArgumentException("Filter key-value pair must be provided");
        HttpResponse<String> response = requestSender.sendPost(
                endpoint.formatted("update", databaseName),
                JsonBody.create().filter(filter).payload(payload).toJson()
        );
        return response.statusCode() == 200 && response.body().isEmpty();
    }
}
