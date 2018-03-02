/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * op-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.storage.api;

import io.github.kszatan.gocd.b2.publish.storage.AuthorizeResponse;
import io.github.kszatan.gocd.b2.publish.storage.ErrorResponse;
import io.github.kszatan.gocd.b2.publish.storage.GetUploadUrlResponse;
import io.github.kszatan.gocd.b2.publish.storage.StorageException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

public class GetUploadUrl extends B2ApiCall {
    private final AuthorizeResponse authorizeResponse;
    private GetUploadUrlResponse getUploadUrlResponse;
    private String bucketId;

    public GetUploadUrl(BackblazeApiWrapper backblazeApiWrapper, AuthorizeResponse authorizeResponse, String bucketId) {
        super("get upload url", backblazeApiWrapper);
        this.authorizeResponse = authorizeResponse;
        this.bucketId = bucketId;
    }

    public Optional<GetUploadUrlResponse> getResponse() {
        return Optional.ofNullable(getUploadUrlResponse);
    }

    @Override
    public Boolean call() throws StorageException {
        try {
            getUploadUrlResponse = backblazeApiWrapper.getUploadUrl(authorizeResponse, bucketId).orElse(null);
        } catch (IOException e) {
            throw new StorageException(e);
        }
        return getUploadUrlResponse != null;
    }

    @Override
    public void handleErrors(ErrorResponse error) throws StorageException {
        super.handleErrors(error);
    }
}
