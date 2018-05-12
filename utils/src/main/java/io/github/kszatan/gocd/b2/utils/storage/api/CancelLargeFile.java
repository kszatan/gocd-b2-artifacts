/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage.api;

import io.github.kszatan.gocd.b2.utils.storage.AuthorizeResponse;
import io.github.kszatan.gocd.b2.utils.storage.ErrorResponse;
import io.github.kszatan.gocd.b2.utils.storage.CancelLargeFileResponse;
import io.github.kszatan.gocd.b2.utils.storage.StorageException;

import java.io.IOException;
import java.util.Optional;

public class CancelLargeFile extends B2ApiCall {
    private final AuthorizeResponse authorizeResponse;
    private CancelLargeFileResponse response;
    private String fileId;

    public CancelLargeFile(BackblazeApiWrapper backblazeApiWrapper, AuthorizeResponse authorizeResponse, String fileId) {
        super("cancel large file", backblazeApiWrapper);
        this.authorizeResponse = authorizeResponse;
        this.fileId = fileId;
    }

    public Optional<CancelLargeFileResponse> getResponse() {
        return Optional.of(response);
    }

    @Override
    public Boolean call() throws StorageException {
        try {
            response = backblazeApiWrapper.cancelLargeFile(authorizeResponse, fileId).orElse(null);
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
