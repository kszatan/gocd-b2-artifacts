/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage.api;

import io.github.kszatan.gocd.b2.utils.storage.AuthorizeResponse;
import io.github.kszatan.gocd.b2.utils.storage.ErrorResponse;
import io.github.kszatan.gocd.b2.utils.storage.GetUploadPartUrlResponse;
import io.github.kszatan.gocd.b2.utils.storage.StorageException;

import java.io.IOException;
import java.util.Optional;

public class GetUploadPartUrl extends B2ApiCall {
    private final AuthorizeResponse authorizeResponse;
    private GetUploadPartUrlResponse getUploadPartUrlResponse;
    private String fileId;

    public GetUploadPartUrl(BackblazeApiWrapper backblazeApiWrapper, AuthorizeResponse authorizeResponse, String fileId) {
        super("get upload part url", backblazeApiWrapper);
        this.authorizeResponse = authorizeResponse;
        this.fileId = fileId;
    }

    public Optional<GetUploadPartUrlResponse> getResponse() {
        return Optional.ofNullable(getUploadPartUrlResponse);
    }

    @Override
    public Boolean call() throws StorageException {
        try {
            getUploadPartUrlResponse = backblazeApiWrapper.getUploadPartUrl(authorizeResponse, fileId).orElse(null);
        } catch (IOException e) {
            throw new StorageException(e);
        }
        return getUploadPartUrlResponse != null;
    }

    @Override
    public void handleErrors(ErrorResponse error) throws StorageException {
        super.handleErrors(error);
    }
}
