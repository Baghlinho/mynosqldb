package com.capstone.driver.client;

import com.capstone.driver.core.RequestSender;

public abstract class RestClient {

    protected final RequestSender requestSender;
    protected final String endpoint;

    public RestClient(RequestSender requestSender, String endpoint) {
        this.requestSender = requestSender;
        this.endpoint = endpoint;
    }
}
