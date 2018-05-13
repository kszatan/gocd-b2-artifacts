/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage;

import java.util.*;

public class CachingCredentialsManager implements CredentialsManager {
    private Map<List<String>, AuthorizeResponse> authorizeResponseCache;
    private Map<List<String>, Map<String, String>> bucketIdCache;

    public CachingCredentialsManager() {
        authorizeResponseCache = new HashMap<>();
        bucketIdCache = new HashMap<>();
    }

    @Override
    public void storeAuthorizeResponse(String accountId, String applicationKey, AuthorizeResponse authorizeResponse) {
        authorizeResponseCache.put(Arrays.asList(accountId, applicationKey), authorizeResponse);
    }

    @Override
    public Optional<AuthorizeResponse> getAuthorizeResponse(String accountId, String applicationKey) {
        List<String> key = Arrays.asList(accountId, applicationKey);
        if (authorizeResponseCache.containsKey(key)) {
            return Optional.of(authorizeResponseCache.get(key));
        }
        return Optional.empty();
    }

    @Override
    public void storeBucketId(String accountId, String applicationKey, String bucketName, String bucketId) {
        List<String> key = Arrays.asList(accountId, applicationKey);
        Map<String, String> nameAndId;
        if (bucketIdCache.containsKey(key)) {
            nameAndId = bucketIdCache.get(key);
        } else {
            nameAndId = new HashMap<>();
        }
        nameAndId.put(bucketName, bucketId);
        bucketIdCache.put(key, nameAndId);
    }

    @Override
    public Optional<String> getBucketId(String accountId, String applicationKey, String bucketName) {
        List<String> key = Arrays.asList(accountId, applicationKey);
        if (!bucketIdCache.containsKey(key)) {
            return Optional.empty();
        }
        Map<String, String> nameAndId = bucketIdCache.get(key);
        if (!nameAndId.containsKey(bucketName)) {
            return Optional.empty();
        }
        return Optional.of(nameAndId.get(bucketName));
    }

    @Override
    public void forgetCredentials(String accountId, String applicationKey) {
        List<String> key = Arrays.asList(accountId, applicationKey);
        authorizeResponseCache.remove(key);
        bucketIdCache.remove(key);
    }
}
