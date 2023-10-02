package com.capstone.node.handler.broadcast;

import com.capstone.node.handler.QueryHandler;
import com.capstone.node.core.Query;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class BroadcastHandler extends QueryHandler {

    private static final Logger logger = Logger.getLogger(BroadcastHandler.class.getName());

    static {
        try {
            FileHandler fileHandler = new FileHandler("db/database.log", true);
            logger.setLevel(Level.WARNING);
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handle(Query query) {
        try {
            QueryHandler handler = BroadcastHandlers.getHandler(query, nextHandler);
            handler.handle(query);
        } catch (Exception e) {
            e.printStackTrace();
            query.setStatus(Query.Status.Rejected);
            query.getRequestOutput().append(e.getMessage());
            logger.warning(e.getMessage());
        }
    }

}
