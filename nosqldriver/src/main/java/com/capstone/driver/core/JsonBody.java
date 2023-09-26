package com.capstone.driver.core;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class JsonBody {

    private final JSONObject request;

    private JsonBody() {
        request = new JSONObject();
    }

    public static JsonBody create() {
        return new JsonBody();
    }

    public JsonBody filter(Map.Entry<String, Object> filter) {
        if(filter == null) return this;
        request.put("filter", Map.of(filter.getKey(), filter.getValue()));
        return this;
    }

    public JsonBody payload(Map<String, Object> payload) {
        request.put("payload", payload);
        return this;
    }

    public JsonBody requiredProperties(List<String> requiredProperties) {
        request.putOnce("requiredProperties", requiredProperties);
        return this;
    }

    public JsonBody username(String username) {
        request.putOnce("username", username);
        return this;
    }

    public JsonBody password(String password) {
        request.putOnce("password", password);
        return this;
    }

    public JsonBody role(Role role) {
        request.putOnce("role", role);
        return this;
    }

    public String toJson() {
        return request.toString();
    }

}
