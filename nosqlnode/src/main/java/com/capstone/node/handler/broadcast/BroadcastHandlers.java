package com.capstone.node.handler.broadcast;

import com.capstone.node.handler.QueryHandler;
import com.capstone.node.core.Query;
import com.capstone.node.core.QueryType;

public class BroadcastHandlers {

    public static QueryHandler getHandler(Query query, QueryHandler nextHandler) {
        QueryHandler handler = query.getQueryType() == QueryType.UpdateDocument
                ? new UpdateBroadcast()
                : new SimpleBroadcast();
        handler.setNext(nextHandler);
        return handler;
    }

}
