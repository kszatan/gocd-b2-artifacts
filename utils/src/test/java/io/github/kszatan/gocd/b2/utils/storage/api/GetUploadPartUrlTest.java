/*
 * Copyright (c) 2018. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage.api;

import io.github.kszatan.gocd.b2.utils.storage.AuthorizeResponse;
import io.github.kszatan.gocd.b2.utils.storage.GetUploadPartUrlResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class GetUploadPartUrlTest {
    private GetUploadPartUrl getUploadPartUrl;
    private BackblazeApiWrapper mockApiWrapper;
    private AuthorizeResponse authorizeResponse;
    private String fileId = "4_z27c88f1d182b150646ff0b16_f1004ba650fe24e6b_d20150809_m012853_c100_v0009990_t0000";

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
        getUploadPartUrl = new GetUploadPartUrl(mockApiWrapper, authorizeResponse, fileId);
    }

    @Test
    public void callShouldReturnTrueOnSuccess() throws Exception {
        GetUploadPartUrlResponse response = new GetUploadPartUrlResponse();
        doReturn(Optional.of(response)).when(mockApiWrapper).getUploadPartUrl(authorizeResponse, fileId);
        Boolean result = getUploadPartUrl.call();
        assertThat(result, equalTo(true));
    }

    @Test
    public void callShouldReturnFalseOnFailure() throws Exception {
        doReturn(Optional.empty()).when(mockApiWrapper).getUploadPartUrl(authorizeResponse, fileId);
        Boolean result = getUploadPartUrl.call();
        assertThat(result, equalTo(false));
    }
}