/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.storage;

import com.thoughtworks.go.plugin.api.logging.Logger;
import io.github.kszatan.gocd.b2.publish.json.GsonService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BackblazeStorage implements Storage {
    private static final String B2_API_URL = "https://api.backblazeb2.com/b2api/v1/";
    private static final String AUTHORIZE_ACCOUNT_CMD = "b2_authorize_account";

    private Logger logger = Logger.getLoggerFor(BackblazeStorage.class);

    private String errorMessage;
    private String bucketId;
    private AuthorizeResponse authorizeResponse;
    private UploadUrlResponse uploadUrlResponse;
    private URLStreamHandler urlStreamHandler;

    public BackblazeStorage(String bucketId) {
        this.errorMessage = "";
        this.bucketId = bucketId;
    }

    @Override
    public String getLastErrorMessage() {
        return errorMessage;
    }

    @Override
    public Boolean authorize(String accountId, String applicationKey) {
        HttpURLConnection connection = null;
        String headerForAuthorizeAccount = "Basic " +
                Base64.getEncoder().encodeToString((accountId + ":" + applicationKey).getBytes());
        try {
            URL url = new URL(new URL(B2_API_URL), AUTHORIZE_ACCOUNT_CMD, urlStreamHandler);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", headerForAuthorizeAccount);
            InputStream in = new BufferedInputStream(connection.getInputStream());
            String jsonResponse = myInputStreamReader(in);
            authorizeResponse = GsonService.fromJson(jsonResponse, AuthorizeResponse.class);
            logger.debug("authorize: " + jsonResponse);
            uploadUrlResponse = getUploadUrl(authorizeResponse);
        } catch (Exception e) {
            authorizeResponse = null;
            errorMessage = e.getMessage();
            logger.debug("authorize error: " + e.getMessage());
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return true;
    }

    @Override
    public void upload(String filePath, String destination) throws StorageException {

        //throw new StorageException("Unable to upload file " + file.getPath());
    }

    @Override
    public void download(String filename) {
        
    }

    private UploadUrlResponse getUploadUrl(AuthorizeResponse authorizeResponse) throws IOException {
        String apiUrl = authorizeResponse.apiUrl; // Provided by b2_authorize_account
        String accountAuthorizationToken = authorizeResponse.authorizationToken; // Provided by b2_authorize_account
        String bucketId = this.bucketId; // The ID of the bucket you want to upload your file to
        HttpURLConnection connection = null;
        String postParams = "{\"bucketId\":\"" + bucketId + "\"}";
        UploadUrlResponse urlResponse;
        byte postData[] = postParams.getBytes(StandardCharsets.UTF_8);
        try {
            URL url = new URL(new URL(apiUrl), "/b2api/v1/b2_get_upload_url", urlStreamHandler);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", accountAuthorizationToken);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
            connection.setDoOutput(true);
            DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
            writer.write(postData);
            String jsonResponse = myInputStreamReader(connection.getInputStream());
            logger.debug("b2_get_upload_url:" + jsonResponse);
            urlResponse = GsonService.fromJson(jsonResponse, UploadUrlResponse.class);
        } finally {
            connection.disconnect();
        }
        return urlResponse;
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
