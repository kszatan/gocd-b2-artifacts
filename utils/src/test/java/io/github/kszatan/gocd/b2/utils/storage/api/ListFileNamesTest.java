/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage.api;

import io.github.kszatan.gocd.b2.utils.storage.AuthorizeResponse;
import io.github.kszatan.gocd.b2.utils.storage.ListFileNamesParams;
import io.github.kszatan.gocd.b2.utils.storage.ListFileNamesResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ListFileNamesTest {
    private ListFileNames listFileNames;
    private BackblazeApiWrapper mockApiWrapper;
    private AuthorizeResponse authorizeResponse;
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
        bucketId = "bucketId";
        listFileNames = new ListFileNames(mockApiWrapper, authorizeResponse, bucketId, "", "", "");
    }

    @Test
    public void callShouldReturnTrueOnSuccess() throws Exception {
        ListFileNamesResponse response = new ListFileNamesResponse();
        doReturn(Optional.of(response)).when(mockApiWrapper).listFileNames(eq(authorizeResponse), any(ListFileNamesParams.class));
        Boolean result = listFileNames.call();
        assertThat(result, equalTo(true));
    }

    @Test
    public void callShouldReturnFalseOnFailure() throws Exception {
        doReturn(Optional.empty()).when(mockApiWrapper).listFileNames(eq(authorizeResponse), any(ListFileNamesParams.class));
        Boolean result = listFileNames.call();
        assertThat(result, equalTo(false));
    }
}