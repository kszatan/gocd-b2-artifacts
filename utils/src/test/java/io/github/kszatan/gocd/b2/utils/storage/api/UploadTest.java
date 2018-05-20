/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com> 
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage.api;

import io.github.kszatan.gocd.b2.utils.storage.AuthorizeResponse;
import io.github.kszatan.gocd.b2.utils.storage.ErrorResponse;
import io.github.kszatan.gocd.b2.utils.storage.GetUploadUrlResponse;
import io.github.kszatan.gocd.b2.utils.storage.UploadFileResponse;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class UploadTest {
    private Upload upload;
    private BackblazeApiWrapper mockApiWrapper;
    private AuthorizeResponse authorizeResponse;
    private GetUploadUrlResponse getUploadUrlResponse;
    private String bucketId;
    private Path workDir;
    private Path relativeFilePath;
    private String destination;

    @Before
    public void setUp() {
        mockApiWrapper = mock(BackblazeApiWrapper.class);
        bucketId = "4a48fe8875c6214145260818";
        authorizeResponse = new AuthorizeResponse();
        authorizeResponse.accountId = "accountId";
        authorizeResponse.absoluteMinimumPartSize = 100;
        authorizeResponse.apiUrl = "https://api001.backblazeb2.com";
        authorizeResponse.authorizationToken = "token_fristajlo";
        authorizeResponse.downloadUrl = "https://f001.backblazeb2.com";
        authorizeResponse.recommendedPartSize = 100000000;
        getUploadUrlResponse = new GetUploadUrlResponse();
        getUploadUrlResponse.bucketId = bucketId;
        getUploadUrlResponse.uploadUrl = "https://api001.backblazeb2.com";
        getUploadUrlResponse.authorizationToken = "token_fristajlo";
        workDir = Paths.get("workdir");
        relativeFilePath = Paths.get("path", "to", "file.txt");
        destination = "dest";
        upload = new Upload(mockApiWrapper, bucketId, workDir, relativeFilePath, destination, authorizeResponse, getUploadUrlResponse);
    }

    @Test
    public void callShouldReturnTrueOnSuccess() throws Exception {
        UploadFileResponse response = new UploadFileResponse();
        doReturn(Optional.of(response)).when(mockApiWrapper).uploadFile(workDir, relativeFilePath, destination, getUploadUrlResponse);
        Boolean result = upload.call();
        assertThat(result, equalTo(true));
    }

    @Test
    public void callShouldReturnFalseOnFailure() throws Exception {
        doReturn(Optional.empty()).when(mockApiWrapper).uploadFile(workDir, relativeFilePath, destination, getUploadUrlResponse);
        Boolean result = upload.call();
        assertThat(result, equalTo(false));
    }

    @Test
    public void callShouldFetchNewUploadUrlIfEmpty() throws Exception {
        upload = new Upload(mockApiWrapper, bucketId, workDir, relativeFilePath, destination, authorizeResponse, null);
        doReturn(Optional.of(getUploadUrlResponse)).when(mockApiWrapper).getUploadUrl(authorizeResponse, bucketId);
        UploadFileResponse response = new UploadFileResponse();
        doReturn(Optional.of(response)).when(mockApiWrapper).uploadFile(workDir, relativeFilePath, destination, getUploadUrlResponse);
        Boolean result = upload.call();
        assertThat(result, equalTo(true));
        verify(mockApiWrapper).getUploadUrl(authorizeResponse, bucketId);

    }

    @Test
    public void handleErrorsShouldFetchNewUploadUrlOnRequestTimeout() throws Exception {
        upload = new Upload(mockApiWrapper, bucketId, workDir, relativeFilePath, destination, authorizeResponse, null);
        doReturn(Optional.of(getUploadUrlResponse)).when(mockApiWrapper).getUploadUrl(authorizeResponse, bucketId);
        ErrorResponse error = new ErrorResponse();
        error.status = HttpStatus.SC_REQUEST_TIMEOUT;
        upload.handleErrors(error);
        verify(mockApiWrapper).getUploadUrl(authorizeResponse, bucketId);

    }
}