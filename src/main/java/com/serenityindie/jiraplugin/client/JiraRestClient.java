package com.serenityindie.jiraplugin.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

import static com.serenityindie.jiraplugin.processor.JiraCommentGenerator.COMMENT_IDENTIFIER;

public class JiraRestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraRestClient.class);

    public void postCommentToIssue(String jiraServerInstanceUrl, String issueId, String username, String password, String commentBody, String existingCommentId) {
        LOGGER.info("Comment body is: {}", commentBody);

        String authHeaderValue = getAuthHeaderValue(username, password);
        String urlString = "";
        String requestMethod = "";
        if (existingCommentId != null) {
            urlString = jiraServerInstanceUrl + "/rest/api/2/issue/" + issueId + "/comment/" + existingCommentId;
            requestMethod = "PUT";
        } else {
            urlString = jiraServerInstanceUrl + "/rest/api/2/issue/" + issueId + "/comment";
            requestMethod = "POST";
        }
        LOGGER.info("Posting to URL: {}", urlString);

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(requestMethod);
            conn.setRequestProperty("Authorization", authHeaderValue);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = commentBody.getBytes("UTF-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            LOGGER.info("Response Code: {}", responseCode);

            if (responseCode >= 200 && responseCode < 300) {
                LOGGER.info("Comment posted successfully for issue {}", issueId);
            } else {
                LOGGER.error("Failed to post comment for issue {}: {}", issueId, conn.getResponseMessage());
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    LOGGER.error("Error response body: {}", response.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String findExistingCommentId(String jiraServerInstanceUrl, String issueId, String username, String password) {

        String authHeaderValue = getAuthHeaderValue(username, password);

        //ordering number of the comment to start analyzing,
        // if there are more than 50 comments, after the analysis of the first 50 is done, next value should be startAt + 50
        int startAt = 0;
        int maxResults = 50; // This can be adjusted based on JIRA's limits
        boolean isLast = false;

        while (!isLast) {

            try {
                URL url = new URL(jiraServerInstanceUrl + "/rest/api/2/issue/" + issueId + "/comment?startAt=" + startAt + "&maxResults=" + maxResults);
                HttpURLConnection conn = null;
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", authHeaderValue);
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    LOGGER.error("Failed to fetch comments: HTTP error code : {}", conn.getResponseCode());
                }

                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                String output;
                StringBuilder response = new StringBuilder();
                while ((output = br.readLine()) != null) {
                    response.append(output);
                }
                conn.disconnect();

                JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
                JsonArray comments = jsonObject.getAsJsonArray("comments");
                for (JsonElement commentElement : comments) {
                    JsonObject commentObject = commentElement.getAsJsonObject();
                    String commentBody = commentObject.get("body").getAsString();
                    if (commentBody.contains(COMMENT_IDENTIFIER)) {
                        return commentObject.get("id").getAsString();
                    }
                }

                // Update 'startAt' for the next iteration
                startAt += comments.size();

                // Check if this is the last page
                if (startAt >= jsonObject.get("total").getAsInt()) {
                    isLast = true;
                }
            } catch (MalformedURLException e) {
                LOGGER.error("Failed to fetch the comment from JIRA, MalformedURLException catch : {}", e.getMessage());
                LOGGER.error("Stack trace : ", e);
                break;
            } catch (IOException e) {
                LOGGER.error("Failed to fetch the comment from JIRA, IOException catch : {}", e.getMessage());
                LOGGER.error("Stack trace :", e);
                break;
            }
        }
        return null;
    }


    private static String getAuthHeaderValue(String username, String password) {
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        String authHeaderValue = "Basic " + encodedAuth;
        return authHeaderValue;
    }

    private static HttpURLConnection getHttpURLConnection(String commentBody, URL url, String authHeaderValue) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Set the necessary HTTP headers and request method
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", authHeaderValue);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Create the JSON object for the comment
        String jsonInputString = "{\"body\": \"" + commentBody + "\"}";

        // Write the JSON payload to the connection output stream
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        return conn;
    }
}
