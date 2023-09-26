package com.capstone.node.controller;

import com.capstone.node.core.DatabaseManager;
import com.capstone.node.core.Query;
import com.capstone.node.core.QueryType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/database")
public class DatabaseController {

    ObjectMapper mapper = new ObjectMapper();
    DatabaseManager manager = DatabaseManager.getInstance();

    @PostMapping("/create/{database}")
    public String createDatabase(@RequestBody Map<String, Object> schema, @PathVariable("database") String databaseName) {
        Query request = Query.builder()
                .originator(Query.Originator.User)
                .queryType(QueryType.CreateDatabase)
                .databaseName(databaseName)
                .payload(mapper.valueToTree(schema))
                .build();
        manager.getHandlersFactory().getHandler(request).handle(request);
        return request.getRequestOutput().toString();
    }

    @PostMapping("/delete.sh/{database}")
    public String deleteDatabase(@PathVariable("database") String databaseName) {
        Query request = Query.builder()
                .originator(Query.Originator.User)
                .queryType(QueryType.DeleteDatabase)
                .databaseName(databaseName)
                .build();
        manager.getHandlersFactory().getHandler(request).handle(request);
        return request.getRequestOutput().toString();
    }

}
