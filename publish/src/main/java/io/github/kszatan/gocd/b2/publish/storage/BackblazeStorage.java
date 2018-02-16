/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.storage;

import com.thoughtworks.go.plugin.api.logging.Logger;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BackblazeStorage implements Storage {
    @FunctionalInterface
    private interface B2ApiCall {
        Boolean call() throws IOException, GeneralSecurityException;
    }
    private static final Integer MAX_RETRY_ATTEMPTS = 5;
    private static final Integer MAX_BACKOFF_SEC = 64;

    private Logger logger = Logger.getLoggerFor(BackblazeStorage.class);

    private String errorMessage = "";
    private String bucketName;
    private Integer backoffSec = 1;
    private BackblazeApiWrapper backblazeApiWrapper;
    private AuthorizeResponse authorizeResponse;
    private GetUploadUrlResponse getUploadUrlResponse;
    private ListBucketsResponse listBucketsResponse;
    private List<ProgressObserver> progressObservers = new ArrayList<>();

    public BackblazeStorage(String bucketName) {
        this.bucketName = bucketName;
        this.backblazeApiWrapper = new BackblazeApiWrapper();
    }

    public BackblazeStorage(String bucketName, BackblazeApiWrapper backblazeApiWrapper) {
        this.bucketName = bucketName;
        this.backblazeApiWrapper = backblazeApiWrapper;
    }

    @Override
    public void addProgressObserver(ProgressObserver observer) {
        progressObservers.add(observer);
    }

    @Override
    public String getLastErrorMessage() {
        return errorMessage;
    }

    @Override
    public Boolean authorize(String accountId, String applicationKey) throws StorageException {
        try {
            if (!attempt(MAX_RETRY_ATTEMPTS, "authorize", () -> tryAuthorize(accountId, applicationKey))) {
                errorMessage = "Failed to authorize: maximum number of retry attempts reached.";
                return false;
            }
            if (!attempt(MAX_RETRY_ATTEMPTS, "list buckets", () -> tryListBuckets(authorizeResponse))) {
                errorMessage = "Failed to list buckets: maximum number of retry attempts reached.";
                return false;
            }
            Optional<Bucket> maybeBucket = getBucketId(bucketName);
            if (!maybeBucket.isPresent()) {
                errorMessage = "Bucket '" + bucketName + "' doesn't exist";
                return false;
            }
            Bucket bucket = maybeBucket.get();
            if (!attempt(MAX_RETRY_ATTEMPTS, "get upload url", () -> tryGetUploadUrl(authorizeResponse, bucket.bucketId))) {
                errorMessage = "Failed to get upload url: maximum number of retry attempts reached";
                return false;
            }
        } catch (GeneralSecurityException | IOException e) {
            authorizeResponse = null;
            logger.info("authorize error: " + e.getMessage());
            throw new StorageException("Failed to authorize: " + e.getMessage(), e);
        }
        notify("Successfully authorized with B2.");
        return true;
    }

    private Boolean attempt(final Integer times, String actionName, final B2ApiCall action) throws IOException, StorageException, GeneralSecurityException {
        int attempt;
        for (attempt = 0; attempt < times; attempt++) {
            if (action.call()) {
                break;
            }
            ErrorResponse errorResponse = backblazeApiWrapper.getLastError().orElseThrow(
                    () -> new StorageException("Unknown error from B2 storage layer"));
            handleGeneralErrors(errorResponse);
            notify("Failed to " + actionName + ": (" + errorResponse.status + ") " + errorResponse.message);
            notify("Retrying...");
        }
        return attempt < times;
    }

    private Boolean tryAuthorize(String accountId, String applicationKey) throws IOException {
        authorizeResponse = backblazeApiWrapper.authorize(accountId, applicationKey).orElse(null);
        return authorizeResponse != null;
    }

    private Boolean tryGetUploadUrl(AuthorizeResponse authorizeResponse, String bucketId) throws IOException {
        getUploadUrlResponse = backblazeApiWrapper.getUploadUrl(authorizeResponse, bucketId).orElse(null);
        return getUploadUrlResponse != null;
    }

    private Boolean tryListBuckets(AuthorizeResponse authorizeResponse) throws IOException {
        listBucketsResponse = backblazeApiWrapper.listBuckets(authorizeResponse).orElse(null);
        return listBucketsResponse != null;
    }

    @Override
    public Boolean upload(Path workDir, String relativeFilePath, String destination)
            throws StorageException, GeneralSecurityException {
        try {
            if (!attempt(MAX_RETRY_ATTEMPTS, "upload " + relativeFilePath,
                    () -> tryUpload(workDir, relativeFilePath, destination, getUploadUrlResponse))) {
                errorMessage = "Failed to upload: maximum number of retry attempts reached.";
                return false;
            }
        } catch (IOException e) {
            logger.info("upload error: " + e.getMessage());
            throw new StorageException("Failed to upload " + relativeFilePath  + ": " + e.getMessage(), e);
        }
        notify("Successfully uploaded " + relativeFilePath + " to " + destination + ".");
        return true;
    }

    public Boolean tryUpload(Path workDir, String relativeFilePath, String destination, GetUploadUrlResponse getUploadUrlResponse)
            throws IOException, GeneralSecurityException {
        Optional<UploadFileResponse> response =
                backblazeApiWrapper.uploadFile(workDir, relativeFilePath, destination, getUploadUrlResponse);
        return response.isPresent();
    }

    @Override
    public void download(String filename) {

    }

    private Optional<Bucket> getBucketId(String bucketName) {
        return listBucketsResponse.buckets.stream().filter(b -> b.bucketName.equals(bucketName)).findFirst();
    }

    private void handleGeneralErrors(ErrorResponse error) throws StorageException {
        switch (error.status) {
            case HttpStatus.SC_BAD_REQUEST:
                throw new StorageException("Bad request: " + error.message);
            case HttpStatus.SC_UNAUTHORIZED:
                throw new StorageException("Unauthorized: " + error.message);
            case HttpStatus.SC_FORBIDDEN:
                throw new StorageException("Forbidden: " + error.message);
            case HttpStatus.SC_REQUEST_TIMEOUT:
                // retry
                break;
            case 429: // Too many requests
                sleep(5000);
                backoffSec = 1;
                break;
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                // retry
                break;
            case HttpStatus.SC_SERVICE_UNAVAILABLE:
                if (backoffSec > MAX_BACKOFF_SEC) {
                    throw new StorageException("Service Unavailable: " + error.message);
                }
                sleep(backoffSec);
                backoffSec *= 2;
                break;
        }
    }

    private void sleep(Integer seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void notify(String notification) {
        progressObservers.stream().forEach(o -> o.notify(notification));
    }
}
