/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage;

import io.github.kszatan.gocd.b2.utils.json.GsonService;
import io.github.kszatan.gocd.b2.utils.storage.api.BackblazeApiWrapper;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class BackblazeApiWrapperTest {
    private BackblazeApiWrapper wrapper;
    private URLStreamHandler stubUrlHandler;
    private HttpURLConnection mockUrlCon;
    private AuthorizeResponse defAuthResponse;
    private FileHash mockFileHash;

    private final String authorizeResponseJson = "{\n" +
            "  \"absoluteMinimumPartSize\": 5000000,\n" +
            "  \"accountId\": \"aaaabbbbcccc\",\n" +
            "  \"apiUrl\": \"https://api001.backblazeb2.com\",\n" +
            "  \"authorizationToken\": \"token_fristajlo\",\n" +
            "  \"downloadUrl\": \"https://f001.backblazeb2.com\",\n" +
            "  \"minimumPartSize\": 100000000,\n" +
            "  \"recommendedPartSize\": 100000000\n" +
            "}";
    private final String getUploadUrlResponseJson = "{\n" +
            "  \"bucketId\" : \"4a48fe8875c6214145260818\",\n" +
            "  \"uploadUrl\" : \"https://pod-000-1005-03.backblaze.com/b2api/v1/b2_upload_file?cvt=c001_v0001005_t0027&bucket=4a48fe8875c6214145260818\",\n" +
            "  \"authorizationToken\" : \"2_20151009170037_f504a0f39a0f4e657337e624_9754dde94359bd7b8f1445c8f4cc1a231a33f714_upld\"\n" +
            "}";
    private final String getUploadPartUrlResponseJson = "{\n" +
            "  \"authorizationToken\": \"3_20160409004829_42b8f80ba60fb4323dcaad98_ec81302316fccc2260201cbf17813247f312cf3b_000_uplg\",\n" +
            "  \"fileId\": \"4_ze73ede9c9c8412db49f60715_f100b4e93fbae6252_d20150824_m224353_c900_v8881000_t0001\",\n" +
            "  \"uploadUrl\": \"https://pod-000-1016-09.backblaze.com/b2api/v1/b2_upload_part/4_ze73ede9c9c8412db49f60715_f100b4e93fbae6252_d20150824_m224353_c900_v8881000_t0001/0037\"\n" +
            "}";
    private final String listBucketsResponseJson = "{\n" +
            "    \"buckets\": [\n" +
            "    {\n" +
            "        \"accountId\": \"30f20426f0b1\",\n" +
            "        \"bucketId\": \"4a48fe8875c6214145260818\",\n" +
            "        \"bucketInfo\": {},\n" +
            "        \"bucketName\" : \"Kitten-Videos\",\n" +
            "        \"corsRules\": [],\n" +
            "        \"bucketType\": \"allPrivate\",\n" +
            "        \"lifecycleRules\": [],\n" +
            "        \"revision\": 1\n" +
            "    },\n" +
            "    {\n" +
            "        \"accountId\": \"30f20426f0b1\",\n" +
            "        \"bucketId\" : \"5b232e8875c6214145260818\",\n" +
            "        \"bucketInfo\": {},\n" +
            "        \"bucketName\": \"Puppy-Videos\",\n" +
            "        \"corsRules\": [],\n" +
            "        \"bucketType\": \"allPublic\",\n" +
            "        \"lifecycleRules\": [],\n" +
            "        \"revision\": 1\n" +
            "    },\n" +
            "    {\n" +
            "        \"accountId\": \"30f20426f0b1\",\n" +
            "        \"bucketId\": \"87ba238875c6214145260818\",\n" +
            "        \"bucketInfo\": {},\n" +
            "        \"bucketName\": \"Vacation-Pictures\",\n" +
            "        \"corsRules\": [],\n" +
            "        \"bucketType\" : \"allPrivate\",\n" +
            "        \"lifecycleRules\": [],\n" +
            "        \"revision\": 1\n" +
            "    } ]\n" +
            "}";
    private final String uploadFileResponseJson = "{\n" +
            "    \"fileId\" : \"4_h4a48fe8875c6214145260818_f000000000000472a_d20140104_m032022_c001_v0000123_t0104\",\n" +
            "    \"fileName\" : \"typing_test.txt\",\n" +
            "    \"accountId\" : \"d522aa47a10f\",\n" +
            "    \"bucketId\" : \"4a48fe8875c6214145260818\",\n" +
            "    \"contentLength\" : 46,\n" +
            "    \"contentSha1\" : \"bae5ed658ab3546aee12f23f36392f35dba1ebdd\",\n" +
            "    \"contentType\" : \"text/plain\",\n" +
            "    \"fileInfo\" : {\n" +
            "       \"author\" : \"unknown\"\n" +
            "    }\n" +
            "}";
    private final String uploadPartResponseJson = "{\n" +
            "  \"contentLength\": 100000000,\n" +
            "  \"contentSha1\": \"062685a84ab248d2488f02f6b01b948de2514ad8\",\n" +
            "  \"fileId\": \"4_ze73ede9c9c8412db49f60715_f100b4e93fbae6252_d20150824_m224353_c900_v8881000_t0001\",\n" +
            "  \"partNumber\": 1\n" +
            "}";
    private final String listFileNamesResponseJson = "{\n" +
            "  \"files\": [\n" +
            "    {\n" +
            "      \"action\": \"upload\",\n" +
            "      \"contentLength\": 6,\n" +
            "      \"fileId\": \"4_z27c88f1d182b150646ff0b16_f1004ba650fe24e6b_d20150809_m012853_c100_v0009990_t0000\",\n" +
            "      \"fileName\": \"files/hello.txt\",\n" +
            "      \"size\": 6,\n" +
            "      \"uploadTimestamp\": 1439083733000\n" +
            "    },\n" +
            "    {\n" +
            "      \"action\": \"upload\",\n" +
            "      \"contentLength\": 6,\n" +
            "      \"fileId\": \"4_z27c88f1d182b150646ff0b16_f1004ba650fe24e6c_d20150809_m012854_c100_v0009990_t0000\",\n" +
            "      \"fileName\": \"files/world.txt\",\n" +
            "      \"size\": 6,\n" +
            "      \"uploadTimestamp\": 1439083734000\n" +
            "    }\n" +
            "  ],\n" +
            "  \"nextFileName\": null\n" +
            "}";
    private final String startLargeFileResponseJson = "{\n"+
            "  \"accountId\": \"d522aa47a10f\",\n"+
            "  \"bucketId\": \"e73ede9c9c8412db49f60715\",\n"+
            "  \"contentType\": \"b2/x-auto\",\n"+
            "  \"fileId\": \"4_za71f544e781e6891531b001a_f200ec353a2184825_d20160409_m004829_c000_v0001016_t0028\",\n"+
            "  \"fileInfo\": {},\n"+
            "  \"fileName\": \"bigfile.dat\",\n"+
            "  \"uploadTimestamp\": 1460162909000\n"+
            "}";
    private final String finishLargeFileResponseJson = "{\n" +
            "  \"accountId\": \"d522aa47a10f\",\n" +
            "  \"action\": \"upload\",\n" +
            "  \"bucketId\": \"e73ede9c9c8412db49f60715\",\n" +
            "  \"contentLength\": 208158542,\n" +
            "  \"contentSha1\": \"none\",\n" +
            "  \"contentType\": \"b2/x-auto\",\n" +
            "  \"fileId\": \"4_ze73ede9c9c8412db49f60715_f100b4e93fbae6252_d20150824_m224353_c900_v8881000_t0001\",\n" +
            "  \"fileInfo\": {},\n" +
            "  \"fileName\": \"bigfile.dat\",\n" +
            "  \"uploadTimestamp\": 1460162909000\n" +
            "}";
    private final String cancelLargeFileResponseJson = "{\n" +
            "  \"accountId\": \"d522aa47a10f\",\n" +
            "  \"bucketId\": \"4a48fe8875c6214145260818\",\n" +
            "  \"fileId\": \"4_za71f544e781e6891531b001a_f200ec353a2184825_d20160409_m004829_c000_v0001016_t0028\",\n" +
            "  \"fileName\": \"bigfile.dat\"\n" +
            "}";
    private String errorResponseJson = "{\n" +
            "    \"status\" : 400,\n" +
            "    \"code\" : \"invalid_bucket_name\",\n" +
            "    \"message\" : \"bucket name is too long\"\n" +
            "}";

    @Before
    public void setUp() throws Exception {
        mockUrlCon = mock(HttpURLConnection.class);
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                return mockUrlCon;
            }
        };
        mockFileHash = mock(FileHash.class);
        wrapper = new BackblazeApiWrapper(stubUrlHandler, mockFileHash);

        defAuthResponse = new AuthorizeResponse();
        defAuthResponse.absoluteMinimumPartSize = 5000000;
        defAuthResponse.accountId = "aaaabbbbcccc";
        defAuthResponse.apiUrl = "https://api001.backblazeb2.com";
        defAuthResponse.authorizationToken = "token_fristajlo";
        defAuthResponse.downloadUrl = "https://f001.backblazeb2.com";
        defAuthResponse.recommendedPartSize = 100000000;
    }

    @Test
    public void successfulAuthorizeCallShouldResultInCorrectlyPopulatedResponseFields() throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(authorizeResponseJson.getBytes("UTF-8"));
        doReturn(is).when(mockUrlCon).getInputStream();
        doReturn(HttpStatus.SC_OK).when(mockUrlCon).getResponseCode();

        String accountId = "account_id";
        String applicationKey = "application_key";
        AuthorizeResponse authorizeResponse = wrapper.authorize(accountId, applicationKey).orElseThrow(
                () -> new RuntimeException("Null authorizeResponse"));
        verify(mockUrlCon).disconnect();
        assertThat(authorizeResponse.absoluteMinimumPartSize, equalTo(5000000));
        assertThat(authorizeResponse.accountId, equalTo("aaaabbbbcccc"));
        assertThat(authorizeResponse.apiUrl, equalTo("https://api001.backblazeb2.com"));
        assertThat(authorizeResponse.authorizationToken, equalTo("token_fristajlo"));
        assertThat(authorizeResponse.downloadUrl, equalTo("https://f001.backblazeb2.com"));
        assertThat(authorizeResponse.recommendedPartSize, equalTo(100000000));
    }

    @Test
    public void exceptionDuringOpeningConnectionOnAuthorizeShouldCloseConnection() throws Exception {
        doThrow(new IOException("Bad")).when(mockUrlCon).getInputStream();

        String accountId = "account_id";
        String applicationKey = "application_key";
        try {
            wrapper.authorize(accountId, applicationKey);
        } catch (Exception e) {
        }
        verify(mockUrlCon).disconnect();
    }

    @Test
    public void exceptionDuringOpeningConnectionOnAuthorizeShouldNotResultInAttemptToCloseConnection() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new IOException("Bad, bad connection");
            }
        };

        wrapper = new BackblazeApiWrapper(stubUrlHandler);

        String accountId = "account_id";
        String applicationKey = "application_key";
        try {
            wrapper.authorize(accountId, applicationKey);
        } catch (Exception e) {
        }
        verify(mockUrlCon, times(0)).disconnect();
    }

    @Test
    public void requestTimeoutDuringConnectionOnAuthorizeShouldResultInCorrectlySetErrorAndEmptyReturn() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new SocketTimeoutException("Request timeout");
            }
        };

        wrapper = new BackblazeApiWrapper(stubUrlHandler);

        String accountId = "account_id";
        String applicationKey = "application_key";
        Optional<AuthorizeResponse> response = wrapper.authorize(accountId, applicationKey);
        assertThat(response, equalTo(Optional.empty()));
        ErrorResponse error = wrapper.getLastError().get();
        assertThat(error.status, equalTo(HttpStatus.SC_REQUEST_TIMEOUT));
        assertThat(error.message, equalTo("Request timeout"));
        assertThat(error.code, equalTo("request_timeout"));
        verify(mockUrlCon, times(0)).disconnect();
    }

    @Test
    public void exceptionDuringOpeningConnectionOnListBucketsShouldCloseConnection() throws Exception {
        doThrow(new IOException("Bad")).when(mockUrlCon).getInputStream();

        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                return mockUrlCon;
            }
        };

        wrapper = new BackblazeApiWrapper(stubUrlHandler);

        try {
            wrapper.listBuckets(GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class));
        } catch (Exception e) {
        }
        verify(mockUrlCon).disconnect();
    }

    @Test
    public void exceptionDuringOpeningConnectionOnListBucketsShouldNotResultInAttemptToCloseConnection() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new IOException("Bad, bad connection");
            }
        };

        wrapper = new BackblazeApiWrapper(stubUrlHandler);

        try {
            wrapper.listBuckets(GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class));
        } catch (Exception e) {
        }
        verify(mockUrlCon, times(0)).disconnect();
    }

    @Test
    public void requestTimeoutDuringConnectionOnListBucketsShouldResultInCorrectlySetErrorAndEmptyReturn() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new SocketTimeoutException("Request timeout");
            }
        };

        wrapper = new BackblazeApiWrapper(stubUrlHandler);

        AuthorizeResponse authorizeResponse = GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class);
        Optional<ListBucketsResponse> response = wrapper.listBuckets(authorizeResponse);
        assertThat(response, equalTo(Optional.empty()));
        ErrorResponse error = wrapper.getLastError().get();
        assertThat(error.status, equalTo(HttpStatus.SC_REQUEST_TIMEOUT));
        assertThat(error.message, equalTo("Request timeout"));
        assertThat(error.code, equalTo("request_timeout"));
        verify(mockUrlCon, times(0)).disconnect();
    }

    @Test
    public void successfulListBucketsCallShouldResultInCorrectlyPopulatedResponseFields() throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(listBucketsResponseJson.getBytes("UTF-8"));
        doReturn(is).when(mockUrlCon).getInputStream();
        doReturn(HttpStatus.SC_OK).when(mockUrlCon).getResponseCode();
        doReturn(mock(OutputStream.class)).when(mockUrlCon).getOutputStream();

        ListBucketsResponse response = wrapper.listBuckets(defAuthResponse).get();
        verify(mockUrlCon).disconnect();
        assertThat(response.buckets.size(), equalTo(3));
        Bucket bucket = response.buckets.get(0);
        assertThat(bucket.accountId, equalTo("30f20426f0b1"));
        assertThat(bucket.id, equalTo("4a48fe8875c6214145260818"));
        assertThat(bucket.name, equalTo("Kitten-Videos"));
        assertThat(bucket.type, equalTo("allPrivate"));
        bucket = response.buckets.get(1);
        assertThat(bucket.accountId, equalTo("30f20426f0b1"));
        assertThat(bucket.id, equalTo("5b232e8875c6214145260818"));
        assertThat(bucket.name, equalTo("Puppy-Videos"));
        assertThat(bucket.type, equalTo("allPublic"));
        bucket = response.buckets.get(2);
        assertThat(bucket.accountId, equalTo("30f20426f0b1"));
        assertThat(bucket.id, equalTo("87ba238875c6214145260818"));
        assertThat(bucket.name, equalTo("Vacation-Pictures"));
        assertThat(bucket.type, equalTo("allPrivate"));
    }

    @Test
    public void exceptionDuringOpeningConnectionOnUploadFileShouldCloseConnection() throws Exception {
        doThrow(new IOException("Bad")).when(mockUrlCon).getInputStream();

        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                return mockUrlCon;
            }
        };
        doReturn("hash").when(mockFileHash).getHashValue(any());

        wrapper = new BackblazeApiWrapper(stubUrlHandler, mockFileHash);

        GetUploadUrlResponse uploadUrlResponse = GsonService.fromJson(getUploadUrlResponseJson, GetUploadUrlResponse.class);
        try {
            wrapper.uploadFile(Paths.get(""), "file", null, uploadUrlResponse);
        } catch (Exception e) {
        }
        verify(mockUrlCon).disconnect();
    }

    @Test
    public void exceptionDuringOpeningConnectionOnUploadFileShouldNotResultInAttemptToCloseConnection() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new IOException("Bad, bad connection");
            }
        };

        wrapper = new BackblazeApiWrapper(stubUrlHandler, mockFileHash);

        GetUploadUrlResponse uploadUrlResponse = GsonService.fromJson(getUploadUrlResponseJson, GetUploadUrlResponse.class);
        try {
            wrapper.uploadFile(Paths.get(""), "file", "", uploadUrlResponse);
        } catch (Exception e) {
        }
        verify(mockUrlCon, times(0)).disconnect();
    }

    @Test
    public void requestTimeoutDuringConnectionOnUploadFileShouldResultInCorrectlySetErrorAndEmptyReturn() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new SocketTimeoutException("Request timeout");
            }
        };

        wrapper = new BackblazeApiWrapper(stubUrlHandler);

        String filePath = this.getClass().getResource("UploadFileTest.txt").getPath();
        GetUploadUrlResponse uploadUrlResponse = GsonService.fromJson(getUploadUrlResponseJson, GetUploadUrlResponse.class);
        Optional<UploadFileResponse> response = wrapper.uploadFile(Paths.get(""), filePath, null, uploadUrlResponse);
        assertThat(response, equalTo(Optional.empty()));
        ErrorResponse error = wrapper.getLastError().get();
        assertThat(error.status, equalTo(HttpStatus.SC_REQUEST_TIMEOUT));
        assertThat(error.message, equalTo("Request timeout"));
        assertThat(error.code, equalTo("request_timeout"));
        verify(mockUrlCon, times(0)).disconnect();
    }

    @Test
    public void uploadFileShouldUploadAllFileContents() throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(uploadFileResponseJson.getBytes("UTF-8"));
        doReturn(is).when(mockUrlCon).getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        doReturn(os).when(mockUrlCon).getOutputStream();
        doReturn(HttpStatus.SC_OK).when(mockUrlCon).getResponseCode();

        String filePath = this.getClass().getResource("UploadFileTest.txt").getPath();
        GetUploadUrlResponse uploadUrlResponse = GsonService.fromJson(getUploadUrlResponseJson, GetUploadUrlResponse.class);

        wrapper.uploadFile(Paths.get(""), filePath, "", uploadUrlResponse);

        byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
        String fileContents = new String(fileBytes);
        assertThat(os.toString(), equalTo(fileContents));
    }

    @Test
    public void uploadFileShouldCloseConnectionAfterSuccessfulUpload() throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(uploadFileResponseJson.getBytes("UTF-8"));
        doReturn(is).when(mockUrlCon).getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        doReturn(os).when(mockUrlCon).getOutputStream();
        doReturn(HttpStatus.SC_OK).when(mockUrlCon).getResponseCode();

        String filePath = this.getClass().getResource("UploadFileTest.txt").getPath();
        GetUploadUrlResponse uploadUrlResponse = GsonService.fromJson(getUploadUrlResponseJson, GetUploadUrlResponse.class);

        wrapper.uploadFile(Paths.get(""), filePath, null, uploadUrlResponse);

        verify(mockUrlCon).disconnect();
    }

    @Test
    public void exceptionDuringOpeningConnectionOnGetUploadUrlShouldCloseConnection() throws Exception {
        doThrow(new IOException("Bad")).when(mockUrlCon).getInputStream();

        try {
            wrapper.getUploadUrl(GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class), "asdf");
        } catch (Exception e) {
        }
        verify(mockUrlCon).disconnect();
    }

    @Test
    public void exceptionDuringOpeningConnectionOnGetUploadUrlShouldNotResultInAttemptToCloseConnection() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new IOException("Bad, bad connection");
            }
        };

        wrapper = new BackblazeApiWrapper(stubUrlHandler);

        try {
            wrapper.getUploadUrl(GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class), "asdf");
        } catch (Exception e) {
        }
        verify(mockUrlCon, times(0)).disconnect();
    }

    @Test
    public void requestTimeoutDuringConnectionOnGetUploadUrlShouldResultInCorrectlySetErrorAndEmptyReturn() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new SocketTimeoutException("Request timeout");
            }
        };

        wrapper = new BackblazeApiWrapper(stubUrlHandler);

        AuthorizeResponse authorizeResponse = GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class);
        Optional<GetUploadUrlResponse> response = wrapper.getUploadUrl(authorizeResponse, "asdf");
        assertThat(response, equalTo(Optional.empty()));
        ErrorResponse error = wrapper.getLastError().get();
        assertThat(error.status, equalTo(HttpStatus.SC_REQUEST_TIMEOUT));
        assertThat(error.message, equalTo("Request timeout"));
        assertThat(error.code, equalTo("request_timeout"));
        verify(mockUrlCon, times(0)).disconnect();
    }

    @Test
    public void successfulGetUploadUrlCallShouldResultInCorrectlyPopulatedResponseFields() throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(getUploadUrlResponseJson.getBytes("UTF-8"));
        doReturn(is).when(mockUrlCon).getInputStream();
        doReturn(mock(OutputStream.class)).when(mockUrlCon).getOutputStream();
        doReturn(HttpStatus.SC_OK).when(mockUrlCon).getResponseCode();

        GetUploadUrlResponse response = wrapper.getUploadUrl(defAuthResponse, "bukhet").get();
        verify(mockUrlCon).disconnect();
        assertThat(response.bucketId, equalTo("4a48fe8875c6214145260818"));
        assertThat(response.authorizationToken, equalTo("2_20151009170037_f504a0f39a0f4e657337e624_9754dde94359bd7b8f1445c8f4cc1a231a33f714_upld"));
        assertThat(response.uploadUrl, equalTo("https://pod-000-1005-03.backblaze.com/b2api/v1/b2_upload_file?cvt=c001_v0001005_t0027&bucket=4a48fe8875c6214145260818"));
    }
    
    @Test
    public void exceptionDuringErrorResponseParsingShouldResultInUnknownErrorSet() throws Exception {
        InputStream is = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("read error");
            }
        };
        final Integer responseCode = 401;
        doReturn(is).when(mockUrlCon).getErrorStream();
        doReturn(responseCode).when(mockUrlCon).getResponseCode();

        String accountId = "account_id";
        String applicationKey = "application_key";
        Optional<AuthorizeResponse> authorizeResponse = wrapper.authorize(accountId, applicationKey);
        assertThat(authorizeResponse, equalTo(Optional.empty()));
        ErrorResponse error = wrapper.getLastError().orElseThrow(
                () -> new Exception("Should return error response"));
        assertThat(error.status, equalTo(responseCode));
        assertThat(error.message, equalTo("Error while reading error response body"));
        assertThat(error.code, equalTo("unknown"));
    }

    @Test
    public void errorResponseShouldContainRetryAfterValueIfPresent() throws Exception {
        doReturn("6").when(mockUrlCon).getHeaderField("Retry-After");
        ByteArrayInputStream is = new ByteArrayInputStream(uploadFileResponseJson.getBytes("UTF-8"));
        doReturn(is).when(mockUrlCon).getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        doReturn(os).when(mockUrlCon).getOutputStream();
        ByteArrayInputStream es =  new ByteArrayInputStream(errorResponseJson.getBytes("UTF-8"));
        doReturn(es).when(mockUrlCon).getErrorStream();
        doReturn(HttpStatus.SC_SERVICE_UNAVAILABLE).when(mockUrlCon).getResponseCode();

        String accountId = "account_id";
        String applicationKey = "application_key";
        wrapper.authorize(accountId, applicationKey);
        ErrorResponse error = wrapper.getLastError().get();
        assertThat(error.retryAfter, equalTo(6));
    }

    @Test
    public void errorResponseShouldBeNullIfRetryAfterValueNotPresent() throws Exception {
        doReturn(null).when(mockUrlCon).getHeaderField("Retry-After");
        ByteArrayInputStream is = new ByteArrayInputStream(uploadFileResponseJson.getBytes("UTF-8"));
        doReturn(is).when(mockUrlCon).getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        doReturn(os).when(mockUrlCon).getOutputStream();
        ByteArrayInputStream es =  new ByteArrayInputStream(errorResponseJson.getBytes("UTF-8"));
        doReturn(es).when(mockUrlCon).getErrorStream();
        doReturn(HttpStatus.SC_SERVICE_UNAVAILABLE).when(mockUrlCon).getResponseCode();

        String accountId = "account_id";
        String applicationKey = "application_key";
        wrapper.authorize(accountId, applicationKey);
        ErrorResponse error = wrapper.getLastError().get();
        assertThat(error.retryAfter, nullValue());
    }

    @Test
    public void exceptionDuringOpeningConnectionOnListFileNamesShouldCloseConnection() throws Exception {
        doThrow(new IOException("Bad")).when(mockUrlCon).getInputStream();

        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                return mockUrlCon;
            }
        };

        wrapper = new BackblazeApiWrapper(stubUrlHandler);

        try {
            wrapper.listFileNames(GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class), "bucketId");
        } catch (Exception e) {
        }
        verify(mockUrlCon).disconnect();
    }

    @Test
    public void exceptionDuringOpeningConnectionOnListFileNamesShouldNotResultInAttemptToCloseConnection() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new IOException("Bad, bad connection");
            }
        };

        wrapper = new BackblazeApiWrapper(stubUrlHandler);

        try {
            wrapper.listFileNames(GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class), "bucketId");
        } catch (Exception e) {
        }
        verify(mockUrlCon, times(0)).disconnect();
    }

    @Test
    public void requestTimeoutDuringConnectionOnListFileNamesShouldResultInCorrectlySetErrorAndEmptyReturn() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new SocketTimeoutException("Request timeout");
            }
        };

        wrapper = new BackblazeApiWrapper(stubUrlHandler);

        AuthorizeResponse authorizeResponse = GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class);
        Optional<ListFileNamesResponse> response = wrapper.listFileNames(authorizeResponse, "bucketId");
        assertThat(response, equalTo(Optional.empty()));
        ErrorResponse error = wrapper.getLastError().get();
        assertThat(error.status, equalTo(HttpStatus.SC_REQUEST_TIMEOUT));
        assertThat(error.message, equalTo("Request timeout"));
        assertThat(error.code, equalTo("request_timeout"));
        verify(mockUrlCon, times(0)).disconnect();
    }

    @Test
    public void successfulListFileNamesCallShouldResultInCorrectlyPopulatedResponseFields() throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(listFileNamesResponseJson.getBytes("UTF-8"));
        doReturn(is).when(mockUrlCon).getInputStream();
        doReturn(HttpStatus.SC_OK).when(mockUrlCon).getResponseCode();
        doReturn(mock(OutputStream.class)).when(mockUrlCon).getOutputStream();

        AuthorizeResponse authorizeResponse = GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class);
        ListFileNamesResponse response = wrapper.listFileNames(authorizeResponse, "bucketId").get();
        verify(mockUrlCon).disconnect();
        assertThat(response.fileNames.size(), equalTo(2));
        FileName fileName = response.fileNames.get(0);
        assertThat(fileName.action, equalTo("upload"));
        assertThat(fileName.contentLength, equalTo(6));
        assertThat(fileName.fileId, equalTo("4_z27c88f1d182b150646ff0b16_f1004ba650fe24e6b_d20150809_m012853_c100_v0009990_t0000"));
        assertThat(fileName.fileName, equalTo("files/hello.txt"));
        assertThat(fileName.size, equalTo(6));
        assertThat(fileName.uploadTimestamp, equalTo(1439083733000L));
        fileName = response.fileNames.get(1);
        assertThat(fileName.action, equalTo("upload"));
        assertThat(fileName.contentLength, equalTo(6));
        assertThat(fileName.fileId, equalTo("4_z27c88f1d182b150646ff0b16_f1004ba650fe24e6c_d20150809_m012854_c100_v0009990_t0000"));
        assertThat(fileName.fileName, equalTo("files/world.txt"));
        assertThat(fileName.size, equalTo(6));
        assertThat(fileName.uploadTimestamp, equalTo(1439083734000L));
        assertThat(response.nextFileName, nullValue());
    }

    @Test
    public void exceptionDuringOpeningConnectionOnDownloadFileShouldCloseConnection() throws Exception {
        doThrow(new IOException("Bad")).when(mockUrlCon).getInputStream();

        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                return mockUrlCon;
            }
        };
        wrapper = new BackblazeApiWrapper(stubUrlHandler);
        AuthorizeResponse authorizeResponse = GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class);
        try {
            wrapper.downloadFileByName("bukhet", "file", Paths.get(""), authorizeResponse);
        } catch (Exception e) {
        }
        verify(mockUrlCon).disconnect();
    }

    @Test
    public void exceptionDuringOpeningConnectionOnDownloadFileShouldNotResultInAttemptToCloseConnection() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new IOException("Bad, bad connection");
            }
        };
        wrapper = new BackblazeApiWrapper(stubUrlHandler);
        AuthorizeResponse authorizeResponse = GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class);
        try {
            wrapper.downloadFileByName("bukhet", "file", Paths.get(""), authorizeResponse);
        } catch (Exception e) {
        }
        verify(mockUrlCon, times(0)).disconnect();
    }

    @Test
    public void requestTimeoutDuringConnectionOnDownloadFileShouldResultInCorrectlySetErrorAndEmptyReturn() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new SocketTimeoutException("Request timeout");
            }
        };

        wrapper = new BackblazeApiWrapper(stubUrlHandler);

        AuthorizeResponse authorizeResponse = GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class);
        Optional<DownloadFileResponse> response = wrapper.downloadFileByName("bukhet", "file", Paths.get(""), authorizeResponse);
        assertThat(response, equalTo(Optional.empty()));
        ErrorResponse error = wrapper.getLastError().get();
        assertThat(error.status, equalTo(HttpStatus.SC_REQUEST_TIMEOUT));
        assertThat(error.message, equalTo("Request timeout"));
        assertThat(error.code, equalTo("request_timeout"));
        verify(mockUrlCon, times(0)).disconnect();
    }

    @Test
    public void downloadFileShouldSendConnectionOutputStreamToDesignatedFileAndReturnProperlyFilledResponse() throws Exception {
        String filePath = this.getClass().getResource("UploadFileTest.txt").getPath();
        final byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
        ByteArrayInputStream is = new ByteArrayInputStream(fileBytes);
        doReturn(is).when(mockUrlCon).getInputStream();
        doReturn(HttpStatus.SC_OK).when(mockUrlCon).getResponseCode();
        final String fileId = "4_h4a48fe8875c6214145260818_f000000000000472a_d20140104_m032022_c001_v0000123_t0104";
        doReturn(fileId).when(mockUrlCon).getHeaderField("X-Bz-File-Id");
        final String fileName = "dir1/dir2/fileName.txt";
        doReturn(fileName).when(mockUrlCon).getHeaderField("X-Bz-File-Name");
        final String contentSha1 = "bae5ed658ab3546aee12f23f36392f35dba1ebdd";
        doReturn(contentSha1).when(mockUrlCon).getHeaderField("X-Bz-Content-Sha1");

        FileOutputStream mockFos = mock(FileOutputStream.class);
        doReturn(mock(FileChannel.class)).when(mockFos).getChannel();
        BackblazeApiWrapper.FileOutputStreamFactory mockFosFactory = mock(BackblazeApiWrapper.FileOutputStreamFactory.class);
        doReturn(mockFos).when(mockFosFactory).create(any());
        wrapper = new BackblazeApiWrapper(stubUrlHandler, mockFosFactory);

        AuthorizeResponse authorizeResponse = GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class);
        final String bucketName = "bukhet";
        final String destination = "/path/to/dest";
        Optional<DownloadFileResponse> maybeResponse =
                wrapper.downloadFileByName(bucketName, fileName, Paths.get(destination), authorizeResponse);

        verify(mockFosFactory).create("/path/to/dest/dir1/dir2/fileName.txt");
        verify(mockFos).close();
        assertThat(maybeResponse.isPresent(), equalTo(true));
        DownloadFileResponse response = maybeResponse.get();
        assertThat(response.fileId, equalTo(fileId));
        assertThat(response.fileName, equalTo(fileName));
        assertThat(response.contentSha1, equalTo(contentSha1));
    }

    @Test
    public void exceptionDuringOpeningConnectionOnStartLargeFileShouldCloseConnection() throws Exception {
        doThrow(new IOException("Bad")).when(mockUrlCon).getInputStream();

        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                return mockUrlCon;
            }
        };
        wrapper = new BackblazeApiWrapper(stubUrlHandler);
        AuthorizeResponse authorizeResponse = GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class);
        try {
            wrapper.startLargeFile(authorizeResponse,"relative/path/to/file.txt", "4a48fe8875c6214145260818");
        } catch (Exception e) {
        }
        verify(mockUrlCon).disconnect();
    }

    @Test
    public void exceptionDuringOpeningConnectionOnStartLargeFileShouldNotResultInAttemptToCloseConnection() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new IOException("Bad, bad connection");
            }
        };
        wrapper = new BackblazeApiWrapper(stubUrlHandler);
        AuthorizeResponse authorizeResponse = GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class);
        try {
            wrapper.startLargeFile(authorizeResponse,"relative/path/to/file.txt", "4a48fe8875c6214145260818");
        } catch (Exception e) {
        }
        verify(mockUrlCon, times(0)).disconnect();
    }

    @Test
    public void requestTimeoutDuringConnectionOnStartLargeFileShouldResultInCorrectlySetErrorAndEmptyReturn() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new SocketTimeoutException("Request timeout");
            }
        };

        wrapper = new BackblazeApiWrapper(stubUrlHandler);

        AuthorizeResponse authorizeResponse = GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class);
        Optional<StartLargeFileResponse> response = wrapper.startLargeFile(authorizeResponse,"relative/path/to/file.txt", "4a48fe8875c6214145260818");
        assertThat(response, equalTo(Optional.empty()));
        ErrorResponse error = wrapper.getLastError().get();
        assertThat(error.status, equalTo(HttpStatus.SC_REQUEST_TIMEOUT));
        assertThat(error.message, equalTo("Request timeout"));
        assertThat(error.code, equalTo("request_timeout"));
        verify(mockUrlCon, times(0)).disconnect();
    }

    @Test
    public void successfulStartLargeFileCallShouldResultInCorrectlyPopulatedResponseFields() throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(startLargeFileResponseJson.getBytes("UTF-8"));
        doReturn(is).when(mockUrlCon).getInputStream();
        doReturn(mock(OutputStream.class)).when(mockUrlCon).getOutputStream();
        doReturn(HttpStatus.SC_OK).when(mockUrlCon).getResponseCode();

        StartLargeFileResponse response = wrapper.startLargeFile(defAuthResponse,"relative/path/to/file.txt", "4a48fe8875c6214145260818").get();
        verify(mockUrlCon).disconnect();
        assertThat(response.accountId , equalTo("d522aa47a10f"));
        assertThat(response.bucketId , equalTo("e73ede9c9c8412db49f60715"));
        assertThat(response.contentType , equalTo("b2/x-auto"));
        assertThat(response.fileId, equalTo("4_za71f544e781e6891531b001a_f200ec353a2184825_d20160409_m004829_c000_v0001016_t0028"));
        assertThat(GsonService.toJson(response.fileInfo) , equalTo("{}"));
        assertThat(response.fileName , equalTo("bigfile.dat"));
        assertThat(response.uploadTimestamp , equalTo(1460162909000L));
    }
    
    @Test
    public void exceptionDuringOpeningConnectionOnGetUploadPartUrlShouldCloseConnection() throws Exception {
        doThrow(new IOException("Bad")).when(mockUrlCon).getInputStream();

        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                return mockUrlCon;
            }
        };
        wrapper = new BackblazeApiWrapper(stubUrlHandler);
        AuthorizeResponse authorizeResponse = GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class);
        try {
            wrapper.getUploadPartUrl(authorizeResponse,"4_h4a48fe8875c6214145260818_f000000000000472a_d20140104_m032022_c001_v0000123_t0104");
        } catch (Exception e) {
        }
        verify(mockUrlCon).disconnect();
    }

    @Test
    public void exceptionDuringOpeningConnectionOnGetUploadPartUrlShouldNotResultInAttemptToCloseConnection() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new IOException("Bad, bad connection");
            }
        };
        wrapper = new BackblazeApiWrapper(stubUrlHandler);
        AuthorizeResponse authorizeResponse = GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class);
        try {
            wrapper.getUploadPartUrl(authorizeResponse,"4_h4a48fe8875c6214145260818_f000000000000472a_d20140104_m032022_c001_v0000123_t0104");
        } catch (Exception e) {
        }
        verify(mockUrlCon, times(0)).disconnect();
    }

    @Test
    public void requestTimeoutDuringConnectionOnGetUploadPartUrlShouldResultInCorrectlySetErrorAndEmptyReturn() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new SocketTimeoutException("Request timeout");
            }
        };

        wrapper = new BackblazeApiWrapper(stubUrlHandler);

        AuthorizeResponse authorizeResponse = GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class);
        Optional<GetUploadPartUrlResponse> response = wrapper.getUploadPartUrl(authorizeResponse,"4_h4a48fe8875c6214145260818_f000000000000472a_d20140104_m032022_c001_v0000123_t0104");
        assertThat(response, equalTo(Optional.empty()));
        ErrorResponse error = wrapper.getLastError().get();
        assertThat(error.status, equalTo(HttpStatus.SC_REQUEST_TIMEOUT));
        assertThat(error.message, equalTo("Request timeout"));
        assertThat(error.code, equalTo("request_timeout"));
        verify(mockUrlCon, times(0)).disconnect();
    }
    
    @Test
    public void successfulGetUploadPartUrlCallShouldResultInCorrectlyPopulatedResponseFields() throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(getUploadPartUrlResponseJson.getBytes("UTF-8"));
        doReturn(is).when(mockUrlCon).getInputStream();
        doReturn(mock(OutputStream.class)).when(mockUrlCon).getOutputStream();
        doReturn(HttpStatus.SC_OK).when(mockUrlCon).getResponseCode();

        GetUploadPartUrlResponse response = wrapper.getUploadPartUrl(defAuthResponse,"4_h4a48fe8875c6214145260818_f000000000000472a_d20140104_m032022_c001_v0000123_t0104").get();
        verify(mockUrlCon).disconnect();
        assertThat(response.fileId, equalTo("4_ze73ede9c9c8412db49f60715_f100b4e93fbae6252_d20150824_m224353_c900_v8881000_t0001"));
        assertThat(response.authorizationToken, equalTo("3_20160409004829_42b8f80ba60fb4323dcaad98_ec81302316fccc2260201cbf17813247f312cf3b_000_uplg"));
        assertThat(response.uploadUrl, equalTo("https://pod-000-1016-09.backblaze.com/b2api/v1/b2_upload_part/4_ze73ede9c9c8412db49f60715_f100b4e93fbae6252_d20150824_m224353_c900_v8881000_t0001/0037"));
    }

    @Test
    public void exceptionDuringOpeningConnectionOnUploadPartShouldCloseConnection() throws Exception {
        doThrow(new IOException("Bad")).when(mockUrlCon).getInputStream();

        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                return mockUrlCon;
            }
        };
        doReturn("hash").when(mockFileHash).getHashValue(any());

        wrapper = new BackblazeApiWrapper(stubUrlHandler, mockFileHash);

        GetUploadPartUrlResponse uploadPartUrlResponse = GsonService.fromJson(getUploadPartUrlResponseJson, GetUploadPartUrlResponse.class);
        try {
            wrapper.uploadPart(new byte[10], 10, 1, uploadPartUrlResponse);
        } catch (Exception e) {
        }
        verify(mockUrlCon).disconnect();
    }

    @Test
    public void exceptionDuringOpeningConnectionOnUploadPartShouldNotResultInAttemptToCloseConnection() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new IOException("Bad, bad connection");
            }
        };

        wrapper = new BackblazeApiWrapper(stubUrlHandler, mockFileHash);

        GetUploadPartUrlResponse uploadPartUrlResponse = GsonService.fromJson(getUploadPartUrlResponseJson, GetUploadPartUrlResponse.class);
        try {
            wrapper.uploadPart(new byte[10], 10, 1, uploadPartUrlResponse);
        } catch (Exception e) {
        }
        verify(mockUrlCon, times(0)).disconnect();
    }

    @Test
    public void requestTimeoutDuringConnectionOnUploadPartShouldResultInCorrectlySetErrorAndEmptyReturn() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new SocketTimeoutException("Request timeout");
            }
        };

        wrapper = new BackblazeApiWrapper(stubUrlHandler);

        GetUploadPartUrlResponse uploadPartUrlResponse = GsonService.fromJson(getUploadPartUrlResponseJson, GetUploadPartUrlResponse.class);
        Optional<UploadPartResponse> response = wrapper.uploadPart(new byte[10], 10, 1, uploadPartUrlResponse);
        assertThat(response, equalTo(Optional.empty()));
        ErrorResponse error = wrapper.getLastError().get();
        assertThat(error.status, equalTo(HttpStatus.SC_REQUEST_TIMEOUT));
        assertThat(error.message, equalTo("Request timeout"));
        assertThat(error.code, equalTo("request_timeout"));
        verify(mockUrlCon, times(0)).disconnect();
    }
    
    @Test
    public void uploadPartShouldUploadAllFileContents() throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(uploadPartResponseJson.getBytes("UTF-8"));
        doReturn(is).when(mockUrlCon).getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        doReturn(os).when(mockUrlCon).getOutputStream();
        doReturn(HttpStatus.SC_OK).when(mockUrlCon).getResponseCode();

        String filePath = this.getClass().getResource("UploadFileTest.txt").getPath();
        int length = (int) Files.size(Paths.get(filePath));
        byte[] buf = new byte[length];
        FileInputStream fis = new FileInputStream(filePath);
        fis.read(buf, 0, length);
        fis.close();

        GetUploadPartUrlResponse uploadPartUrlResponse = GsonService.fromJson(getUploadPartUrlResponseJson, GetUploadPartUrlResponse.class);
        wrapper = new BackblazeApiWrapper(stubUrlHandler);
        wrapper.uploadPart(buf, length,1, uploadPartUrlResponse);

        byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
        String fileContents = new String(fileBytes);
        assertThat(os.toString(), equalTo(fileContents));
    }

    @Test
    public void successfulUploadPartCallShouldResultInCorrectlyPopulatedResponseFields() throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(uploadPartResponseJson.getBytes("UTF-8"));
        doReturn(is).when(mockUrlCon).getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        doReturn(os).when(mockUrlCon).getOutputStream();
        doReturn(HttpStatus.SC_OK).when(mockUrlCon).getResponseCode();

        String filePath = this.getClass().getResource("UploadFileTest.txt").getPath();
        int length = (int) Files.size(Paths.get(filePath));
        byte[] buf = new byte[length];
        FileInputStream fis = new FileInputStream(filePath);
        fis.read(buf, 0, length);
        fis.close();

        GetUploadPartUrlResponse uploadPartUrlResponse = GsonService.fromJson(getUploadPartUrlResponseJson, GetUploadPartUrlResponse.class);
        UploadPartResponse response = wrapper.uploadPart(buf, length,1, uploadPartUrlResponse).get();

        assertThat(response.fileId, equalTo("4_ze73ede9c9c8412db49f60715_f100b4e93fbae6252_d20150824_m224353_c900_v8881000_t0001"));
        assertThat(response.contentSha1, equalTo("062685a84ab248d2488f02f6b01b948de2514ad8"));
        assertThat(response.contentLength, equalTo(100000000));
        assertThat(response.partNumber, equalTo(1));
    }

    @Test
    public void uploadPartShouldCloseConnectionAfterSuccessfulUpload() throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(uploadPartResponseJson.getBytes("UTF-8"));
        doReturn(is).when(mockUrlCon).getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        doReturn(os).when(mockUrlCon).getOutputStream();
        doReturn(HttpStatus.SC_OK).when(mockUrlCon).getResponseCode();

        GetUploadPartUrlResponse uploadPartUrlResponse = GsonService.fromJson(getUploadPartUrlResponseJson, GetUploadPartUrlResponse.class);
        wrapper.uploadPart(new byte[10], 10, 1, uploadPartUrlResponse);

        verify(mockUrlCon).disconnect();
    }

    @Test
    public void exceptionDuringOpeningConnectionOnFinishLargeFileShouldCloseConnection() throws Exception {
        doThrow(new IOException("Bad")).when(mockUrlCon).getInputStream();

        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                return mockUrlCon;
            }
        };
        wrapper = new BackblazeApiWrapper(stubUrlHandler);
        AuthorizeResponse authorizeResponse = GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class);
        try {
            wrapper.finishLargeFile(authorizeResponse,"4_ze73ede9c9c8412db49f60715", new ArrayList<>());
        } catch (Exception e) {
        }
        verify(mockUrlCon).disconnect();
    }

    @Test
    public void exceptionDuringOpeningConnectionOnFinishLargeFileShouldNotResultInAttemptToCloseConnection() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new IOException("Bad, bad connection");
            }
        };
        wrapper = new BackblazeApiWrapper(stubUrlHandler);
        AuthorizeResponse authorizeResponse = GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class);
        try {
            wrapper.finishLargeFile(authorizeResponse,"4_ze73ede9c9c8412db49f60715", new ArrayList<>());
        } catch (Exception e) {
        }
        verify(mockUrlCon, times(0)).disconnect();
    }

    @Test
    public void requestTimeoutDuringConnectionOnFinishLargeFileShouldResultInCorrectlySetErrorAndEmptyReturn() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new SocketTimeoutException("Request timeout");
            }
        };

        wrapper = new BackblazeApiWrapper(stubUrlHandler);

        AuthorizeResponse authorizeResponse = GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class);
        Optional<FinishLargeFileResponse> response = wrapper.finishLargeFile(authorizeResponse,"4_ze73ede9c9c8412db49f60715", new ArrayList<>());
        assertThat(response, equalTo(Optional.empty()));
        ErrorResponse error = wrapper.getLastError().get();
        assertThat(error.status, equalTo(HttpStatus.SC_REQUEST_TIMEOUT));
        assertThat(error.message, equalTo("Request timeout"));
        assertThat(error.code, equalTo("request_timeout"));
        verify(mockUrlCon, times(0)).disconnect();
    }

    @Test
    public void successfulFinishLargeFileCallShouldResultInCorrectlyPopulatedResponseFields() throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(finishLargeFileResponseJson.getBytes("UTF-8"));
        doReturn(is).when(mockUrlCon).getInputStream();
        doReturn(mock(OutputStream.class)).when(mockUrlCon).getOutputStream();
        doReturn(HttpStatus.SC_OK).when(mockUrlCon).getResponseCode();

        FinishLargeFileResponse response = wrapper.finishLargeFile(defAuthResponse,"4_ze73ede9c9c8412db49f60715", new ArrayList<>()).get();
        verify(mockUrlCon).disconnect();
        assertThat(response.accountId , equalTo("d522aa47a10f"));
        assertThat(response.action, equalTo("upload"));
        assertThat(response.bucketId , equalTo("e73ede9c9c8412db49f60715"));
        assertThat(response.contentLength, equalTo(208158542));
        assertThat(response.contentSha1, equalTo("none"));
        assertThat(response.contentType , equalTo("b2/x-auto"));
        assertThat(response.fileId, equalTo("4_ze73ede9c9c8412db49f60715_f100b4e93fbae6252_d20150824_m224353_c900_v8881000_t0001"));
        assertThat(GsonService.toJson(response.fileInfo) , equalTo("{}"));
        assertThat(response.fileName , equalTo("bigfile.dat"));
        assertThat(response.uploadTimestamp , equalTo(1460162909000L));
    }

    @Test
    public void exceptionDuringOpeningConnectionOnCancelLargeFileShouldCloseConnection() throws Exception {
        doThrow(new IOException("Bad")).when(mockUrlCon).getInputStream();

        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                return mockUrlCon;
            }
        };
        wrapper = new BackblazeApiWrapper(stubUrlHandler);
        AuthorizeResponse authorizeResponse = GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class);
        try {
            wrapper.cancelLargeFile(authorizeResponse,"4_ze73ede9c9c8412db49f60715");
        } catch (Exception e) {
        }
        verify(mockUrlCon).disconnect();
    }

    @Test
    public void exceptionDuringOpeningConnectionOnCancelLargeFileShouldNotResultInAttemptToCloseConnection() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new IOException("Bad, bad connection");
            }
        };
        wrapper = new BackblazeApiWrapper(stubUrlHandler);
        AuthorizeResponse authorizeResponse = GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class);
        try {
            wrapper.cancelLargeFile(authorizeResponse,"4_ze73ede9c9c8412db49f60715");
        } catch (Exception e) {
        }
        verify(mockUrlCon, times(0)).disconnect();
    }

    @Test
    public void requestTimeoutDuringConnectionOnCancelLargeFileShouldResultInCorrectlySetErrorAndEmptyReturn() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new SocketTimeoutException("Request timeout");
            }
        };

        wrapper = new BackblazeApiWrapper(stubUrlHandler);

        AuthorizeResponse authorizeResponse = GsonService.fromJson(authorizeResponseJson, AuthorizeResponse.class);
        Optional<CancelLargeFileResponse> response = wrapper.cancelLargeFile(authorizeResponse,"4_ze73ede9c9c8412db49f60715");
        assertThat(response, equalTo(Optional.empty()));
        ErrorResponse error = wrapper.getLastError().get();
        assertThat(error.status, equalTo(HttpStatus.SC_REQUEST_TIMEOUT));
        assertThat(error.message, equalTo("Request timeout"));
        assertThat(error.code, equalTo("request_timeout"));
        verify(mockUrlCon, times(0)).disconnect();
    }

    @Test
    public void successfulCancelLargeFileCallShouldResultInCorrectlyPopulatedResponseFields() throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(cancelLargeFileResponseJson.getBytes("UTF-8"));
        doReturn(is).when(mockUrlCon).getInputStream();
        doReturn(mock(OutputStream.class)).when(mockUrlCon).getOutputStream();
        doReturn(HttpStatus.SC_OK).when(mockUrlCon).getResponseCode();

        CancelLargeFileResponse response = wrapper.cancelLargeFile(defAuthResponse,"4_ze73ede9c9c8412db49f60715").get();
        verify(mockUrlCon).disconnect();
        assertThat(response.accountId , equalTo("d522aa47a10f"));
        assertThat(response.bucketId , equalTo("4a48fe8875c6214145260818"));
        assertThat(response.fileId, equalTo("4_za71f544e781e6891531b001a_f200ec353a2184825_d20160409_m004829_c000_v0001016_t0028"));
        assertThat(response.fileName , equalTo("bigfile.dat"));
    }
}