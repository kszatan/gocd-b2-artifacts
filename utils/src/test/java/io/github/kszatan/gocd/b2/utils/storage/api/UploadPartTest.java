/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com> 
 * This file is subject to the license terms in the LICENSE file found in the 
 * top-level directory of this distribution. 
 */

package io.github.kszatan.gocd.b2.utils.storage.api;

import io.github.kszatan.gocd.b2.utils.storage.AuthorizeResponse;
import io.github.kszatan.gocd.b2.utils.storage.ErrorResponse;
import io.github.kszatan.gocd.b2.utils.storage.GetUploadPartUrlResponse;
import io.github.kszatan.gocd.b2.utils.storage.UploadPartResponse;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class UploadPartTest {
    private UploadPart uploadPart;
    private BackblazeApiWrapper mockApiWrapper;
    private byte[] filePart;
    private Integer partLength;
    private Integer partNumber;
    private String fileId;
    private AuthorizeResponse authorizeResponse;
    private GetUploadPartUrlResponse getUploadPartUrlResponse;

    @Before
    public void setUp() {
        mockApiWrapper = mock(BackblazeApiWrapper.class);
        filePart = "This is a buffer".getBytes();
        partLength = filePart.length;
        partNumber = 1;
        fileId = "4_z27c88f1d182b150646ff0b16_f1004ba650fe24e6b_d20150809_m012853_c100_v0009990_t0000";
        authorizeResponse = new AuthorizeResponse();
        authorizeResponse.accountId = "accountId";
        authorizeResponse.absoluteMinimumPartSize = 100;
        authorizeResponse.apiUrl = "https://api001.backblazeb2.com";
        authorizeResponse.authorizationToken = "token_fristajlo";
        authorizeResponse.downloadUrl = "https://f001.backblazeb2.com";
        authorizeResponse.recommendedPartSize = 100000000;
        getUploadPartUrlResponse = new GetUploadPartUrlResponse();
        getUploadPartUrlResponse.fileId = fileId;
        getUploadPartUrlResponse.uploadUrl = "https://api001.backblazeb2.com";
        getUploadPartUrlResponse.authorizationToken = "token_fristajlo";
        uploadPart = new UploadPart(mockApiWrapper, filePart, partLength, partNumber, authorizeResponse, getUploadPartUrlResponse);
    }

    @Test
    public void callShouldReturnTrueOnSuccess() throws Exception {
        UploadPartResponse response = new UploadPartResponse();
        doReturn(Optional.of(response)).when(mockApiWrapper).uploadPart(filePart, partLength, partNumber, getUploadPartUrlResponse);
        Boolean result = uploadPart.call();
        assertThat(result, equalTo(true));
    }

    @Test
    public void callShouldReturnFalseOnFailure() throws Exception {
        doReturn(Optional.empty()).when(mockApiWrapper).uploadPart(filePart, partLength, partNumber, getUploadPartUrlResponse);
        Boolean result = uploadPart.call();
        assertThat(result, equalTo(false));
    }

    @Test
    public void handleErrorsShouldFetchNewUploadPartUrlOnRequestTimeout() throws Exception {
        uploadPart = new UploadPart(mockApiWrapper, filePart, partLength, partNumber, authorizeResponse, getUploadPartUrlResponse);
        doReturn(Optional.of(getUploadPartUrlResponse)).when(mockApiWrapper).getUploadPartUrl(authorizeResponse, fileId);
        ErrorResponse error = new ErrorResponse();
        error.status = HttpStatus.SC_REQUEST_TIMEOUT;
        uploadPart.handleErrors(error);
        verify(mockApiWrapper).getUploadPartUrl(authorizeResponse, fileId);

    }
}