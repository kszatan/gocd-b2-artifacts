/*
 * Copyright (c) 2018. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage.api;

import io.github.kszatan.gocd.b2.utils.storage.AuthorizeResponse;
import io.github.kszatan.gocd.b2.utils.storage.ErrorResponse;
import io.github.kszatan.gocd.b2.utils.storage.StorageException;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class AuthorizeTest {
    private Authorize authorize;
    private BackblazeApiWrapper mockApiWrapper;
    private final String accountId = "accountId";
    private final String applicationKey = "applicationKey";
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        mockApiWrapper = mock(BackblazeApiWrapper.class);
        authorize = new Authorize(mockApiWrapper, accountId, applicationKey);
    }

    @Test
    public void callShouldReturnTrueOnSuccess() throws Exception {
        AuthorizeResponse response = new AuthorizeResponse();
        doReturn(Optional.of(response)).when(mockApiWrapper).authorize(accountId, applicationKey);
        Boolean result = authorize.call();
        assertThat(result, equalTo(true));
    }
    
    @Test
    public void callShouldReturnFalseOnFailure() throws Exception {
        doReturn(Optional.empty()).when(mockApiWrapper).authorize(accountId, applicationKey);
        Boolean result = authorize.call();
        assertThat(result, equalTo(false));
    }

    @Test
    public void handleErrorsShouldThrowOnUnauthorized() throws Exception {
        thrown.expect(StorageException.class);
        ErrorResponse error = new ErrorResponse();
        error.status = HttpStatus.SC_UNAUTHORIZED;
        authorize.handleErrors(error);
    }
}