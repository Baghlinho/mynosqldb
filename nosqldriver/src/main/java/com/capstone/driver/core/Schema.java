package com.capstone.driver.core;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Schema {
    private final Map<String, JsonType> properties;

    private Schema () {
        properties = new HashMap<>();
    }

    public static Schema create() {
        return new Schema();
    }
    public Schema addProperty(String property, JsonType type) {
        properties.put(property, type);
        return this;
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    public String toJson() {
        return new JSONObject(properties).toString();
    }
}
