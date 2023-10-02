package com.capstone.node.handler;

import com.capstone.node.core.Query;
import com.capstone.node.core.QueryType;
import com.capstone.node.core.DatabaseManager;
import com.capstone.node.handler.broadcast.BroadcastHandler;
import com.capstone.node.handler.login.LoginHandler;
import com.capstone.node.handler.register.RegisterHandler;
import com.capstone.node.handler.schema.SchemaHandler;

import java.util.Arrays;

public class HandlerFactory {
    DatabaseManager manager = DatabaseManager.getInstance();

    public QueryHandler getHandler(Query query) {
        switch (query.getOriginator()) {
            case User:
                return getUserHandler(query);
            case Broadcaster:
                return getBroadcastHandler(query);
            case Deferrer:
                return getDeferrerHandler();
            case SelfUpdate:
                return getSelfUpdateHandler();
        }
        return null;
    }

    private QueryHandler getSelfUpdateHandler() {
        QueryHandler handlerChain = manager.getCacheService().getHandler();
        handlerChain
                .setNext(manager.getIndexService().getHandler())
                .setNext(manager.getDatabaseService().getHandler());
        return handlerChain;
    }

    private QueryHandler getUserHandler(Query request) {
        QueryHandler handlerChain = manager.getLockService().getHandler();

        if(request.getQueryType() == QueryType.Login) {
            handlerChain.setNext(new LoginHandler());
            return handlerChain;
        }

        if(request.getQueryType() == QueryType.RegisterUser) {
            handlerChain
                    .setNext(new RegisterHandler())
                    .setNext(manager.getIndexService().getHandler())
                    .setNext(manager.getDatabaseService().getHandler())
                    .setNext(new BroadcastHandler());
            return handlerChain;
        }

        if(oneOf(request, QueryType.CreateIndex, QueryType.DeleteIndex)) {
            handlerChain
                    .setNext(manager.getCacheService().getHandler())
                    .setNext(manager.getIndexService().getHandler())
                    .setNext(new BroadcastHandler());
        } else {
            handlerChain
                    .setNext(manager.getCacheService().getHandler())
                    .setNext(new SchemaHandler())
                    .setNext(manager.getIndexService().getHandler())
                    .setNext(manager.getDatabaseService().getHandler())
                    .setNext(new BroadcastHandler());
        }
        return handlerChain;
    }

    private QueryHandler getBroadcastHandler(Query query) {
        QueryHandler handlerChain = manager.getLockService().getHandler();

        if(oneOf(query, QueryType.CreateIndex, QueryType.DeleteIndex)) {
            handlerChain
                    .setNext(manager.getCacheService().getHandler())
                    .setNext(manager.getIndexService().getHandler());
        } else {
            handlerChain
                    .setNext(manager.getCacheService().getHandler())
                    .setNext(new SchemaHandler())
                    .setNext(manager.getIndexService().getHandler())
                    .setNext(manager.getDatabaseService().getHandler());
        }
        return handlerChain;
    }

    private QueryHandler getDeferrerHandler() {
        QueryHandler handlerChain = manager.getLockService().getHandler();
        handlerChain
                .setNext(manager.getCacheService().getHandler())
                .setNext(new SchemaHandler())
                .setNext(manager.getIndexService().getHandler())
                .setNext(manager.getDatabaseService().getHandler())
                .setNext(new BroadcastHandler());
        return handlerChain;
    }


    private boolean oneOf(Query request, QueryType... types) {
        return Arrays.stream(types).anyMatch(t -> request.getQueryType() == t);
    }


}
