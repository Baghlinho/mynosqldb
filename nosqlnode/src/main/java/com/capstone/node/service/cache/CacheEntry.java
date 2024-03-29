package com.capstone.node.service.cache;

import com.capstone.node.core.QueryType;
import com.capstone.node.core.Query;
import com.fasterxml.jackson.databind.JsonNode;
import com.capstone.node.core.Entry;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CacheEntry {

    private QueryType type;
    private String databaseName;
    private Entry<String, JsonNode> filterKey;
    private List<String> requiredProperties;
    private Set<String> usedDocuments;

    public CacheEntry(Query request) {
        this.type = request.getQueryType();
        this.databaseName = request.getDatabaseName();
        this.filterKey = request.getFilterKey();
        if(request.getRequiredProperties() != null) {
            this.requiredProperties = request.getRequiredProperties().stream().collect(Collectors.toList()); // copy the list
            Collections.sort(this.requiredProperties); // sort the list
        }

        this.usedDocuments = request.getUsedDocuments();
    }

    public QueryType getType() {
        return type;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public Entry<String, JsonNode> getFilterKey() {
        return filterKey;
    }

    public List<String> getRequiredProperties() {
        return requiredProperties;
    }

    public Set<String> getUsedDocuments() {
        return usedDocuments;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheEntry that = (CacheEntry) o;
        return type == that.type && databaseName.equals(that.databaseName) && Objects.equals(filterKey, that.filterKey) && Objects.equals(requiredProperties, that.requiredProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, databaseName, filterKey, requiredProperties);
    }

    @Override
    public String toString() {
        return "CacheEntry{" +
                "type=" + type +
                ", databaseName='" + databaseName + '\'' +
                ", filterKey=" + filterKey +
                ", requiredProperties=" + requiredProperties +
                ", usedDocuments=" + usedDocuments +
                '}';
    }
}
