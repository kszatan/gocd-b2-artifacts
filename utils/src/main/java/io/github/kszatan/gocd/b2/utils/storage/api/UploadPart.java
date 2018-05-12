/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage.api;

import io.github.kszatan.gocd.b2.utils.storage.*;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

public class UploadPart extends B2ApiCall {
    private final byte[] filePart;
    private final Integer partLength;
    private final Integer partNumber;
    private final AuthorizeResponse authorizeResponse;
    private GetUploadPartUrlResponse getUploadPartUrlResponse;
    private UploadPartResponse uploadPartResponse;

    public UploadPart(BackblazeApiWrapper backblazeApiWrapper, byte[] filePart, Integer partLength, Integer partNumber,
                      AuthorizeResponse authorizeResponse, GetUploadPartUrlResponse getUploadPartUrlResponse) {
        super("upload part ", backblazeApiWrapper);
        this.filePart = filePart;
        this.partNumber = partNumber;
        this.partLength = partLength;
        this.getUploadPartUrlResponse = getUploadPartUrlResponse;
        this.authorizeResponse = authorizeResponse;
    }

    public Optional<UploadPartResponse> getResponse() {
        return Optional.ofNullable(uploadPartResponse);
    }

    @Override
    public Boolean call() throws StorageException {
        try {
            uploadPartResponse = backblazeApiWrapper.uploadPart(filePart, partLength, partNumber, getUploadPartUrlResponse)
                    .orElse(null);
        } catch(IOException | GeneralSecurityException e) {
            throw new StorageException("Exception while uploading file part: " + e.getMessage(), e);
        }
        return uploadPartResponse != null;
    }

    @Override
    public void handleErrors(ErrorResponse error) throws StorageException {
        if (error.status == HttpStatus.SC_REQUEST_TIMEOUT) {
            fetchNewUploadUrl();
        }
        super.handleErrors(error);
    }

    private void fetchNewUploadUrl() throws StorageException {
        GetUploadPartUrl getUploadPartUrl = new GetUploadPartUrl(backblazeApiWrapper, authorizeResponse, getUploadPartUrlResponse.fileId);
        if (getUploadPartUrl.call()) {
            this.getUploadPartUrlResponse = getUploadPartUrl.getResponse().get();
        } else {
            String errorMessage = backblazeApiWrapper.getLastError().orElse(new ErrorResponse()).message;
            throw new StorageException("Cannot get new upload part URL: " + errorMessage);
        }
    }

}
