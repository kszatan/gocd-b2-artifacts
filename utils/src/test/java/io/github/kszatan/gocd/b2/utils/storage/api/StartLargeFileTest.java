/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com> 
 * This file is subject to the license terms in the LICENSE file found in the 
 * top-level directory of this distribution. 
 */

package io.github.kszatan.gocd.b2.utils.storage.api;

import io.github.kszatan.gocd.b2.utils.storage.AuthorizeResponse;
import io.github.kszatan.gocd.b2.utils.storage.StartLargeFileResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class StartLargeFileTest {
    private StartLargeFile startLargeFile;
    private BackblazeApiWrapper mockApiWrapper;
    private AuthorizeResponse authorizeResponse;
    private String relativeFilePath;
    private String bucketId;

    @Before
    public void setUp() {
        mockApiWrapper = mock(BackblazeApiWrapper.class);
        authorizeResponse = new AuthorizeResponse();
        authorizeResponse.accountId = "accountId";
        authorizeResponse.absoluteMinimumPartSize = 100;
        authorizeResponse.apiUrl = "https://api001.backblazeb2.com";
        authorizeResponse.authorizationToken = "token_fristajlo";
        authorizeResponse.downloadUrl = "https://f001.backblazeb2.com";
        authorizeResponse.recommendedPartSize = 100000000;
        relativeFilePath = "path/to/file.txt";
        bucketId = "bukhet";
        startLargeFile = new StartLargeFile(mockApiWrapper, authorizeResponse, relativeFilePath, bucketId);
    }

    @Test
    public void callShouldReturnTrueOnSuccess() throws Exception {
        StartLargeFileResponse response = new StartLargeFileResponse();
        doReturn(Optional.of(response)).when(mockApiWrapper).startLargeFile(authorizeResponse, relativeFilePath, bucketId);
        Boolean result = startLargeFile.call();
        assertThat(result, equalTo(true));
    }

    @Test
    public void callShouldReturnFalseOnFailure() throws Exception {
        doReturn(Optional.empty()).when(mockApiWrapper).startLargeFile(authorizeResponse, relativeFilePath, bucketId);
        Boolean result = startLargeFile.call();
        assertThat(result, equalTo(false));
    }
}