/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage;

import com.thoughtworks.go.plugin.api.logging.Logger;
import io.github.kszatan.gocd.b2.utils.storage.api.*;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BackblazeStorage implements Storage {
    private static final Integer MAX_RETRY_ATTEMPTS = 5;

    private Logger logger = Logger.getLoggerFor(BackblazeStorage.class);
    private String errorMessage = "";
    private String bucketName;
    private String bucketId;
    private BackblazeApiWrapper backblazeApiWrapper;
    private AuthorizeResponse authorizeResponse;
    private GetUploadUrlResponse getUploadUrlResponse;
    private ListBucketsResponse listBucketsResponse;
    private List<ProgressObserver> progressObservers = new ArrayList<>();

    public BackblazeStorage(String bucketName) throws IOException {
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
            final Authorize authorize = new Authorize(backblazeApiWrapper, accountId, applicationKey);
            if (!attempt(MAX_RETRY_ATTEMPTS, authorize)) {
                return false;
            }
            authorizeResponse = authorize.getResponse().get();
            final ListBuckets listBuckets = new ListBuckets(backblazeApiWrapper, authorizeResponse);
            if (!attempt(MAX_RETRY_ATTEMPTS, listBuckets)) {
                return false;
            }
            listBucketsResponse = listBuckets.getResponse().get();
            Optional<Bucket> maybeBucket = getBucketId(bucketName);
            if (!maybeBucket.isPresent()) {
                errorMessage = "Bucket '" + bucketName + "' doesn't exist";
                return false;
            }
            bucketId = maybeBucket.get().bucketId;
            final GetUploadUrl getUploadUrl = new GetUploadUrl(backblazeApiWrapper, authorizeResponse, bucketId);
            if (!attempt(MAX_RETRY_ATTEMPTS, getUploadUrl)) {
                return false;
            }
            getUploadUrlResponse = getUploadUrl.getResponse().get();
        } catch (GeneralSecurityException | IOException e) {
            authorizeResponse = null;
            logger.info("authorize error: " + e.getMessage());
            throw new StorageException("Failed to authorize: " + e.getMessage(), e);
        }
        notify("Successfully authorized with B2.");
        return true;
    }

    @Override
    public Boolean upload(Path workDir, String relativeFilePath, String destination)
            throws StorageException, GeneralSecurityException {
        try {
            Upload upload = new Upload(backblazeApiWrapper, bucketId, workDir, relativeFilePath, destination,
                    authorizeResponse, getUploadUrlResponse);
            if (!attempt(MAX_RETRY_ATTEMPTS, upload)) {
                return false;
            }
        } catch (IOException e) {
            logger.info("upload error: " + e.getMessage());
            throw new StorageException("Failed to upload " + relativeFilePath  + ": " + e.getMessage(), e);
        }
        notify("Successfully uploaded " + relativeFilePath + " to " + destination + ".");
        return true;
    }

    private Boolean attempt(final Integer times, final B2ApiCall action) throws IOException, StorageException, GeneralSecurityException {
        int attempt;
        for (attempt = 0; attempt < times; attempt++) {
            if (action.call()) {
                break;
            }
            ErrorResponse errorResponse = backblazeApiWrapper.getLastError().orElseThrow(
                    () -> new StorageException("Unknown error from B2 storage layer"));
            action.handleErrors(errorResponse);
            notify("Failed to " + action.getName() + ": (" + errorResponse.status + ") " + errorResponse.message);
            notify("Retrying...");
        }
        Boolean success = attempt < times;
        if (!success) {
            errorMessage = "Failed to " + action.getName() + ": maximum number of retry attempts reached";
        }
        return success;
    }

    @Override
    public void download(String filename) {

    }

    private Optional<Bucket> getBucketId(String bucketName) {
        return listBucketsResponse.buckets.stream().filter(b -> b.bucketName.equals(bucketName)).findFirst();
    }

    private void notify(String notification) {
        progressObservers.stream().forEach(o -> o.notify(notification));
    }
}