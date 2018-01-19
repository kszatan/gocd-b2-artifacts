/*
 * Copyright (c) 2018. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.storage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLStreamHandler;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class BackblazeStorageTest {
    private BackblazeStorage storage;
    private URLStreamHandler stubUrlHandler;
    private HttpURLConnection mockUrlCon;
    private final String authorizeResponse = "{\n" +
            "  \"absoluteMinimumPartSize\": 5000000,\n" +
            "  \"accountId\": \"aaaabbbbcccc\",\n" +
            "  \"apiUrl\": \"https://api001.backblazeb2.com\",\n" +
            "  \"authorizationToken\": \"token_fristajlo\",\n" +
            "  \"downloadUrl\": \"https://f001.backblazeb2.com\",\n" +
            "  \"minimumPartSize\": 100000000,\n" +
            "  \"recommendedPartSize\": 100000000\n" +
            "}";
    private final String getUploadUrlResponse = "{\n" +
            "  \"bucketId\" : \"4a48fe8875c6214145260818\",\n" +
            "  \"uploadUrl\" : \"https://pod-000-1005-03.backblaze.com/b2api/v1/b2_upload_file?cvt=c001_v0001005_t0027&bucket=4a48fe8875c6214145260818\",\n" +
            "  \"authorizationToken\" : \"2_20151009170037_f504a0f39a0f4e657337e624_9754dde94359bd7b8f1445c8f4cc1a231a33f714_upld\"\n" +
            "}";

    @Before
    public void setUp() throws Exception {
        mockUrlCon = mock(HttpURLConnection.class);
        
        ByteArrayInputStream is1 = new ByteArrayInputStream(authorizeResponse.getBytes("UTF-8"));
        ByteArrayInputStream is2 = new ByteArrayInputStream(getUploadUrlResponse.getBytes("UTF-8"));
        doReturn(is1).doReturn(is2).when(mockUrlCon).getInputStream();
        doReturn(mock(OutputStream.class)).when(mockUrlCon).getOutputStream();

        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                return mockUrlCon;
            }
        };

        storage = new BackblazeStorage("defaultBucket");
        Whitebox.setInternalState(storage, "urlStreamHandler", stubUrlHandler);
    }

    @Test
    public void successfulAuthorizeCallShouldResultInCorrectlyPopulatedResponseFields() throws Exception {
        String accountId = "account_id";
        String applicationKey = "application_key";
        Boolean result = storage.authorize(accountId, applicationKey);
        assertThat(result, equalTo(true));
        verify(mockUrlCon, times(2)).disconnect();
        AuthorizeResponse authorizeResponse;
        authorizeResponse = (AuthorizeResponse)Whitebox.getInternalState(storage, "authorizeResponse");
        assertThat(authorizeResponse.absoluteMinimumPartSize, equalTo(5000000));
        assertThat(authorizeResponse.accountId, equalTo("aaaabbbbcccc"));
        assertThat(authorizeResponse.apiUrl, equalTo("https://api001.backblazeb2.com"));
        assertThat(authorizeResponse.authorizationToken, equalTo("token_fristajlo"));
        assertThat(authorizeResponse.downloadUrl, equalTo("https://f001.backblazeb2.com"));
        assertThat(authorizeResponse.minimumPartSize, equalTo(100000000));
        assertThat(authorizeResponse.recommendedPartSize, equalTo(100000000));
    }

    @Test
    public void exceptionDuringOpeningConnectionDuringAuthorizeShouldReturnFalse() throws Exception {
        doThrow(new IOException("Bad")).when(mockUrlCon).getInputStream();

        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                return mockUrlCon;
            }
        };

        storage = new BackblazeStorage("defaultBucket");
        Whitebox.setInternalState(storage, "urlStreamHandler", stubUrlHandler);
        
        String accountId = "account_id";
        String applicationKey = "application_key";
        Boolean result = storage.authorize(accountId, applicationKey);
        assertThat(result, equalTo(false));
    }

    @Test
    public void exceptionDuringAuthorizeShouldCloseConnectionAndReturnFalse() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new IOException("Bad, bad connection");
            }
        };

        storage = new BackblazeStorage("defaultBucket");
        Whitebox.setInternalState(storage, "urlStreamHandler", stubUrlHandler);

        String accountId = "account_id";
        String applicationKey = "application_key";
        Boolean result = storage.authorize(accountId, applicationKey);
        assertThat(result, equalTo(false));
//        verify(mockUrlCon).disconnect();
    }

    @Test
    public void exceptionDuringAuthorizeShouldResultInAuthorizeResponseSetToNull() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new IOException("Bad, bad connection");
            }
        };

        storage = new BackblazeStorage("defaultBucket");
        Whitebox.setInternalState(storage, "urlStreamHandler", stubUrlHandler);

        String accountId = "account_id";
        String applicationKey = "application_key";
        Boolean result = storage.authorize(accountId, applicationKey);
        assertThat(result, equalTo(false));
        AuthorizeResponse authorizeResponse;
        authorizeResponse = (AuthorizeResponse)Whitebox.getInternalState(storage, "authorizeResponse");
        assertThat(authorizeResponse, nullValue());
    }

}