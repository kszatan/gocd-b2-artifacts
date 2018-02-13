/*
 * Copyright (c) 2018. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.storage;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class BackblazeStorageTest {
    private final String bucketName = "bucket_name";
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private BackblazeStorage storage;
    private BackblazeApiWrapper backblazeApiWrapperMock;

    @Before
    public void setUp() {
        backblazeApiWrapperMock = mock(BackblazeApiWrapper.class);
        storage = new BackblazeStorage(bucketName, backblazeApiWrapperMock);
    }

    @Test
    public void authorizeShouldReturnTrueWhenAllApiOperationsSucceed() throws Exception {
        final String accountId = "account_id";
        final String applicationKey = "application_key";
        Optional<AuthorizeResponse> authorizeResponse = Optional.of(new AuthorizeResponse());
        doReturn(authorizeResponse).when(backblazeApiWrapperMock).authorize(accountId, applicationKey);
        Bucket bucket = new Bucket();
        bucket.accountId = accountId;
        bucket.bucketId = "bucket_id";
        bucket.bucketName = bucketName;
        ListBucketsResponse bucketList = new ListBucketsResponse();
        bucketList.buckets = new ArrayList<>();
        bucketList.buckets.add(bucket);
        Optional<ListBucketsResponse> listBucketResponse = Optional.of(bucketList);
        doReturn(listBucketResponse).when(backblazeApiWrapperMock).listBuckets(authorizeResponse.get());
        Optional<GetUploadUrlResponse> getUploadUrlResponse = Optional.of(new GetUploadUrlResponse());
        doReturn(getUploadUrlResponse).when(backblazeApiWrapperMock).getUploadUrl(authorizeResponse.get(), bucket.bucketId);

        Boolean result = storage.authorize(accountId, applicationKey);
        assertThat(result, equalTo(true));
    }

    @Test
    public void authorizeShouldReturnTrueAfterFifthSuccessfulAuthorizeAttemptWhenAllOtherApiOperationsSucceed() throws Exception {
        final String accountId = "account_id";
        final String applicationKey = "application_key";
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.status = 429;
        errorResponse.message = "Too many requests";
        doReturn(Optional.of(errorResponse)).when(backblazeApiWrapperMock).getLastError();
        Optional<AuthorizeResponse> authorizeResponse = Optional.of(new AuthorizeResponse());
        doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(authorizeResponse).when(backblazeApiWrapperMock).authorize(accountId, applicationKey);
        Bucket bucket = new Bucket();
        bucket.accountId = accountId;
        bucket.bucketId = "bucket_id";
        bucket.bucketName = bucketName;
        ListBucketsResponse bucketList = new ListBucketsResponse();
        bucketList.buckets = new ArrayList<>();
        bucketList.buckets.add(bucket);
        Optional<ListBucketsResponse> listBucketResponse = Optional.of(bucketList);
        doReturn(listBucketResponse).when(backblazeApiWrapperMock).listBuckets(authorizeResponse.get());
        Optional<GetUploadUrlResponse> getUploadUrlResponse = Optional.of(new GetUploadUrlResponse());
        doReturn(getUploadUrlResponse).when(backblazeApiWrapperMock).getUploadUrl(authorizeResponse.get(), bucket.bucketId);

        Boolean result = storage.authorize(accountId, applicationKey);
        assertThat(result, equalTo(true));
    }

    @Test
    public void authorizeShouldReturnFalseAfterFiveUnsuccessfulAuthorizeAttempts() throws Exception {
        final String accountId = "account_id";
        final String applicationKey = "application_key";
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.status = 429;
        errorResponse.message = "Too many requests";
        doReturn(Optional.of(errorResponse)).when(backblazeApiWrapperMock).getLastError();
        doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .when(backblazeApiWrapperMock).authorize(accountId, applicationKey);

        Boolean result = storage.authorize(accountId, applicationKey);
        assertThat(result, equalTo(false));
    }

    @Test
    public void authorizeShouldThrowExceptionWhenLastErrorIsEmptyAfterFailedCall() throws Exception {
        final String accountId = "account_id";
        final String applicationKey = "application_key";
        doReturn(Optional.empty()).when(backblazeApiWrapperMock).getLastError();
        doReturn(Optional.empty())
                .when(backblazeApiWrapperMock).authorize(accountId, applicationKey);

        thrown.expect(StorageException.class);
        storage.authorize(accountId, applicationKey);
    }

    @Test
    public void authorizeShouldThrowStorageExceptionAuthorizeCallThrowsIoException() throws Exception {
        final String accountId = "account_id";
        final String applicationKey = "application_key";
        doThrow(new IOException("read error"))
                .when(backblazeApiWrapperMock).authorize(accountId, applicationKey);

        thrown.expect(StorageException.class);
        thrown.expectCause(IsInstanceOf.instanceOf(IOException.class));
        storage.authorize(accountId, applicationKey);
    }
}