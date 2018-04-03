/*
 * Copyright (c) 2018. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage.api;

import io.github.kszatan.gocd.b2.utils.json.GsonService;
import io.github.kszatan.gocd.b2.utils.storage.*;
import org.apache.http.HttpStatus;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;
import java.util.Properties;

public class BackblazeApiWrapper {
    private static final String B2_API_URL = "https://api.backblazeb2.com";
    private static final String AUTHORIZE_ACCOUNT_CMD = "/b2api/v1/b2_authorize_account";
    private static final String GET_UPLOAD_URL_CMD = "/b2api/v1/b2_get_upload_url";
    private static final String LIST_BUCKETS_CMD = "/b2api/v1/b2_list_buckets";
    private static final String LIST_FILE_NAMES_CMD = "/b2api/v1/b2_list_file_names";
    private static final Integer CONNECTION_TIMEOUT_MS = 60 * 1000;
    private static final Integer READ_TIMEOUT_MS = 120 * 1000;

    private ErrorResponse lastError;
    private URLStreamHandler urlStreamHandler;
    private FileHash fileHash;
    private Properties properties;

    public BackblazeApiWrapper() throws IOException {
        this.urlStreamHandler = null;
        this.fileHash = new Sha1FileHash();
        this.properties = new Properties();
        InputStream in = this.getClass().getResourceAsStream("/version.properties");
        properties.load(in);
        in.close();
    }

    public BackblazeApiWrapper(URLStreamHandler urlStreamHandler) {
        this(urlStreamHandler, new Sha1FileHash());
    }

    public BackblazeApiWrapper(URLStreamHandler urlStreamHandler, FileHash fileHash) {
        this.urlStreamHandler = urlStreamHandler;
        this.fileHash = fileHash;
        properties = new Properties();
        properties.setProperty("version", "0.314");
    }

    public Optional<ErrorResponse> getLastError() {
        return Optional.ofNullable(lastError);
    }

    public Optional<AuthorizeResponse> authorize(String accountId, String applicationKey) throws IOException {
        HttpURLConnection connection = null;
        String headerForAuthorizeAccount = "Basic " +
                Base64.getEncoder().encodeToString((accountId + ":" + applicationKey).getBytes());
        String jsonResponse = "";
        try {
            connection = newHttpConnection(B2_API_URL, AUTHORIZE_ACCOUNT_CMD, "GET");
            connection.setRequestProperty("Authorization", headerForAuthorizeAccount);
            if (connection.getResponseCode() == HttpStatus.SC_OK) {
                jsonResponse = myStreamReader(connection.getInputStream());
            } else {
                parseErrorResponse(connection);
                return Optional.empty();
            }
        } catch (SocketTimeoutException e) {
            setRequestTimeoutError(e);
            return Optional.empty();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return Optional.of(GsonService.fromJson(jsonResponse, AuthorizeResponse.class));
    }

    public Optional<UploadFileResponse> uploadFile(Path workDir, String filePath, String destination, GetUploadUrlResponse getUploadUrlResponse)
            throws NoSuchAlgorithmException, IOException {
        Path absoluteFilePath = workDir.resolve(filePath);
        String content_sha1 = fileHash.getHashValue(absoluteFilePath);
        HttpURLConnection connection = null;
        String jsonResponse = "";
        destination = destination == null ? "" : destination;
        try {
            connection = newHttpConnection(getUploadUrlResponse.uploadUrl, "", "POST");
            connection.setRequestProperty("Authorization", getUploadUrlResponse.authorizationToken);
            connection.setRequestProperty("Content-Type", "b2/x-auto");
            connection.setRequestProperty("X-Bz-File-Name", Paths.get(destination, filePath).toString());
            connection.setRequestProperty("X-Bz-Content-Sha1", content_sha1);
            BasicFileAttributes attrs = Files.readAttributes(absoluteFilePath, BasicFileAttributes.class);
            connection.setRequestProperty("X-Bz-Info-src_last_modified_millis",
                    String.valueOf(attrs.lastModifiedTime().toMillis()));
            connection.setDoOutput(true);
            DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
            Files.copy(absoluteFilePath, writer);
            if (connection.getResponseCode() == HttpStatus.SC_OK) {
                jsonResponse = myStreamReader(connection.getInputStream());
            } else {
                parseErrorResponse(connection);
                return Optional.empty();
            }
        } catch (SocketTimeoutException e) {
            setRequestTimeoutError(e);
            return Optional.empty();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return Optional.of(GsonService.fromJson(jsonResponse, UploadFileResponse.class));
    }

    public void download(String filename) {

    }

    public Optional<ListBucketsResponse> listBuckets(AuthorizeResponse authorizeResponse) throws IOException {
        String apiUrl = authorizeResponse.apiUrl;
        String accountId = authorizeResponse.accountId;
        String accountAuthorizationToken = authorizeResponse.authorizationToken;
        HttpURLConnection connection = null;
        String postParams = "{\"accountId\":\"" + accountId + "\", \"bucketTypes\": [\"allPrivate\",\"allPublic\"]}";
        String jsonResponse = "";
        byte postData[] = postParams.getBytes(StandardCharsets.UTF_8);
        try {
            connection = newHttpConnection(apiUrl, LIST_BUCKETS_CMD, "POST");
            connection.setRequestProperty("Authorization", accountAuthorizationToken);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
            connection.setDoOutput(true);
            DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
            writer.write(postData);
            if (connection.getResponseCode() == HttpStatus.SC_OK) {
                jsonResponse = myStreamReader(connection.getInputStream());
            } else {
                parseErrorResponse(connection);
                return Optional.empty();
            }
        } catch (SocketTimeoutException e) {
            setRequestTimeoutError(e);
            return Optional.empty();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return Optional.of(GsonService.fromJson(jsonResponse, ListBucketsResponse.class));
    }

    public Optional<GetUploadUrlResponse> getUploadUrl(AuthorizeResponse authorizeResponse, String bucketId) throws IOException {
        String apiUrl = authorizeResponse.apiUrl;
        String accountAuthorizationToken = authorizeResponse.authorizationToken;
        HttpURLConnection connection = null;
        String postParams = "{\"bucketId\":\"" + bucketId + "\"}";
        String jsonResponse = "";
        byte postData[] = postParams.getBytes(StandardCharsets.UTF_8);
        try {
            connection = newHttpConnection(apiUrl, GET_UPLOAD_URL_CMD, "POST");
            connection.setRequestProperty("Authorization", accountAuthorizationToken);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
            connection.setDoOutput(true);
            DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
            writer.write(postData);
            if (connection.getResponseCode() == HttpStatus.SC_OK) {
                jsonResponse = myStreamReader(connection.getInputStream());
            } else {
                parseErrorResponse(connection);
                return Optional.empty();
            }
        } catch (SocketTimeoutException e) {
            setRequestTimeoutError(e);
            return Optional.empty();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return Optional.of(GsonService.fromJson(jsonResponse, GetUploadUrlResponse.class));
    }

    public Optional<ListFileNamesResponse> listFileNames(AuthorizeResponse authorizeResponse, String bucketId) throws IOException {
        return listFileNames(authorizeResponse, bucketId, "", "", "");
    }

    public Optional<ListFileNamesResponse> listFileNames(AuthorizeResponse authorizeResponse, String bucketId,
                                                         String startFileName, String prefix, String delimiter) throws IOException {
        String apiUrl = authorizeResponse.apiUrl;
        String accountAuthorizationToken = authorizeResponse.authorizationToken;
        HttpURLConnection connection = null;
        String postParams = "{\"bucketId\":\"" + bucketId + "\", " +
                "\"startFileName\":\"" + startFileName + "\"," +
                "\"prefix\":\"" + prefix + "\"," +
                "\"delimiter\":\"" + delimiter + "\"," +
                "\"maxFileCount\": 1000}";
        String jsonResponse = "";
        byte postData[] = postParams.getBytes(StandardCharsets.UTF_8);
        try {
            connection = newHttpConnection(apiUrl, LIST_FILE_NAMES_CMD, "POST");
            connection.setRequestProperty("Authorization", accountAuthorizationToken);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
            connection.setDoOutput(true);
            DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
            writer.write(postData);
            if (connection.getResponseCode() == HttpStatus.SC_OK) {
                jsonResponse = myStreamReader(connection.getInputStream());
            } else {
                parseErrorResponse(connection);
                return Optional.empty();
            }
        } catch (SocketTimeoutException e) {
            setRequestTimeoutError(e);
            return Optional.empty();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return Optional.of(GsonService.fromJson(jsonResponse, ListFileNamesResponse.class));
    }

    static private String myStreamReader(InputStream in) throws IOException {
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

    private HttpURLConnection newHttpConnection(String urlContext, String urlSpec, String requestMethod) throws IOException {
        URL url = new URL(new URL(urlContext), urlSpec, urlStreamHandler);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(requestMethod);
        connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setRequestProperty("User-Agent", userAgentVersion());
//        connection.setRequestProperty("X-Bz-Test-Mode", "fail_some_uploads");
//        connection.setRequestProperty("X-Bz-Test-Mode", "expire_some_account_authorization_tokens");
//        connection.setRequestProperty("X-Bz-Test-Mode", "force_cap_exceeded");
        return connection;
    }

    private void setRequestTimeoutError(SocketTimeoutException e) {
        lastError = new ErrorResponse();
        lastError.status = HttpStatus.SC_REQUEST_TIMEOUT;
        lastError.message = e.getMessage();
        lastError.code = "request_timeout";
    }

    private void parseErrorResponse(HttpURLConnection connection) throws IOException {
        String responseBody;
        try {
            responseBody = myStreamReader(connection.getErrorStream());
            lastError = GsonService.fromJson(responseBody, ErrorResponse.class);
            if (connection.getResponseCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
                String retryAfter = connection.getHeaderField("Retry-After");
                if (retryAfter != null) {
                    lastError.retryAfter = Integer.parseInt(retryAfter);
                }
            }
        } catch (IOException e) {
            lastError = new ErrorResponse();
            lastError.status = connection.getResponseCode();
            lastError.message = "Error while reading error response body";
            lastError.code = "unknown";
        }
    }

    private String userAgentVersion() {
        return "gocd-b2-artifacts-" + properties.getProperty("version");
    }
}
