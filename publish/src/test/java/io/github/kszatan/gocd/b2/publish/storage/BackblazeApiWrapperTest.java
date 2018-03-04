/*
 * Copyright (c) 2018. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.storage;

import io.github.kszatan.gocd.b2.publish.json.GsonService;
import io.github.kszatan.gocd.b2.publish.storage.api.BackblazeApiWrapper;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
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
    private String uploadFileResponseJson = "{\n" +
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
        assertThat(bucket.bucketId, equalTo("4a48fe8875c6214145260818"));
        assertThat(bucket.bucketName, equalTo("Kitten-Videos"));
        assertThat(bucket.bucketType, equalTo("allPrivate"));
        bucket = response.buckets.get(1);
        assertThat(bucket.accountId, equalTo("30f20426f0b1"));
        assertThat(bucket.bucketId, equalTo("5b232e8875c6214145260818"));
        assertThat(bucket.bucketName, equalTo("Puppy-Videos"));
        assertThat(bucket.bucketType, equalTo("allPublic"));
        bucket = response.buckets.get(2);
        assertThat(bucket.accountId, equalTo("30f20426f0b1"));
        assertThat(bucket.bucketId, equalTo("87ba238875c6214145260818"));
        assertThat(bucket.bucketName, equalTo("Vacation-Pictures"));
        assertThat(bucket.bucketType, equalTo("allPrivate"));
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
    public void uploadFileShouldReturnEmptyOptionalAndSetErrorOnSocketTimeout() throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(uploadFileResponseJson.getBytes("UTF-8"));
        doReturn(is).when(mockUrlCon).getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        doReturn(os).when(mockUrlCon).getOutputStream();
        doReturn(HttpStatus.SC_OK).when(mockUrlCon).getResponseCode();

        String filePath = this.getClass().getResource("UploadFileTest.txt").getPath();
        GetUploadUrlResponse uploadUrlResponse = GsonService.fromJson(getUploadUrlResponseJson, GetUploadUrlResponse.class);

        wrapper.uploadFile(Paths.get(""), filePath, "", uploadUrlResponse);

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
    public void errorResponseShouldBeMinusOneIfRetryAfterValueNotPresent() throws Exception {
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
        assertThat(error.retryAfter, equalTo(-1));
    }
}