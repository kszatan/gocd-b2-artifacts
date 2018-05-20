/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage.api;

import io.github.kszatan.gocd.b2.utils.storage.AuthorizeResponse;
import io.github.kszatan.gocd.b2.utils.storage.ErrorResponse;
import io.github.kszatan.gocd.b2.utils.storage.StartLargeFileResponse;
import io.github.kszatan.gocd.b2.utils.storage.StorageException;

import java.io.IOException;
import java.util.Optional;

public class StartLargeFile extends B2ApiCall {
    private final AuthorizeResponse authorizeResponse;
    private StartLargeFileResponse response;
    private String backblazeFileName;
    private String bucketId;

    public StartLargeFile(BackblazeApiWrapper backblazeApiWrapper, AuthorizeResponse authorizeResponse,
                          String backblazeFileName, String bucketId) {
        super("start large file", backblazeApiWrapper);
        this.authorizeResponse = authorizeResponse;
        this.backblazeFileName = backblazeFileName;
        this.bucketId = bucketId;
    }

    public Optional<StartLargeFileResponse> getResponse() {
        return Optional.of(response);
    }

    @Override
    public Boolean call() throws StorageException {
        try {
            response = backblazeApiWrapper.startLargeFile(authorizeResponse, backblazeFileName, bucketId).orElse(null);
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
