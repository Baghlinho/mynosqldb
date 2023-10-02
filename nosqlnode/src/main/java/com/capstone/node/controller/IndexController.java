package com.capstone.node.controller;

import com.capstone.node.core.DatabaseManager;
import com.capstone.node.core.Query;

import com.capstone.node.core.QueryType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/index")
public class IndexController {

    DatabaseManager manager = DatabaseManager.getInstance();

    @PostMapping("/create/{database}/{index}")
    public String createIndex(@PathVariable("database") String databaseName, @PathVariable("index") String indexFieldName) {
        Query request = Query.builder()
                .originator(Query.Originator.User)
                .queryType(QueryType.CreateIndex)
                .databaseName(databaseName)
                .indexFieldName(indexFieldName)
                .build();
        manager.getHandlersFactory().getHandler(request).handle(request);
        return request.getRequestOutput().toString();
    }

    @PostMapping("/delete/{database}/{index}")
    public String deleteIndex(@PathVariable("database") String databaseName, @PathVariable("index") String indexFieldName) {
        Query request = Query.builder()
                .originator(Query.Originator.User)
                .queryType(QueryType.DeleteIndex)
                .databaseName(databaseName)
                .indexFieldName(indexFieldName)
                .build();
        manager.getHandlersFactory().getHandler(request).handle(request);
        return request.getStatus() + " => " + request.getRequestOutput();
    }

}
