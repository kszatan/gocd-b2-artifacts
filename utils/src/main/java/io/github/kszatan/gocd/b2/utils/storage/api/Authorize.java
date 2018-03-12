/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage.api;

import io.github.kszatan.gocd.b2.utils.storage.AuthorizeResponse;
import io.github.kszatan.gocd.b2.utils.storage.ErrorResponse;
import io.github.kszatan.gocd.b2.utils.storage.Storage;
import io.github.kszatan.gocd.b2.utils.storage.StorageException;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

public class Authorize extends B2ApiCall {
    private final String accountId;
    private final String applicationKey;
    private AuthorizeResponse authorizeResponse;

    public Authorize(BackblazeApiWrapper backblazeApiWrapper, String accountId, String applicationKey) {
        super("authorize", backblazeApiWrapper);
        this.accountId = accountId;
        this.applicationKey = applicationKey;
    }

    public Optional<AuthorizeResponse> getResponse() {
        return Optional.of(authorizeResponse);
    }

    @Override
    public Boolean call() throws StorageException {
        try {
            authorizeResponse = backblazeApiWrapper.authorize(accountId, applicationKey).orElse(null);
        } catch (IOException e) {
            throw new StorageException(e);
        }
        return authorizeResponse != null;
    }

    @Override
    public void handleErrors(ErrorResponse error) throws StorageException {
        if (error.status == HttpStatus.SC_UNAUTHORIZED) {
            throw new StorageException("Unauthorized: " + error.message);
        }
        super.handleErrors(error);
    }
}
