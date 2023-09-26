package com.capstone.node.handler;


import com.capstone.node.core.Query;

public abstract class QueryHandler {

    protected QueryHandler nextHandler;

    public abstract void handle(Query query) ;
    public QueryHandler setNext(QueryHandler handler) {
        nextHandler = handler;
        return handler;
    }
    protected void pass(Query query)  {
        if(nextHandler != null)
            nextHandler.handle(query);
    }

    protected boolean updateNotNeeded(Query query){
        return query.getStatus() != Query.Status.Accepted;
    }
}
