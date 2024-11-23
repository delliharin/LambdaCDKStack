package com.socgen;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;

/**
 * Hello world!
 */
public class HelloWorldLambda implements RequestHandler<Map<String, Object>, String> {

    @Override
    public String handleRequest(final Map<String, Object> s3Event, final Context context) {
        try {
            context.getLogger().log("Object Bucket");
            context.getLogger().log("Object Bucket" + s3Event.toString());
            context.getLogger().log("s3EventNotificationRecord ");
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> detail = (Map<String, Object>) s3Event.get("detail");
            if (detail != null) {
                Map<String, Object> requestParameters = (Map<String, Object>) detail.get("object");
                if (requestParameters != null) {
                    String bucketName = (String) ((Map<String, Object>) detail.get("bucket")).get("name");
                    String objectKey = (String) ((Map<String, Object>) detail.get("object")).get("key");
                    context.getLogger().log("Bucket Name :" + bucketName);
                    context.getLogger().log("Object Key :" + objectKey);
                    Base64.Encoder encoder = Base64.getEncoder();
                    String originalString = "javainuse-client:javainuse-secret";
                    String encodedString = encoder.encodeToString(originalString.getBytes());
                    String headerAthorization = "Basic " + encodedString;
                    HttpRequest request = HttpRequest.newBuilder(new URI("https://446d533a60f2b8bfa556f12cce473108.serveo.net/oauth/token"))
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .header("Authorization", headerAthorization)
                            .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                            .build();
                    HttpClient httpClient = HttpClient.newHttpClient();
                    HttpResponse<String> postResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    JsonNode node = mapper.readTree(postResponse.body());
                    String token = node.get("access_token").asText();
                    context.getLogger().log("Token :" + token);
                    HttpRequest httpRequest = HttpRequest.newBuilder()
                            .uri(new URI("https://f872a2530416b977189756e36335713c.serveo.net/test/d10"))
                            .headers("Content-Type", "application/json", "Authorization", "Bearer " + token)
                            .GET()
                            .build();
                    HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                    context.getLogger().log("Object Bucket :" + bucketName + "Object Key :" + objectKey);
                    context.getLogger().log("Response : " + response.body());
                    return response.body();
                }
            }

        } catch (final Exception exception) {
            context.getLogger().log("Exception : "+ exception.getMessage() + " " +exception.getStackTrace().toString());
            return exception.getMessage();
        }
        return "Processed successfully";
    }
}
