/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage;

import java.util.Optional;

public interface CredentialsManager {
    void storeAuthorizeResponse(String accountId, String applicationKey, AuthorizeResponse authorizeResponse);
    Optional<AuthorizeResponse> getAuthorizeResponse(String accountId, String applicationKey);
    void storeBucketId(String accountId, String applicationKey, String bucketName, String bucketId);
    Optional<String> getBucketId(String accountId, String applicationKey, String bucketName);
    void forgetCredentials(String accountId, String applicationKey);
}
