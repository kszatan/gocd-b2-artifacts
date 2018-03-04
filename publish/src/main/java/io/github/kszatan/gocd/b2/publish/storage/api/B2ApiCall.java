/*
 * Copyright (c) 2018. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.storage.api;

import io.github.kszatan.gocd.b2.publish.storage.ErrorResponse;
import io.github.kszatan.gocd.b2.publish.storage.StorageException;
import org.apache.http.HttpStatus;

public abstract class B2ApiCall {
    @FunctionalInterface
    public interface ThreadSleeper {
        void sleep(Integer seconds);
    }

    private static final Integer MAX_BACKOFF_SEC = 64;
    private Integer backoffSec = 1;
    private String name;
    protected BackblazeApiWrapper backblazeApiWrapper;
    private ThreadSleeper sleeper;

    public B2ApiCall(String callName, BackblazeApiWrapper backblazeApiWrapper) {
        this.name = callName;
        this.backblazeApiWrapper = backblazeApiWrapper;
        this.sleeper = seconds -> {
            try {
                Thread.sleep(seconds * 1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        };
    }

    public B2ApiCall(String callName, BackblazeApiWrapper backblazeApiWrapper, ThreadSleeper sleeper) {
        this.name = callName;
        this.backblazeApiWrapper = backblazeApiWrapper;
        this.sleeper = sleeper;
    }

    public String getName() {
        return name;
    }

    public abstract Boolean call() throws StorageException;
    
    public void handleErrors(ErrorResponse error) throws StorageException {
        switch (error.status) {
            case HttpStatus.SC_BAD_REQUEST:
                throw new StorageException("Bad request: " + error.message);
            case HttpStatus.SC_UNAUTHORIZED:
                // Usually obtain a new auth token and retry
                break;
            case HttpStatus.SC_FORBIDDEN:
                throw new StorageException("Forbidden: " + error.message);
            case HttpStatus.SC_REQUEST_TIMEOUT:
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
                if (error.retryAfter > 0) {
                    sleep(error.retryAfter);
                } else {
                    if (backoffSec > MAX_BACKOFF_SEC) {
                        throw new StorageException("Service Unavailable: " + error.message);
                    }
                    sleep(backoffSec);
                    backoffSec *= 2;
                }
                break;
        }
    }

    protected void sleep(Integer seconds) {
        sleeper.sleep(seconds);
    }
}
