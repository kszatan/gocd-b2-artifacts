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
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BackblazeStorage implements Storage {
    private static final Integer MAX_RETRY_ATTEMPTS = 5;

    private Logger logger = Logger.getLoggerFor(BackblazeStorage.class);

    private String errorMessage = "";
    private String bucketName;
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
            if (!tryAuthorize(accountId, applicationKey)) {
                errorMessage = "Failed to authorize: maximum number of retry attempts reached.";
                return false;
            }
            listBucketsResponse = backblazeApiWrapper.listBuckets(authorizeResponse).orElseThrow(
                    () -> new StorageException("Error during listing of buckets"));
            Bucket bucket = getBucketId(bucketName).orElseThrow(
                    () -> new StorageException("Bucket '" + bucketName + "' doesn't exist"));
            getUploadUrlResponse = backblazeApiWrapper.getUploadUrl(authorizeResponse, bucket.bucketId).orElseThrow(
                    () -> new StorageException("Error during fetching upload url"));
        } catch (IOException e) {
            authorizeResponse = null;
            logger.info("authorize error: " + e.getMessage());
            throw new StorageException("Failed to authorize: " + e.getMessage(), e);
        }
        return true;
    }

    @Override
    public void upload(Path workDir, String relativeFilePath, String destination) throws StorageException {
        logger.info("uploading " + relativeFilePath);
        Optional<UploadFileResponse> response;
        try {
            response = backblazeApiWrapper.uploadFile(workDir, relativeFilePath, getUploadUrlResponse);
            if (response.isPresent()) {
                logger.info("SUCCESS");
            } else {
                ErrorResponse error = backblazeApiWrapper.getLastError().get();
                logger.info("FAILURE: " + error.toString() );
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new StorageException(e.getMessage());
        }
    }

    @Override
    public void download(String filename) {

    }

    private Boolean tryAuthorize(String accountId, String applicationKey) throws IOException, StorageException {
        int attempt;
        for (attempt = 0; attempt < MAX_RETRY_ATTEMPTS; attempt++) {
            Optional<AuthorizeResponse> maybeAuthorizeResponse = backblazeApiWrapper.authorize(accountId, applicationKey);
            if (maybeAuthorizeResponse.isPresent()) {
                authorizeResponse = maybeAuthorizeResponse.get();
                notify("Successfully authorized with B2.");
                break;
            }
            ErrorResponse errorResponse = backblazeApiWrapper.getLastError().orElseThrow(
                    () -> new StorageException("Unknown error from B2 storage layer"));
            logger.info("authorize call error: (" + errorResponse.status + ") " + errorResponse.message);
            handleGeneralErrors(errorResponse);
            notify("Failed to authorize: (" + errorResponse.status + ") " + errorResponse.message);
            notify("Retrying...");
        }
        return attempt < MAX_RETRY_ATTEMPTS;
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
                // retry
                break;
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                // retry
                break;
            case HttpStatus.SC_SERVICE_UNAVAILABLE:
                // retry with backoff
                break;
        }
    }

    private void notify(String notification) {
        progressObservers.stream().forEach(o -> o.notify(notification));
    }
}
