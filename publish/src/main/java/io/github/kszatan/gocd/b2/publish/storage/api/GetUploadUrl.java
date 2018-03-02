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

public class GetUploadUrl extends B2ApiCall {
    private final AuthorizeResponse authorizeResponse;
    private GetUploadUrlResponse getUploadUrlResponse;
    private String bucketId;

    public GetUploadUrl(AuthorizeResponse authorizeResponse, String bucketId) {
        super("get upload url");
        this.authorizeResponse = authorizeResponse;
        this.bucketId = bucketId;
    }

    public GetUploadUrlResponse getResponse() {
        return getUploadUrlResponse;
    }

    @Override
    public Boolean call(BackblazeApiWrapper backblazeApiWrapper) throws IOException, GeneralSecurityException {
        getUploadUrlResponse = backblazeApiWrapper.getUploadUrl(authorizeResponse, bucketId).orElse(null);
        return getUploadUrlResponse != null;
    }

    @Override
    public void handleErrors(ErrorResponse error) throws StorageException {
        super.handleErrors(error);
    }

    @Override
    public Boolean shouldGetNewUploadUrl(ErrorResponse response) {
        return false;
    }
}
