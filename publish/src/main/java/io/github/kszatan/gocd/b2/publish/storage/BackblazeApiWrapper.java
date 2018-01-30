/*
 * Copyright (c) 2018. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.storage;

import com.thoughtworks.go.plugin.api.logging.Logger;
import io.github.kszatan.gocd.b2.publish.json.GsonService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BackblazeApiWrapper {
    private static final String B2_API_URL = "https://api.backblazeb2.com";
    private static final String AUTHORIZE_ACCOUNT_CMD = "/b2api/v1/b2_authorize_account";
    private static final String LIST_BUCKETS_CMD = "/b2api/v1/b2_list_buckets";
    private static final String GET_UPLOAD_URL_CMD = "/b2api/v1/b2_get_upload_url";

    private Logger logger = Logger.getLoggerFor(BackblazeApiWrapper.class);

    private ErrorResponse lastError;
    private String bucketName;
    private URLStreamHandler urlStreamHandler;

    public BackblazeApiWrapper(String bucketName, URLStreamHandler urlStreamHandler) {
        this.bucketName = bucketName;
        this.urlStreamHandler = urlStreamHandler;
    }

    public Optional<ErrorResponse> getLastError() {
        return Optional.ofNullable(lastError);
    }

    public AuthorizeResponse authorize(String accountId, String applicationKey) throws IOException {
        HttpURLConnection connection = null;
        String headerForAuthorizeAccount = "Basic " +
                Base64.getEncoder().encodeToString((accountId + ":" + applicationKey).getBytes());
        String jsonResponse;
        try {
            URL url = new URL(new URL(B2_API_URL), AUTHORIZE_ACCOUNT_CMD, urlStreamHandler);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", headerForAuthorizeAccount);
            InputStream in = new BufferedInputStream(connection.getInputStream());
            jsonResponse = myInputStreamReader(in);
            logger.info("authorize: " + jsonResponse);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return GsonService.fromJson(jsonResponse, AuthorizeResponse.class);
    }

    public Optional<UploadFileResponse> uploadFile(Path workDir, String filePath, GetUploadUrlResponse getUploadUrlResponse)
            throws NoSuchAlgorithmException, IOException {
    }

    public void download(String filename) {

    }

    public ListBucketsResponse listBuckets(AuthorizeResponse authorizeResponse) throws IOException {
        String apiUrl = authorizeResponse.apiUrl;
        String accountId = authorizeResponse.accountId;
        String accountAuthorizationToken = authorizeResponse.authorizationToken;
        HttpURLConnection connection = null;
        String postParams = "{\"accountId\":\"" + accountId + "\", \"bucketTypes\": [\"allPrivate\",\"allPublic\"]}";
        String jsonResponse;
        byte postData[] = postParams.getBytes(StandardCharsets.UTF_8);
        try {
            URL url = new URL(new URL(apiUrl), LIST_BUCKETS_CMD, urlStreamHandler);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", accountAuthorizationToken);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
            connection.setDoOutput(true);
            DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
            writer.write(postData);
            jsonResponse = myInputStreamReader(connection.getInputStream());
            logger.info("listBuckets: " + jsonResponse);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return GsonService.fromJson(jsonResponse, ListBucketsResponse.class);
    }

    public GetUploadUrlResponse getUploadUrl(AuthorizeResponse authorizeResponse, String bucketId) throws IOException {
        String apiUrl = authorizeResponse.apiUrl;
        String accountAuthorizationToken = authorizeResponse.authorizationToken;
        HttpURLConnection connection = null;
        String postParams = "{\"bucketId\":\"" + bucketId + "\"}";
        String jsonResponse;
        byte postData[] = postParams.getBytes(StandardCharsets.UTF_8);
        try {
            URL url = new URL(new URL(apiUrl), GET_UPLOAD_URL_CMD, urlStreamHandler);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", accountAuthorizationToken);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
            connection.setDoOutput(true);
            DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
            writer.write(postData);
            jsonResponse = myInputStreamReader(connection.getInputStream());
            logger.info("b2_get_upload_url:" + jsonResponse);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return GsonService.fromJson(jsonResponse, GetUploadUrlResponse.class);
    }

    static public String myInputStreamReader(InputStream in) throws IOException {
        InputStreamReader reader = new InputStreamReader(in);
        StringBuilder sb = new StringBuilder();
        int c = reader.read();
        while (c != -1) {
            sb.append((char)c);
            c = reader.read();
        }
        reader.close();
        return sb.toString();
    }
}
