/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * op-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.storage.api;

import io.github.kszatan.gocd.b2.publish.storage.*;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Optional;

public class Upload extends B2ApiCall {
    private final String bucketId;
    private final Path workDir;
    private final String relativeFilePath;
    private final String destination;
    private final AuthorizeResponse authorizeResponse;
    private GetUploadUrlResponse getUploadUrlResponse;
    private UploadFileResponse uploadFileResponse;

    public Upload(String bucketId, Path workDir, String relativeFilePath, String destination,
                  AuthorizeResponse authorizeResponse, GetUploadUrlResponse getUploadUrlResponse) {
        super("upload " + relativeFilePath);
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
    public Boolean call(BackblazeApiWrapper backblazeApiWrapper) throws IOException, GeneralSecurityException {
        if (getUploadUrlResponse == null) {
//            notify("Fetching new upload url...");
            Optional<GetUploadUrlResponse> maybeUploadUrl = backblazeApiWrapper.getUploadUrl(authorizeResponse, bucketId);
            if (!maybeUploadUrl.isPresent()) {
                return false;
            }
            this.getUploadUrlResponse = maybeUploadUrl.get();
        }
        uploadFileResponse = backblazeApiWrapper.uploadFile(workDir, relativeFilePath, destination, getUploadUrlResponse)
                .orElse(null);
        return uploadFileResponse != null;
    }

    @Override
    public void handleErrors(ErrorResponse error) throws StorageException {
        super.handleErrors(error);
    }

    @Override
    public Boolean shouldGetNewUploadUrl(ErrorResponse error) {
        return error.status == HttpStatus.SC_REQUEST_TIMEOUT;
    }
}
