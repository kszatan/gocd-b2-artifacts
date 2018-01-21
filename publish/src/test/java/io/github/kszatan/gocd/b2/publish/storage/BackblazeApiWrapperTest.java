/*
 * Copyright (c) 2018. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.storage;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLStreamHandler;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class BackblazeApiWrapperTest {
    private BackblazeApiWrapper wrapper;
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

        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                return mockUrlCon;
            }
        };

        wrapper = new BackblazeApiWrapper("defaultBucket", stubUrlHandler);
    }

    @Test
    public void successfulAuthorizeCallShouldResultInCorrectlyPopulatedResponseFields() throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(authorizeResponse.getBytes("UTF-8"));
        doReturn(is).when(mockUrlCon).getInputStream();

        String accountId = "account_id";
        String applicationKey = "application_key";
        AuthorizeResponse authorizeResponse = wrapper.authorize(accountId, applicationKey);
        verify(mockUrlCon).disconnect();
        assertThat(authorizeResponse.absoluteMinimumPartSize, equalTo(5000000));
        assertThat(authorizeResponse.accountId, equalTo("aaaabbbbcccc"));
        assertThat(authorizeResponse.apiUrl, equalTo("https://api001.backblazeb2.com"));
        assertThat(authorizeResponse.authorizationToken, equalTo("token_fristajlo"));
        assertThat(authorizeResponse.downloadUrl, equalTo("https://f001.backblazeb2.com"));
        assertThat(authorizeResponse.minimumPartSize, equalTo(100000000));
        assertThat(authorizeResponse.recommendedPartSize, equalTo(100000000));
    }

    @Test
    public void exceptionDuringOpeningConnectionDuringAuthorizeShouldCloseConnection() throws Exception {
        doThrow(new IOException("Bad")).when(mockUrlCon).getInputStream();

        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                return mockUrlCon;
            }
        };

        wrapper = new BackblazeApiWrapper("defaultBucket", stubUrlHandler);

        String accountId = "account_id";
        String applicationKey = "application_key";
        try {
            wrapper.authorize(accountId, applicationKey);
        } catch (Exception e) {
        }
        verify(mockUrlCon).disconnect();
    }

    @Test
    public void exceptionDuringOpeningConnectionShouldNotResultInAttemptToCloseConnection() throws Exception {
        stubUrlHandler = new URLStreamHandler() {
            @Override
            protected HttpURLConnection openConnection(URL u) throws IOException {
                throw new IOException("Bad, bad connection");
            }
        };

        wrapper = new BackblazeApiWrapper("defaultBucket", stubUrlHandler);

        String accountId = "account_id";
        String applicationKey = "application_key";
        try {
            wrapper.authorize(accountId, applicationKey);
        } catch (Exception e) {
        }
        verify(mockUrlCon, times(0)).disconnect();
    }

    @Test
    public void successfulGetUploadUrlCallShouldResultInCorrectlyPopulatedResponseFields() throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(getUploadUrlResponse.getBytes("UTF-8"));
        doReturn(is).when(mockUrlCon).getInputStream();
        doReturn(mock(OutputStream.class)).when(mockUrlCon).getOutputStream();

        AuthorizeResponse authorizeResponse = new AuthorizeResponse();
        authorizeResponse.absoluteMinimumPartSize =5000000;
        authorizeResponse.accountId = "aaaabbbbcccc";
        authorizeResponse.apiUrl = "https://api001.backblazeb2.com";
        authorizeResponse.authorizationToken = "token_fristajlo";
        authorizeResponse.downloadUrl = "https://f001.backblazeb2.com";
        authorizeResponse.minimumPartSize = 100000000;
        authorizeResponse.recommendedPartSize = 100000000;

        UploadUrlResponse response  = wrapper.getUploadUrl(authorizeResponse, "bukhet");
        verify(mockUrlCon).disconnect();
        assertThat(response.bucketId, equalTo("4a48fe8875c6214145260818"));
        assertThat(response.authorizationToken, equalTo("2_20151009170037_f504a0f39a0f4e657337e624_9754dde94359bd7b8f1445c8f4cc1a231a33f714_upld"));
        assertThat(response.uploadUrl, equalTo("https://pod-000-1005-03.backblaze.com/b2api/v1/b2_upload_file?cvt=c001_v0001005_t0027&bucket=4a48fe8875c6214145260818"));
    }

}