/*
 * Copyright (c) 2018. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage;

import io.github.kszatan.gocd.b2.utils.storage.api.BackblazeApiWrapper;
import org.apache.http.HttpStatus;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

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
    public void checkConnectionShouldReturnSuccessResponseWhenSuccessfullyAuthorized() throws Exception {
        final String accountId = "account_id";
        final String applicationKey = "application_key";
        Optional<AuthorizeResponse> authorizeResponse = Optional.of(new AuthorizeResponse());
        doReturn(authorizeResponse).when(backblazeApiWrapperMock).authorize(accountId, applicationKey);
        ListBucketsResponse listBucketsResponse = new ListBucketsResponse();
        Bucket bucket = new Bucket();
        bucket.name = bucketName;
        listBucketsResponse.buckets.add(bucket);
        Optional<ListBucketsResponse> maybeListBucketsResponse = Optional.of(listBucketsResponse);
        doReturn(maybeListBucketsResponse).when(backblazeApiWrapperMock).listBuckets(authorizeResponse.get());

        Boolean result = storage.checkConnection(accountId, applicationKey);
        assertThat(result, equalTo(true));
    }

    @Test
    public void checkConnectionShouldReturnFailureResponseWhenBucketNotFound() throws Exception {
        final String accountId = "account_id";
        final String applicationKey = "application_key";
        Optional<AuthorizeResponse> authorizeResponse = Optional.of(new AuthorizeResponse());
        doReturn(authorizeResponse).when(backblazeApiWrapperMock).authorize(accountId, applicationKey);
        ListBucketsResponse listBucketsResponse = new ListBucketsResponse();
        Bucket bucket = new Bucket();
        bucket.name = "different bucket name";
        listBucketsResponse.buckets.add(bucket);
        Optional<ListBucketsResponse> maybeListBucketsResponse = Optional.of(listBucketsResponse);
        doReturn(maybeListBucketsResponse).when(backblazeApiWrapperMock).listBuckets(authorizeResponse.get());

        Boolean result = storage.checkConnection(accountId, applicationKey);
        assertThat(result, equalTo(false));
        assertThat(storage.getLastErrorMessage(), equalTo("Bucket 'bucket_name' doesn't exist"));
    }

    @Test
    public void checkConnectionShouldReturnFailureResponseWhenAuthorizeFails() throws Exception {
        final String accountId = "account_id";
        final String applicationKey = "application_key";
        doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .when(backblazeApiWrapperMock).authorize(accountId, applicationKey);
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        errorResponse.message = "Too many requests";
        doReturn(Optional.of(errorResponse)).when(backblazeApiWrapperMock).getLastError();

        Boolean result = storage.checkConnection(accountId, applicationKey);
        assertThat(result, equalTo(false));
    }

    @Test
    public void checkConnectionShouldThrowStorageExceptionWhenAuthorizeCallThrowsIoException() throws Exception {
        final String accountId = "account_id";
        final String applicationKey = "application_key";
        doThrow(new IOException("read error"))
                .when(backblazeApiWrapperMock).authorize(accountId, applicationKey);

        thrown.expect(StorageException.class);
        thrown.expectCause(IsInstanceOf.instanceOf(IOException.class));
        storage.checkConnection(accountId, applicationKey);
    }

    @Test
    public void authorizeShouldReturnTrueWhenAllApiOperationsSucceed() throws Exception {
        final String accountId = "account_id";
        final String applicationKey = "application_key";
        Optional<AuthorizeResponse> authorizeResponse = Optional.of(new AuthorizeResponse());
        doReturn(authorizeResponse).when(backblazeApiWrapperMock).authorize(accountId, applicationKey);
        Bucket bucket = new Bucket();
        bucket.accountId = accountId;
        bucket.id = "bucket_id";
        bucket.name = bucketName;
        ListBucketsResponse bucketList = new ListBucketsResponse();
        bucketList.buckets = new ArrayList<>();
        bucketList.buckets.add(bucket);
        Optional<ListBucketsResponse> listBucketResponse = Optional.of(bucketList);
        doReturn(listBucketResponse).when(backblazeApiWrapperMock).listBuckets(authorizeResponse.get());
        Optional<GetUploadUrlResponse> getUploadUrlResponse = Optional.of(new GetUploadUrlResponse());
        doReturn(getUploadUrlResponse).when(backblazeApiWrapperMock).getUploadUrl(authorizeResponse.get(), bucket.id);

        Boolean result = storage.authorize(accountId, applicationKey);
        assertThat(result, equalTo(true));
    }

    @Test
    public void authorizeShouldReturnTrueAfterFifthSuccessfulAuthorizeAttemptWhenAllOtherApiOperationsSucceed() throws Exception {
        final String accountId = "account_id";
        final String applicationKey = "application_key";
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
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
        bucket.id = "bucket_id";
        bucket.name = bucketName;
        ListBucketsResponse bucketList = new ListBucketsResponse();
        bucketList.buckets = new ArrayList<>();
        bucketList.buckets.add(bucket);
        Optional<ListBucketsResponse> listBucketResponse = Optional.of(bucketList);
        doReturn(listBucketResponse).when(backblazeApiWrapperMock).listBuckets(authorizeResponse.get());
        Optional<GetUploadUrlResponse> getUploadUrlResponse = Optional.of(new GetUploadUrlResponse());
        doReturn(getUploadUrlResponse).when(backblazeApiWrapperMock).getUploadUrl(authorizeResponse.get(), bucket.id);

        Boolean result = storage.authorize(accountId, applicationKey);
        assertThat(result, equalTo(true));
    }

    @Test
    public void authorizeShouldReturnFalseAfterFiveUnsuccessfulAuthorizeAttempts() throws Exception {
        final String accountId = "account_id";
        final String applicationKey = "application_key";
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
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
    public void authorizeShouldReturnFalseAfterFiveUnsuccessfulListBucketsAttempts() throws Exception {
        final String accountId = "account_id";
        final String applicationKey = "application_key";
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        errorResponse.message = "Too many requests";
        doReturn(Optional.of(errorResponse)).when(backblazeApiWrapperMock).getLastError();
        Optional<AuthorizeResponse> authorizeResponse = Optional.of(new AuthorizeResponse());
        doReturn(authorizeResponse).when(backblazeApiWrapperMock).authorize(accountId, applicationKey);
        Bucket bucket = new Bucket();
        bucket.accountId = accountId;
        bucket.id = "bucket_id";
        bucket.name = bucketName;
        ListBucketsResponse bucketList = new ListBucketsResponse();
        bucketList.buckets = new ArrayList<>();
        bucketList.buckets.add(bucket);
        Optional<ListBucketsResponse> listBucketResponse = Optional.of(bucketList);
        doReturn(listBucketResponse).when(backblazeApiWrapperMock).listBuckets(authorizeResponse.get());
        doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .when(backblazeApiWrapperMock).getUploadUrl(authorizeResponse.get(), bucket.id);

        Boolean result = storage.authorize(accountId, applicationKey);
        assertThat(result, equalTo(false));
    }

    @Test
    public void authorizeShouldReturnFalseAfterFiveUnsuccessfulGetUploadUrlAttempts() throws Exception {
        final String accountId = "account_id";
        final String applicationKey = "application_key";
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        errorResponse.message = "Too many requests";
        doReturn(Optional.of(errorResponse)).when(backblazeApiWrapperMock).getLastError();
        Optional<AuthorizeResponse> authorizeResponse = Optional.of(new AuthorizeResponse());
        doReturn(authorizeResponse).when(backblazeApiWrapperMock).authorize(accountId, applicationKey);
        doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .when(backblazeApiWrapperMock).listBuckets(authorizeResponse.get());

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
    public void authorizeShouldThrowStorageExceptionWhenAuthorizeCallThrowsIoException() throws Exception {
        final String accountId = "account_id";
        final String applicationKey = "application_key";
        doThrow(new IOException("read error"))
                .when(backblazeApiWrapperMock).authorize(accountId, applicationKey);

        thrown.expect(StorageException.class);
        thrown.expectCause(IsInstanceOf.instanceOf(IOException.class));
        storage.authorize(accountId, applicationKey);
    }

    @Test
    public void uploadFileShouldThrowNoSuchAlgorithmExceptionWhenShaImplementationIsNotPresent() throws Exception {
        Optional<GetUploadUrlResponse> getUploadUrlResponse = Optional.of(new GetUploadUrlResponse());
        doReturn(getUploadUrlResponse).when(backblazeApiWrapperMock).getUploadUrl(any(), any());
        thrown.expect(StorageException.class);
        thrown.expectCause(IsInstanceOf.instanceOf(NoSuchAlgorithmException.class));
        doThrow(NoSuchAlgorithmException.class).when(backblazeApiWrapperMock).uploadFile(any(), any(), any(), any());
        storage.upload(Paths.get(""), "", "");
    }

    @Test
    public void uploadShouldReturnFalseAfterFiveUnsuccessfulUploadFileAttempts() throws Exception {
        Optional<GetUploadUrlResponse> getUploadUrlResponse = Optional.of(new GetUploadUrlResponse());
        doReturn(getUploadUrlResponse).when(backblazeApiWrapperMock).getUploadUrl(any(), any());
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        errorResponse.message = "Internal Error";
        doReturn(Optional.of(errorResponse)).when(backblazeApiWrapperMock).getLastError();
        doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .when(backblazeApiWrapperMock).uploadFile(any(), any(), any(), any());

        Boolean result = storage.upload(Paths.get(""), "relative/file", "dest");
        assertThat(result, equalTo(false));
    }

    @Test
    public void uploadShouldFetchNewUploadUrlAfterReceivingRequestTimeout() throws Exception {
        final String accountId = "account_id";
        final String applicationKey = "application_key";
        Optional<AuthorizeResponse> authorizeResponse = Optional.of(new AuthorizeResponse());
        doReturn(authorizeResponse).when(backblazeApiWrapperMock).authorize(accountId, applicationKey);
        Bucket bucket = new Bucket();
        bucket.accountId = accountId;
        bucket.id = "bucket_id";
        bucket.name = bucketName;
        ListBucketsResponse bucketList = new ListBucketsResponse();
        bucketList.buckets = new ArrayList<>();
        bucketList.buckets.add(bucket);
        Optional<ListBucketsResponse> listBucketResponse = Optional.of(bucketList);
        doReturn(listBucketResponse).when(backblazeApiWrapperMock).listBuckets(authorizeResponse.get());
        Optional<GetUploadUrlResponse> getUploadUrlResponse = Optional.of(new GetUploadUrlResponse());
        doReturn(getUploadUrlResponse).when(backblazeApiWrapperMock).getUploadUrl(authorizeResponse.get(), bucket.id);
        storage.authorize(accountId, applicationKey);
        reset(backblazeApiWrapperMock);

        doReturn(getUploadUrlResponse).when(backblazeApiWrapperMock).getUploadUrl(any(), any());

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.status = HttpStatus.SC_REQUEST_TIMEOUT;
        errorResponse.message = "Request Timeout";
        doReturn(Optional.of(errorResponse)).when(backblazeApiWrapperMock).getLastError();
        doReturn(Optional.empty()).doReturn(Optional.of(new UploadFileResponse()))
                .when(backblazeApiWrapperMock).uploadFile(any(), any(), any(), any());

        Boolean result = storage.upload(Paths.get(""), "relative/file", "dest");
        assertThat(result, equalTo(true));
        verify(backblazeApiWrapperMock).getUploadUrl(any(), eq("bucket_id"));
    }

    @Test
    public void uploadShouldThrowStorageExceptionWhenUploadFileCallThrowsIoException() throws Exception {
        Optional<GetUploadUrlResponse> getUploadUrlResponse = Optional.of(new GetUploadUrlResponse());
        doReturn(getUploadUrlResponse).when(backblazeApiWrapperMock).getUploadUrl(any(), any());
        doThrow(new IOException("read error"))
                .when(backblazeApiWrapperMock).uploadFile(any(), any(), any(), any());

        thrown.expect(StorageException.class);
        thrown.expectCause(IsInstanceOf.instanceOf(IOException.class));
        storage.upload(Paths.get(""), "relative/file", "dest");
    }
}