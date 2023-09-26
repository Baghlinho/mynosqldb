package com.capstone.driver.client;

import com.capstone.driver.core.JsonBody;
import com.capstone.driver.core.RequestSender;
import com.capstone.driver.core.Role;
import com.capstone.driver.operation.AuthOperations;

import java.io.FileWriter;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Arrays;

public class AuthClient extends RestClient implements AuthOperations {

    public AuthClient(RequestSender requestSender) {
        super(requestSender, "/auth/%s");
    }

    @Override
    public boolean login(String username, String password) {
        if(username == null || username.isEmpty())
            throw new IllegalArgumentException("Username must be provided");
        if(password == null || password.isEmpty())
            throw new IllegalArgumentException("Password must be provided");
        HttpResponse<String> response = requestSender.sendPost(
                endpoint.formatted("login"),
                JsonBody.create().username(username).password(password).toJson()
        );
        return response.body().startsWith("{");
    }

    @Override
    public boolean register(String username, String password, Role role) throws InterruptedException {
        if(username == null || username.isEmpty())
            throw new IllegalArgumentException("Username must be provided");
        if(password == null || password.isEmpty())
            throw new IllegalArgumentException("Password must be provided");
        if(role == null)
            throw new IllegalArgumentException("Role must be provided");
        HttpResponse<String> response = requestSender.sendPost(
                endpoint.formatted("register"),
                JsonBody.create().username(username).password(password).role(role).toJson()
        );
        String[] split = response.body().split("_");
        if(split.length != 2) return false;
        int nodeId = Integer.parseInt(split[1]);
        requestSender.setNodeId(nodeId);
        try(FileWriter wr = new FileWriter("node.txt")){
            wr.write(""+nodeId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Thread.sleep(100);
        return true;
    }

    @Override
    public boolean logout() {
        HttpResponse<String> response = requestSender.sendPost(
                endpoint.formatted("logout"),
                JsonBody.create().toJson()
        );
        return response.statusCode() == 200;
    }
}
