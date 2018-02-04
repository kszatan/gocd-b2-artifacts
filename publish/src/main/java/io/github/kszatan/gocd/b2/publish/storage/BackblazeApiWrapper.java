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
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

public class BackblazeApiWrapper {
    private static final String B2_API_URL = "https://api.backblazeb2.com";
    private static final String AUTHORIZE_ACCOUNT_CMD = "/b2api/v1/b2_authorize_account";
    private static final String LIST_BUCKETS_CMD = "/b2api/v1/b2_list_buckets";
    private static final String GET_UPLOAD_URL_CMD = "/b2api/v1/b2_get_upload_url";
    private static final Integer CONNECTION_TIMEOUT = 5000;

    private Logger logger = Logger.getLoggerFor(BackblazeApiWrapper.class);

    private ErrorResponse lastError;
    private URLStreamHandler urlStreamHandler;
    private FileHash fileHash;

    public BackblazeApiWrapper() {
        this.urlStreamHandler = null;
        this.fileHash = new Sha1FileHash();
    }

    public BackblazeApiWrapper(URLStreamHandler urlStreamHandler) {
        this.urlStreamHandler = urlStreamHandler;
        this.fileHash = new Sha1FileHash();
    }

    public BackblazeApiWrapper(URLStreamHandler urlStreamHandler, FileHash fileHash) {
        this.urlStreamHandler = urlStreamHandler;
        this.fileHash = fileHash;
    }

    public Optional<ErrorResponse> getLastError() {
        return Optional.ofNullable(lastError);
    }

    public AuthorizeResponse authorize(String accountId, String applicationKey) throws IOException {
        HttpURLConnection connection = null;
        String headerForAuthorizeAccount = "Basic " +
                Base64.getEncoder().encodeToString((accountId + ":" + applicationKey).getBytes());
        String jsonResponse = "";
        try {
            URL url = new URL(new URL(B2_API_URL), AUTHORIZE_ACCOUNT_CMD, urlStreamHandler);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setRequestProperty("Authorization", headerForAuthorizeAccount);
            InputStream in = new BufferedInputStream(connection.getInputStream());
            jsonResponse = myStreamReader(in);
            logger.info("authorize: " + jsonResponse);
        } catch (SocketTimeoutException e) {
            logger.info("Connection timeout");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return GsonService.fromJson(jsonResponse, AuthorizeResponse.class);
    }

    public Optional<UploadFileResponse> uploadFile(Path workDir, String filePath, GetUploadUrlResponse getUploadUrlResponse)
            throws NoSuchAlgorithmException, IOException {
        Path absoluteFilePath = workDir.resolve(filePath);
        String content_sha1 = fileHash.getHashValue(absoluteFilePath);
        HttpURLConnection connection = null;
        String jsonResponse;
        try {
            URL url = new URL(new URL(getUploadUrlResponse.uploadUrl), "", urlStreamHandler);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setRequestProperty("Authorization", getUploadUrlResponse.authorizationToken);
            connection.setRequestProperty("Content-Type", "b2/x-auto");
            connection.setRequestProperty("X-Bz-File-Name", filePath);
            connection.setRequestProperty("X-Bz-Content-Sha1", content_sha1);
            connection.setDoOutput(true);
            DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
            Files.copy(absoluteFilePath, writer);
            if (connection.getResponseCode() == 200) {
                jsonResponse = myStreamReader(connection.getInputStream());
                logger.info("uploadFile: " + jsonResponse);
                return Optional.of(GsonService.fromJson(jsonResponse, UploadFileResponse.class));
            } else {
                jsonResponse = myStreamReader(connection.getErrorStream());
                lastError = GsonService.fromJson(jsonResponse, ErrorResponse.class);
                logger.info("uploadFile: " + jsonResponse);
            }
        } catch (SocketTimeoutException e) {
            logger.info("Connection timeout");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return Optional.empty();
    }

    public void download(String filename) {

    }

    public ListBucketsResponse listBuckets(AuthorizeResponse authorizeResponse) throws IOException {
        String apiUrl = authorizeResponse.apiUrl;
        String accountId = authorizeResponse.accountId;
        String accountAuthorizationToken = authorizeResponse.authorizationToken;
        HttpURLConnection connection = null;
        String postParams = "{\"accountId\":\"" + accountId + "\", \"bucketTypes\": [\"allPrivate\",\"allPublic\"]}";
        String jsonResponse = "";
        byte postData[] = postParams.getBytes(StandardCharsets.UTF_8);
        try {
            URL url = new URL(new URL(apiUrl), LIST_BUCKETS_CMD, urlStreamHandler);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setRequestProperty("Authorization", accountAuthorizationToken);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
            connection.setDoOutput(true);
            DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
            writer.write(postData);
            jsonResponse = myStreamReader(connection.getInputStream());
            logger.info("listBuckets: " + jsonResponse);
        } catch (SocketTimeoutException e) {
            logger.info("Connection timeout");
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
        String jsonResponse = "";
        byte postData[] = postParams.getBytes(StandardCharsets.UTF_8);
        try {
            URL url = new URL(new URL(apiUrl), GET_UPLOAD_URL_CMD, urlStreamHandler);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setRequestProperty("Authorization", accountAuthorizationToken);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
            connection.setDoOutput(true);
            DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
            writer.write(postData);
            jsonResponse = myStreamReader(connection.getInputStream());
            logger.info("b2_get_upload_url:" + jsonResponse);
        } catch (SocketTimeoutException e) {
            logger.info("Connection timeout");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return GsonService.fromJson(jsonResponse, GetUploadUrlResponse.class);
    }

    static public String myStreamReader(InputStream in) throws IOException {
        InputStreamReader reader = new InputStreamReader(in);
        StringBuilder sb = new StringBuilder();
        int c = reader.read();
        while (c != -1) {
            sb.append((char) c);
            c = reader.read();
        }
        reader.close();
        return sb.toString();
    }
}
