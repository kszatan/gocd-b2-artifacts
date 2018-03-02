/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * op-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.storage.api;

import io.github.kszatan.gocd.b2.publish.storage.AuthorizeResponse;
import io.github.kszatan.gocd.b2.publish.storage.ErrorResponse;
import io.github.kszatan.gocd.b2.publish.storage.ListBucketsResponse;
import io.github.kszatan.gocd.b2.publish.storage.StorageException;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class ListBuckets extends B2ApiCall {
    private final AuthorizeResponse authorizeResponse;
    private ListBucketsResponse listBucketsResponse;

    public ListBuckets(AuthorizeResponse authorizeResponse) {
        super("list buckets");
        this.authorizeResponse = authorizeResponse;
    }

    public ListBucketsResponse getResponse() {
        return listBucketsResponse;
    }

    @Override
    public Boolean call(BackblazeApiWrapper backblazeApiWrapper) throws IOException, GeneralSecurityException {
        listBucketsResponse = backblazeApiWrapper.listBuckets(authorizeResponse).orElse(null);
        return listBucketsResponse != null;
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
