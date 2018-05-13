/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage;

import java.util.Optional;

public class DummyCredentialsManager implements CredentialsManager {
    @Override
    public void storeAuthorizeResponse(String accountId, String applicationKey, AuthorizeResponse authorizeResponse) {
        // do nothing
    }

    @Override
    public Optional<AuthorizeResponse> getAuthorizeResponse(String accountId, String applicationKey) {
        return Optional.empty();
    }

    @Override
    public void storeBucketId(String accountId, String applicationKey, String bucketName, String bucketId) {
        // do nothing
    }

    @Override
    public Optional<String> getBucketId(String accountId, String applicationKey, String bucketName) {
        return Optional.empty();
    }

    @Override
    public void forgetCredentials(String accountId, String applicationKey) {
        // do nothing
    }
}
