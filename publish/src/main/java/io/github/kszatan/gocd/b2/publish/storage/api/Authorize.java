/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * op-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.storage.api;

import io.github.kszatan.gocd.b2.publish.storage.AuthorizeResponse;
import io.github.kszatan.gocd.b2.publish.storage.ErrorResponse;
import io.github.kszatan.gocd.b2.publish.storage.StorageException;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class Authorize extends B2ApiCall {
    private final String accountId;
    private final String applicationKey;
    private AuthorizeResponse authorizeResponse;

    public Authorize(String accountId, String applicationKey) {
        super("authorize");
        this.accountId = accountId;
        this.applicationKey = applicationKey;
    }

    public AuthorizeResponse getResponse() {
        return authorizeResponse;
    }

    @Override
    public Boolean call(BackblazeApiWrapper backblazeApiWrapper) throws IOException, GeneralSecurityException {
        authorizeResponse = backblazeApiWrapper.authorize(accountId, applicationKey).orElse(null);
        return authorizeResponse != null;
    }

    @Override
    public void handleErrors(ErrorResponse error) throws StorageException {
        super.handleErrors(error);
    }

    @Override
    public Boolean shouldGetNewUploadUrl(ErrorResponse response) {
        return null;
    }
}
