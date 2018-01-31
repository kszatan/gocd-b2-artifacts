/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.storage;

import com.thoughtworks.go.plugin.api.logging.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class BackblazeStorage implements Storage {
    private static final String B2_API_URL = "https://api.backblazeb2.com/b2api/v1/";
    private static final String AUTHORIZE_ACCOUNT_CMD = "b2_authorize_account";

    private Logger logger = Logger.getLoggerFor(BackblazeStorage.class);

    private String errorMessage;
    private String bucketName;
    private BackblazeApiWrapper backblazeApiWrapper;
    private AuthorizeResponse authorizeResponse;
    private GetUploadUrlResponse getUploadUrlResponse;
    private ListBucketsResponse listBucketsResponse;

    public BackblazeStorage(String bucketName) {
        this.errorMessage = "";
        this.bucketName = bucketName;
        this.backblazeApiWrapper = new BackblazeApiWrapper(bucketName, null);
    }

    @Override
    public String getLastErrorMessage() {
        return errorMessage;
    }

    @Override
    public Boolean authorize(String accountId, String applicationKey) {
        try {
            authorizeResponse = backblazeApiWrapper.authorize(accountId, applicationKey);
            listBucketsResponse = backblazeApiWrapper.listBuckets(authorizeResponse);
            Bucket bucket = getBucketId(bucketName).orElseThrow(
                    () -> new StorageException("Bucket '" + bucketName + "' doesn't exist"));
            getUploadUrlResponse = backblazeApiWrapper.getUploadUrl(authorizeResponse, bucket.bucketId);
        } catch (Exception e) {
            authorizeResponse = null;
            errorMessage = e.getMessage();
            logger.info("authorize error: " + e.getMessage());
            return false;
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

    private Optional<Bucket> getBucketId(String bucketName) {
        return listBucketsResponse.buckets.stream().filter(b -> b.bucketName.equals(bucketName)).findFirst();
    }
}
