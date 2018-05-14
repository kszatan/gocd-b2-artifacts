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
import java.nio.file.Path;
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
    private String testFilePath;

    @Before
    public void setUp() {
        backblazeApiWrapperMock = mock(BackblazeApiWrapper.class);
        storage = new BackblazeStorage(bucketName, backblazeApiWrapperMock);
        testFilePath = this.getClass().getResource("UploadFileTest.txt").getPath();
    }

    @Test
    public void listFilesShouldReturnNonEmptyResponseWhenNoErrors() throws Exception {
        ListFileNamesParams params = new ListFileNamesParams();
        params.startFileName = "files/hello.txt";
        params.prefix = "files/";
        params.delimiter = "/";
        params.maxFileCount = 1000;

        ListFileNamesResponse listFileNamesResponse = new ListFileNamesResponse();
        FileName fileName = new FileName();
        fileName.fileName = "files/world.txt";
        fileName.fileId = "4_z27c88f1d182b150646ff0b16_f1004ba650fe24e6b_d20150809_m012853_c100_v0009990_t0000";
        fileName.action = "upload";
        fileName.uploadTimestamp = 1439083733000L;
        fileName.contentLength = 6;
        listFileNamesResponse.fileNames.add(fileName);
        listFileNamesResponse.nextFileName = "next file";
        Optional<ListFileNamesResponse> maybeListFileNamesResponse = Optional.of(listFileNamesResponse);
        AuthorizeResponse authorizeResponse = new AuthorizeResponse();
        authorize(authorizeResponse);
        doReturn(maybeListFileNamesResponse).when(backblazeApiWrapperMock).listFileNames(
                eq(authorizeResponse), any(ListFileNamesParams.class));

        Optional<ListFileNamesResponse> maybeResponse = storage.listFiles(params.startFileName, params.prefix, params.delimiter);

        assertThat(maybeResponse.isPresent(), equalTo(true));
        ListFileNamesResponse response = maybeResponse.get();
        assertThat(response.nextFileName, equalTo("next file"));
        assertThat(response.fileNames.size(), equalTo(1));
        FileName firstFileName = response.fileNames.get(0);
        assertThat(firstFileName, equalTo(fileName));
    }

    @Test
    public void listFilesShouldThrowExceptionWhenUnauthorized() throws Exception {
        final String startFileName = "files/hello.txt";
        final String prefix = "files/";
        final String delimiter = "/";

        thrown.expect(StorageException.class);
        Optional<ListFileNamesResponse> maybeResponse = storage.listFiles(startFileName, prefix, delimiter);
    }

    @Test
    public void listFilesShouldThrowStorageExceptionWhenAuthorizeCallThrowsIoException() throws Exception {
        final String startFileName = "files/hello.txt";
        final String prefix = "files/";
        final String delimiter = "/";
        
        AuthorizeResponse authorizeResponse = new AuthorizeResponse();
        authorize(authorizeResponse);

        doThrow(new IOException("read error"))
                .when(backblazeApiWrapperMock).listFileNames(any(), any(ListFileNamesParams.class));
        thrown.expect(StorageException.class);
        thrown.expectCause(IsInstanceOf.instanceOf(IOException.class));

        storage.listFiles(startFileName, prefix, delimiter);
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
        assertThat(storage.getLastErrorMessage(), equalTo("Bucket 'bucket_name' not found"));
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
    public void authorizeShouldUsePreviouslySetCredentials() throws Exception {
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

        storage.setCredentials(accountId, applicationKey);
        storage.authorize();
        verify(backblazeApiWrapperMock, times(5)).authorize(accountId, applicationKey);
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

        storage.setCredentials(accountId, applicationKey);
        Boolean result = storage.authorize();
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

        storage.setCredentials(accountId, applicationKey);
        Boolean result = storage.authorize();
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

        storage.setCredentials(accountId, applicationKey);
        Boolean result = storage.authorize();
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
        doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .when(backblazeApiWrapperMock).listBuckets(authorizeResponse.get());

        storage.setCredentials(accountId, applicationKey);
        Boolean result = storage.authorize();
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
        storage.setCredentials(accountId, applicationKey);
        storage.authorize();
    }

    @Test
    public void authorizeShouldThrowStorageExceptionWhenAuthorizeCallThrowsIoException() throws Exception {
        final String accountId = "account_id";
        final String applicationKey = "application_key";
        doThrow(new IOException("read error"))
                .when(backblazeApiWrapperMock).authorize(accountId, applicationKey);

        thrown.expect(StorageException.class);
        thrown.expectCause(IsInstanceOf.instanceOf(IOException.class));
        storage.setCredentials(accountId, applicationKey);
        storage.authorize();
    }

    @Test
    public void uploadFileShouldThrowIfItCannotGetUploadUrl() throws Exception {
        AuthorizeResponse authorizeResponse = new AuthorizeResponse();
        authorizeResponse.recommendedPartSize = 100000;
        authorize(authorizeResponse);
        doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .when(backblazeApiWrapperMock).getUploadUrl(any(), any());
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        errorResponse.message = "Too many requests";
        doReturn(Optional.of(errorResponse)).when(backblazeApiWrapperMock).getLastError();
        thrown.expect(StorageException.class);
        storage.upload(Paths.get(""), "", "");
    }

    @Test
    public void uploadFileShouldThrowNoSuchAlgorithmExceptionWhenShaImplementationIsNotPresent() throws Exception {
        AuthorizeResponse authorizeResponse = new AuthorizeResponse();
        authorizeResponse.recommendedPartSize = 100000;
        authorize(authorizeResponse);
        Optional<GetUploadUrlResponse> getUploadUrlResponse = Optional.of(new GetUploadUrlResponse());
        doReturn(getUploadUrlResponse).when(backblazeApiWrapperMock).getUploadUrl(any(), any());
        thrown.expect(StorageException.class);
        thrown.expectCause(IsInstanceOf.instanceOf(NoSuchAlgorithmException.class));
        doThrow(NoSuchAlgorithmException.class).when(backblazeApiWrapperMock).uploadFile(any(), any(), any(), any());
        storage.upload(Paths.get(""), "", "");
    }

    @Test
    public void uploadShouldThrowAfterFiveUnsuccessfulUploadFileAttempts() throws Exception {
        AuthorizeResponse authorizeResponse = new AuthorizeResponse();
        authorizeResponse.recommendedPartSize = 100000;
        authorize(authorizeResponse);
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

        thrown.expect(StorageException.class);
        storage.upload(Paths.get(""), testFilePath, "dest");
    }

    @Test
    public void uploadShouldFetchNewUploadUrlAfterReceivingRequestTimeout() throws Exception {
        AuthorizeResponse authorizeResponse = new AuthorizeResponse();
        authorizeResponse.recommendedPartSize = 100000;
        authorize(authorizeResponse);

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.status = HttpStatus.SC_REQUEST_TIMEOUT;
        errorResponse.message = "Request Timeout";
        doReturn(Optional.of(errorResponse)).when(backblazeApiWrapperMock).getLastError();
        Optional<GetUploadUrlResponse> getUploadUrlResponse = Optional.of(new GetUploadUrlResponse());
        doReturn(getUploadUrlResponse).when(backblazeApiWrapperMock).getUploadUrl(eq(authorizeResponse), any());
        doReturn(Optional.empty()).doReturn(Optional.of(new UploadFileResponse()))
                .when(backblazeApiWrapperMock).uploadFile(any(), any(), any(), any());

        storage.upload(Paths.get(""), testFilePath, "dest");
        verify(backblazeApiWrapperMock, times(2)).getUploadUrl(any(), eq("bucket_id"));
    }

    @Test
    public void uploadShouldThrowStorageExceptionWhenUploadFileCallThrowsIoException() throws Exception {
        authorize(new AuthorizeResponse());
        Optional<GetUploadUrlResponse> getUploadUrlResponse = Optional.of(new GetUploadUrlResponse());
        doReturn(getUploadUrlResponse).when(backblazeApiWrapperMock).getUploadUrl(any(), any());
        doThrow(new IOException("read error"))
                .when(backblazeApiWrapperMock).uploadFile(any(), any(), any(), any());

        thrown.expect(StorageException.class);
        thrown.expectCause(IsInstanceOf.instanceOf(IOException.class));
        storage.upload(Paths.get(""), "relative/file", "dest");
    }

    @Test
    public void uploadShouldUseNormalUploadMethodIfFileSizeSmallerThanRecommendedPartSize() throws Exception {
        AuthorizeResponse authorizeResponse = new AuthorizeResponse();
        authorizeResponse.recommendedPartSize = 100000;
        authorize(authorizeResponse);
        doReturn(Optional.of(new GetUploadUrlResponse()))
                .when(backblazeApiWrapperMock).getUploadUrl(any(), any());

        doReturn(Optional.of(new UploadFileResponse()))
                .when(backblazeApiWrapperMock).uploadFile(any(), any(), any(), any());

        Path workDir = Paths.get("");
        storage.upload(workDir, testFilePath, "dest");
        verify(backblazeApiWrapperMock).uploadFile(eq(workDir), eq(testFilePath), eq("dest"), any());
    }

    @Test
    public void uploadShouldUseUploadLargeFiledMethodIfFileSizeBiggerThanRecommendedPartSize() throws Exception {
        AuthorizeResponse authorizeResponse = new AuthorizeResponse();
        authorizeResponse.recommendedPartSize = 10;
        authorize(authorizeResponse);

        doReturn(Optional.of(new StartLargeFileResponse()))
                .when(backblazeApiWrapperMock).startLargeFile(any(), any(), any());
        doReturn(Optional.of(new GetUploadPartUrlResponse()))
                .when(backblazeApiWrapperMock).getUploadPartUrl(any(), any());
        doReturn(Optional.of(new UploadPartResponse()))
                .when(backblazeApiWrapperMock).uploadPart(any(), any(), any(), any());
        doReturn(Optional.of(new FinishLargeFileResponse()))
                .when(backblazeApiWrapperMock).finishLargeFile(any(), any(), any());

        Path workDir = Paths.get("");
        storage.upload(workDir, testFilePath, "dest");
        verify(backblazeApiWrapperMock).startLargeFile(any(), any(), any());
        verify(backblazeApiWrapperMock).getUploadPartUrl(any(), any());
        verify(backblazeApiWrapperMock, times(3)).uploadPart(any(), any(), any(), any());
        verify(backblazeApiWrapperMock).finishLargeFile(any(), any(), any());
    }

    @Test
    public void uploadLargeFileShouldThrowIfStartLargeFileFails() throws Exception {
        AuthorizeResponse authorizeResponse = new AuthorizeResponse();
        authorizeResponse.recommendedPartSize = 10;
        authorize(authorizeResponse);
        
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        errorResponse.message = "Internal Error";
        doReturn(Optional.of(errorResponse)).when(backblazeApiWrapperMock).getLastError();

        doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .when(backblazeApiWrapperMock).startLargeFile(any(), any(), any());

        thrown.expect(StorageException.class);
        storage.upload(Paths.get(""), testFilePath, "dest");
    }

    @Test
    public void uploadLargeFileShouldThrowIfGetUploadPartUrlFails() throws Exception {
        AuthorizeResponse authorizeResponse = new AuthorizeResponse();
        authorizeResponse.recommendedPartSize = 10;
        authorize(authorizeResponse);

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        errorResponse.message = "Internal Error";
        doReturn(Optional.of(errorResponse)).when(backblazeApiWrapperMock).getLastError();

        final String fileId = "file_id";
        StartLargeFileResponse startLargeFileResponse = new StartLargeFileResponse();
        startLargeFileResponse.fileId = fileId;
        doReturn(Optional.of(startLargeFileResponse))
                .when(backblazeApiWrapperMock).startLargeFile(any(), any(), any());
        doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .when(backblazeApiWrapperMock).getUploadPartUrl(any(), any());
        doReturn(Optional.of(new CancelLargeFileResponse())).
                when(backblazeApiWrapperMock).cancelLargeFile(any(), eq(fileId));

        thrown.expect(StorageException.class);
        storage.upload(Paths.get(""), testFilePath, "dest");
        verify(backblazeApiWrapperMock).cancelLargeFile(any(), eq(fileId));
    }

    @Test
    public void uploadLargeFileShouldThrowIfUploadPartFails() throws Exception {
        AuthorizeResponse authorizeResponse = new AuthorizeResponse();
        authorizeResponse.recommendedPartSize = 10;
        authorize(authorizeResponse);

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        errorResponse.message = "Internal Error";
        doReturn(Optional.of(errorResponse)).when(backblazeApiWrapperMock).getLastError();

        final String fileId = "file_id";
        StartLargeFileResponse startLargeFileResponse = new StartLargeFileResponse();
        startLargeFileResponse.fileId = fileId;
        doReturn(Optional.of(startLargeFileResponse))
                .when(backblazeApiWrapperMock).startLargeFile(any(), any(), any());
        doReturn(Optional.of(new GetUploadPartUrlResponse()))
                .when(backblazeApiWrapperMock).getUploadPartUrl(any(), any());
        doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .when(backblazeApiWrapperMock).uploadPart(any(), any(), any(), any());
        doReturn(Optional.of(new CancelLargeFileResponse())).
                when(backblazeApiWrapperMock).cancelLargeFile(any(), eq(fileId));

        thrown.expect(StorageException.class);
        storage.upload(Paths.get(""), testFilePath, "dest");
        verify(backblazeApiWrapperMock).cancelLargeFile(any(), eq(fileId));
    }

    @Test
    public void uploadLargeFileShouldReturnFalseIfFinishLargeFileFails() throws Exception {
        AuthorizeResponse authorizeResponse = new AuthorizeResponse();
        authorizeResponse.recommendedPartSize = 10;
        authorize(authorizeResponse);

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        errorResponse.message = "Internal Error";
        doReturn(Optional.of(errorResponse)).when(backblazeApiWrapperMock).getLastError();

        final String fileId = "file_id";
        StartLargeFileResponse startLargeFileResponse = new StartLargeFileResponse();
        startLargeFileResponse.fileId = fileId;
        doReturn(Optional.of(startLargeFileResponse))
                .when(backblazeApiWrapperMock).startLargeFile(any(), any(), any());
        doReturn(Optional.of(new GetUploadPartUrlResponse()))
                .when(backblazeApiWrapperMock).getUploadPartUrl(any(), any());
        doReturn(Optional.of(new UploadPartResponse()))
                .when(backblazeApiWrapperMock).uploadPart(any(), any(), any(), any());
        doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .when(backblazeApiWrapperMock).finishLargeFile(any(), any(), any());
        doReturn(Optional.of(new CancelLargeFileResponse())).
                when(backblazeApiWrapperMock).cancelLargeFile(any(), eq(fileId));

        thrown.expect(StorageException.class);
        storage.upload(Paths.get(""), testFilePath, "dest");
        verify(backblazeApiWrapperMock).cancelLargeFile(any(), eq(fileId));
    }

    @Test
    public void downloadShouldReturnFalseAfterFiveUnsuccessfulUploadFileAttempts() throws Exception {
        authorize(new AuthorizeResponse());
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        errorResponse.message = "Internal Error";
        doReturn(Optional.of(errorResponse)).when(backblazeApiWrapperMock).getLastError();
        doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .when(backblazeApiWrapperMock).downloadFileByName(any(), any(), any(), any());

        Boolean result = storage.download("dir1/fileName.txt", Paths.get(System.getProperty("java.io.tmpdir")));
        assertThat(result, equalTo(false));
    }

    @Test
    public void downloadShouldThrowStorageExceptionWhenUploadFileCallThrowsIoException() throws Exception {
        authorize(new AuthorizeResponse());
        doThrow(new IOException("read error"))
                .when(backblazeApiWrapperMock).downloadFileByName(any(), any(), any(), any());

        thrown.expect(StorageException.class);
        thrown.expectCause(IsInstanceOf.instanceOf(IOException.class));
        storage.download("dir1/fileName.txt", Paths.get(System.getProperty("java.io.tmpdir")));
    }

    @Test
    public void storageShouldTryToAuthorizeWhenApiCallThrowsUnauthorized() throws Exception {
        final String startFileName = "files/hello.txt";
        final String prefix = "files/";
        final String delimiter = "/";

        AuthorizeResponse authorizeResponse = new AuthorizeResponse();
        authorize(authorizeResponse);

        doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .doReturn(Optional.empty())
                .when(backblazeApiWrapperMock).listFileNames(any(), any(ListFileNamesParams.class));
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.status = HttpStatus.SC_UNAUTHORIZED;
        errorResponse.message = "Unauthorized";
        doReturn(Optional.of(errorResponse)).when(backblazeApiWrapperMock).getLastError();

        final String accountId = "account_id";
        final String applicationKey = "application_key";
        doReturn(Optional.of(authorizeResponse)).when(backblazeApiWrapperMock).authorize(accountId, applicationKey);
        Bucket bucket = new Bucket();
        bucket.accountId = accountId;
        bucket.id = "bucket_id";
        bucket.name = bucketName;
        ListBucketsResponse bucketList = new ListBucketsResponse();
        bucketList.buckets = new ArrayList<>();
        bucketList.buckets.add(bucket);
        Optional<ListBucketsResponse> listBucketResponse = Optional.of(bucketList);
        doReturn(listBucketResponse).when(backblazeApiWrapperMock).listBuckets(authorizeResponse);

        storage.setCredentials(accountId, applicationKey);
        storage.listFiles(startFileName, prefix, delimiter);
        verify(backblazeApiWrapperMock, times(5)).authorize(accountId, applicationKey);
    }

    private void authorize(AuthorizeResponse authorizeResponse) throws Exception {
        final String accountId = "account_id";
        final String applicationKey = "application_key";
        Optional<AuthorizeResponse> maybeAuthorizeResponse = Optional.of(authorizeResponse);
        doReturn(maybeAuthorizeResponse).when(backblazeApiWrapperMock).authorize(accountId, applicationKey);
        Bucket bucket = new Bucket();
        bucket.accountId = accountId;
        bucket.id = "bucket_id";
        bucket.name = bucketName;
        ListBucketsResponse bucketList = new ListBucketsResponse();
        bucketList.buckets = new ArrayList<>();
        bucketList.buckets.add(bucket);
        Optional<ListBucketsResponse> listBucketResponse = Optional.of(bucketList);
        doReturn(listBucketResponse).when(backblazeApiWrapperMock).listBuckets(maybeAuthorizeResponse.get());
        Optional<GetUploadUrlResponse> getUploadUrlResponse = Optional.of(new GetUploadUrlResponse());
        doReturn(getUploadUrlResponse).when(backblazeApiWrapperMock).getUploadUrl(maybeAuthorizeResponse.get(), bucket.id);

        storage.setCredentials(accountId, applicationKey);
        Boolean result = storage.authorize();
        assertThat(result, equalTo(true));
        reset(backblazeApiWrapperMock);
    }
}