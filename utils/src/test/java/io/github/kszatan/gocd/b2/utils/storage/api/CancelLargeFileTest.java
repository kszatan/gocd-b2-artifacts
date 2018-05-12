/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com> 
 * This file is subject to the license terms in the LICENSE file found in the 
 * top-level directory of this distribution. 
 */

package io.github.kszatan.gocd.b2.utils.storage.api;

import io.github.kszatan.gocd.b2.utils.storage.AuthorizeResponse;
import io.github.kszatan.gocd.b2.utils.storage.CancelLargeFileResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class CancelLargeFileTest {
    private CancelLargeFile cancelLargeFile;
    private BackblazeApiWrapper mockApiWrapper;
    private AuthorizeResponse authorizeResponse;
    private String fileId;

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
        fileId = "4_ze73ede9c9c8412db49f60715_f100b4e93fbae6252_d20150824_m224353_c900_v8881000_t0001";
        cancelLargeFile = new CancelLargeFile(mockApiWrapper, authorizeResponse, fileId);
    }

    @Test
    public void callShouldReturnTrueOnSuccess() throws Exception {
        CancelLargeFileResponse response = new CancelLargeFileResponse();
        doReturn(Optional.of(response)).when(mockApiWrapper).cancelLargeFile(authorizeResponse,  fileId);
        Boolean result = cancelLargeFile.call();
        assertThat(result, equalTo(true));
    }

    @Test
    public void callShouldReturnFalseOnFailure() throws Exception {
        doReturn(Optional.empty()).when(mockApiWrapper).cancelLargeFile(authorizeResponse,  fileId);
        Boolean result = cancelLargeFile.call();
        assertThat(result, equalTo(false));
    }
}