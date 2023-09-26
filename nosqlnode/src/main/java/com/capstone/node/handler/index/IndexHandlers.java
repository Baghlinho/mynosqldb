package com.capstone.node.handler.index;

import com.capstone.node.handler.QueryHandler;
import com.capstone.node.core.Query;

import java.util.Optional;

public class IndexHandlers {

    private IndexHandlers() {}

    public static Optional<QueryHandler> getHandler(Query query, QueryHandler nextHandler) {
        QueryHandler handler = null;
        switch (query.getQueryType()) {
            case FindDocument:
            case FindDocuments:
                handler = new ReadHandler();
                break;
            case AddDocument:
            case UpdateDocument:
            case DeleteDocument:
                handler = new DocumentHandler();
                break;
            case CreateIndex:
            case DeleteIndex:
                handler = new CreationHandler();
                break;
            case DeleteDatabase:
                handler = new DeleteDatabaseHandler();
                break;
            case RegisterUser:
                handler = new RegisterUserHandler();
                break;
        }

        if(handler == null)
            return Optional.empty();

        handler.setNext(nextHandler);
        return Optional.of(handler);
    }


}
