/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage.api;

import io.github.kszatan.gocd.b2.utils.storage.*;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class Upload extends B2ApiCall {
    private final String bucketId;
    private final Path workDir;
    private final String relativeFilePath;
    private final String destination;
    private final AuthorizeResponse authorizeResponse;
    private GetUploadUrlResponse getUploadUrlResponse;
    private UploadFileResponse uploadFileResponse;

    public Upload(BackblazeApiWrapper backblazeApiWrapper, String bucketId, Path workDir, String relativeFilePath,
                  String destination, AuthorizeResponse authorizeResponse, GetUploadUrlResponse getUploadUrlResponse) {
        super("upload " + relativeFilePath, backblazeApiWrapper);
        this.bucketId = bucketId;
        this.workDir = workDir;
        this.relativeFilePath = relativeFilePath;
        this.destination = destination;
        this.getUploadUrlResponse = getUploadUrlResponse;
        this.authorizeResponse = authorizeResponse;
    }

    Optional<UploadFileResponse> getResponse() {
        return Optional.ofNullable(uploadFileResponse);
    }

    @Override
    public Boolean call() throws StorageException {
        if (getUploadUrlResponse == null) {
            fetchNewUploadUrl();
        }
        try {
            uploadFileResponse = backblazeApiWrapper.uploadFile(workDir, relativeFilePath, destination, getUploadUrlResponse)
                    .orElse(null);
        } catch(IOException | GeneralSecurityException e) {
            throw new StorageException("Exception while uploading file: " + e.getMessage(), e);
        }
        return uploadFileResponse != null;
    }

    @Override
    public void handleErrors(ErrorResponse error) throws StorageException {
        if (error.status == HttpStatus.SC_REQUEST_TIMEOUT) {
            fetchNewUploadUrl();
        }
        super.handleErrors(error);
    }

    private void fetchNewUploadUrl() throws StorageException {
        GetUploadUrl getUploadUrl = new GetUploadUrl(backblazeApiWrapper, authorizeResponse, bucketId);
        if (getUploadUrl.call()) {
            this.getUploadUrlResponse = getUploadUrl.getResponse().get();
        } else {
            String errorMessage = backblazeApiWrapper.getLastError().orElse(new ErrorResponse()).message;
            throw new StorageException("Cannot get new upload URL: " + errorMessage);
        }
    }

}
