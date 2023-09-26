package com.capstone.driver.core;

import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Scanner;

public class RequestSender {
    private int nodeId;
    private final HttpClient httpClient;
    private static final String requestURIFormat = "http://localhost:800%d%s";

    public RequestSender() {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        httpClient = HttpClient.newBuilder()
                .cookieHandler(CookieHandler.getDefault())
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        File node = new File("node.txt");
        if(node.exists()) {
            try(Scanner scanner = new Scanner(node)) {
                nodeId = scanner.nextInt();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public HttpResponse<String> sendGet(String endpoint, String requestBody){
        return sendRequest("GET", endpoint, requestBody);
    }

    public HttpResponse<String> sendPost(String endpoint, String requestBody) {
        return sendRequest("POST", endpoint, requestBody);
    }

    private HttpResponse<String> sendRequest(String method, String endpoint, String requestBody) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestURIFormat.formatted(nodeId, endpoint)))
                .method(method, HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Accept", "application/json")
                .headers("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(5))
                .build();
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
