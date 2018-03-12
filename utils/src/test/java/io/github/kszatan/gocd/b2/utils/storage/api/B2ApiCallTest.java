/*
 * Copyright (c) 2018. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage.api;

import io.github.kszatan.gocd.b2.utils.storage.ErrorResponse;
import io.github.kszatan.gocd.b2.utils.storage.StorageException;
import io.github.kszatan.gocd.b2.utils.storage.UnauthorizedCallException;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class B2ApiCallTest {
    private BackblazeApiWrapper apiWrapper;
    private B2ApiCall.ThreadSleeper mockSleeper;
    private B2ApiCall b2ApiCall;
    private final String callName = "ApiCall";
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        apiWrapper = mock(BackblazeApiWrapper.class);
        mockSleeper = mock(B2ApiCall.ThreadSleeper.class);
        b2ApiCall = new B2ApiCall(callName, apiWrapper, mockSleeper) {
            @Override
            public Boolean call() throws StorageException {
                return true;
            }
        };
    }

    @Test
    public void getNameShouldReturnCallName() {
        assertThat(b2ApiCall.getName(), equalTo(callName));
    }

    @Test
    public void handleErrorShouldThrowOnBadRequest() throws Exception {
        thrown.expect(instanceOf(StorageException.class));
        ErrorResponse error = new ErrorResponse();
        error.message = "Bad Request";
        error.status = HttpStatus.SC_BAD_REQUEST;
        b2ApiCall.handleErrors(error);
    }

    @Test
    public void handleErrorShouldThrownUnauthorizedCallOnUnauthorized() throws Exception {
        thrown.expect(instanceOf(UnauthorizedCallException.class));
        ErrorResponse error = new ErrorResponse();
        error.message = "Unauthorized";
        error.status = HttpStatus.SC_UNAUTHORIZED;
        b2ApiCall.handleErrors(error);
    }

    @Test
    public void handleErrorShouldThrowOnForbidden() throws Exception {
        thrown.expect(instanceOf(StorageException.class));
        ErrorResponse error = new ErrorResponse();
        error.message = "Forbidden";
        error.status = HttpStatus.SC_FORBIDDEN;
        b2ApiCall.handleErrors(error);
    }

    @Test
    public void handleErrorShouldDoNothingOnRequestTimeout() throws Exception {
        ErrorResponse error = new ErrorResponse();
        error.message = "Request Timeout";
        error.status = HttpStatus.SC_REQUEST_TIMEOUT;
        b2ApiCall.handleErrors(error);
    }

    @Test
    public void handleErrorShouldSleepFiveSecsOnTooManyRequests() throws Exception {
        ErrorResponse error = new ErrorResponse();
        error.message = "Too many Requests";
        error.status = 429;
        b2ApiCall.handleErrors(error);
        verify(mockSleeper).sleep(5);
    }

    @Test
    public void handleErrorShouldDoNothingOnInternalServerError() throws Exception {
        ErrorResponse error = new ErrorResponse();
        error.message = "Server Error";
        error.status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        b2ApiCall.handleErrors(error);
    }

    @Test
    public void handleErrorShouldSleepWithExponentialBackoffOnServiceUnavailable() throws Exception {
        ErrorResponse error = new ErrorResponse();
        error.message = "Service Unavailable";
        error.status = HttpStatus.SC_SERVICE_UNAVAILABLE;
        b2ApiCall.handleErrors(error);
        verify(mockSleeper).sleep(1);
        b2ApiCall.handleErrors(error);
        verify(mockSleeper).sleep(2);
        b2ApiCall.handleErrors(error);
        verify(mockSleeper).sleep(4);
        b2ApiCall.handleErrors(error);
        verify(mockSleeper).sleep(8);
        b2ApiCall.handleErrors(error);
        verify(mockSleeper).sleep(16);
        b2ApiCall.handleErrors(error);
        verify(mockSleeper).sleep(32);
        b2ApiCall.handleErrors(error);
        verify(mockSleeper).sleep(64);
    }

    @Test
    public void handleErrorShouldThrowIfBackoffIsLongerThan65Seconds() throws Exception {
        thrown.expect(StorageException.class);
        thrown.expectMessage("Service Unavailable");
        ErrorResponse error = new ErrorResponse();
        error.message = "Service Unavailable";
        error.status = HttpStatus.SC_SERVICE_UNAVAILABLE;
        b2ApiCall.handleErrors(error);
        b2ApiCall.handleErrors(error);
        b2ApiCall.handleErrors(error);
        b2ApiCall.handleErrors(error);
        b2ApiCall.handleErrors(error);
        b2ApiCall.handleErrors(error);
        b2ApiCall.handleErrors(error);
        b2ApiCall.handleErrors(error);
    }

    @Test
    public void handleErrorShouldSleepRetryAfterAmountOfSeconds() throws Exception {
        ErrorResponse error = new ErrorResponse();
        error.message = "Service Unavailable";
        error.status = HttpStatus.SC_SERVICE_UNAVAILABLE;
        error.retryAfter = 6;
        b2ApiCall.handleErrors(error);
        verify(mockSleeper).sleep(error.retryAfter);
    }
}