package com.capstone.node.handler.database;

import com.capstone.node.handler.QueryHandler;
import com.capstone.node.core.Query;

public class DatabaseHandlers {

    public static QueryHandler getHandler(Query query, QueryHandler nextHandler) {
        QueryHandler handler = null;
        switch (query.getQueryType()) {
            case CreateDatabase:
            case DeleteDatabase:
                handler = new CreationHandler();
                break;
            case AddDocument:
            case DeleteDocument:
            case UpdateDocument:
                handler = new DocumentHandler();
                break;
            case FindDocument:
            case FindDocuments:
                handler = new FindDocumentHandler();
                break;
            case RegisterUser:
                handler = new RegisterUserHandler();
                break;
        }

        handler.setNext(nextHandler);
        return handler;
    }
}
