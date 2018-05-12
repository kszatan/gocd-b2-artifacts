/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage.api;

import io.github.kszatan.gocd.b2.utils.storage.*;

import java.io.IOException;
import java.util.Optional;

public class StartLargeFile extends B2ApiCall {
    private final AuthorizeResponse authorizeResponse;
    private StartLargeFileResponse response;
    private String relativeFilePath;
    private String bucketId;

    public StartLargeFile(BackblazeApiWrapper backblazeApiWrapper, AuthorizeResponse authorizeResponse,
                          String relativeFilePath, String bucketId) {
        super("start large file", backblazeApiWrapper);
        this.authorizeResponse = authorizeResponse;
        this.relativeFilePath = relativeFilePath;
        this.bucketId = bucketId;
    }

    public Optional<StartLargeFileResponse> getResponse() {
        return Optional.of(response);
    }

    @Override
    public Boolean call() throws StorageException {
        try {
            response = backblazeApiWrapper.startLargeFile(authorizeResponse, relativeFilePath, bucketId).orElse(null);
        } catch (IOException e) {
            throw new StorageException(e);
        }
        return response != null;
    }

    @Override
    public void handleErrors(ErrorResponse error) throws StorageException {
        super.handleErrors(error);
    }

}
