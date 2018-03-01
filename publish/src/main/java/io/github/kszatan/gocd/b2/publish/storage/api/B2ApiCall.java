/*
 * Copyright (c) 2018. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.storage.api;

import io.github.kszatan.gocd.b2.publish.storage.ErrorResponse;
import io.github.kszatan.gocd.b2.publish.storage.StorageException;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.security.GeneralSecurityException;

public abstract class B2ApiCall {
    private static final Integer MAX_BACKOFF_SEC = 64;
    private Integer backoffSec = 1;
    private String name;

    public B2ApiCall(String callName) {
        this.name = callName;
    }

    public String getName() {
        return name;
    }

    public abstract Boolean call() throws IOException, GeneralSecurityException;
    public void handleErrors(ErrorResponse error) throws StorageException {
        switch (error.status) {
            case HttpStatus.SC_BAD_REQUEST:
                throw new StorageException("Bad request: " + error.message);
            case HttpStatus.SC_UNAUTHORIZED:
                throw new StorageException("Unauthorized: " + error.message);
            case HttpStatus.SC_FORBIDDEN:
                throw new StorageException("Forbidden: " + error.message);
            case HttpStatus.SC_REQUEST_TIMEOUT:
//                getUploadUrlResponse = null;
                // retry
                break;
            case 429: // Too many requests
                sleep(5);
                backoffSec = 1;
                break;
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                // retry
                break;
            case HttpStatus.SC_SERVICE_UNAVAILABLE:
                if (backoffSec > MAX_BACKOFF_SEC) {
                    throw new StorageException("Service Unavailable: " + error.message);
                }
                sleep(backoffSec);
                backoffSec *= 2;
                break;
        }
    }

    public abstract Boolean shouldGetNewUploadUrl(ErrorResponse response);

    protected void sleep(Integer seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
