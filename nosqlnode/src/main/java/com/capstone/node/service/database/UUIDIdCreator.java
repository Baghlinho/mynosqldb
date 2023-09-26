package com.capstone.node.service.database;

import com.capstone.node.core.Query;

import java.util.UUID;

public class UUIDIdCreator implements IdCreator {
    @Override
    public String createId(Query request) {
        return UUID.randomUUID().toString();
    }
}
