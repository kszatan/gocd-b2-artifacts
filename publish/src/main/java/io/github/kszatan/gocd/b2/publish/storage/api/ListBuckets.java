/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.storage.api;

import io.github.kszatan.gocd.b2.publish.storage.AuthorizeResponse;
import io.github.kszatan.gocd.b2.publish.storage.ErrorResponse;
import io.github.kszatan.gocd.b2.publish.storage.ListBucketsResponse;
import io.github.kszatan.gocd.b2.publish.storage.StorageException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

public class ListBuckets extends B2ApiCall {
    private final AuthorizeResponse authorizeResponse;
    private ListBucketsResponse listBucketsResponse;

    public ListBuckets(BackblazeApiWrapper backblazeApiWrapper, AuthorizeResponse authorizeResponse) {
        super("list buckets", backblazeApiWrapper);
        this.authorizeResponse = authorizeResponse;
    }

    public Optional<ListBucketsResponse> getResponse() {
        return Optional.of(listBucketsResponse);
    }

    @Override
    public Boolean call() throws StorageException {
        try {
            listBucketsResponse = backblazeApiWrapper.listBuckets(authorizeResponse).orElse(null);
        } catch (IOException e) {
            throw new StorageException(e);
        }
        return listBucketsResponse != null;
    }

    @Override
    public void handleErrors(ErrorResponse error) throws StorageException {
        super.handleErrors(error);
    }

}
