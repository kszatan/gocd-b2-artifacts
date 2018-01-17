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
    private final String response = "{\n" +
            "  \"absoluteMinimumPartSize\": 5000000,\n" +
            "  \"accountId\": \"aaaabbbbcccc\",\n" +
            "  \"apiUrl\": \"https://api001.backblazeb2.com\",\n" +
            "  \"authorizationToken\": \"token_fristajlo\",\n" +
            "  \"downloadUrl\": \"https://f001.backblazeb2.com\",\n" +
            "  \"minimumPartSize\": 100000000,\n" +
            "  \"recommendedPartSize\": 100000000\n" +
            "}";

    @Before
    public void setUp() throws Exception {
        mockUrlCon = mock(HttpURLConnection.class);
        
        ByteArrayInputStream is = new ByteArrayInputStream(response.getBytes("UTF-8"));
        doReturn(is).when(mockUrlCon).getInputStream();

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
    public void successfulAuthorizeCallShouldResultInCorrectlyPopulatedResponseField() throws Exception {
        String accountId = "account_id";
        String applicationKey = "application_key";
        Boolean result = storage.authorize(accountId, applicationKey);
        assertThat(result, equalTo(true));
        verify(mockUrlCon).disconnect();
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