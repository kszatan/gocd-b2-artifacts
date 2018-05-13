/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage;

import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class CachingCredentialsManagerTest {
    static private final String ACCOUNT_ID = "asdf";
    static private final String APPLICATION_KEY = "fdsa";
    
    @Test
    public void itShouldBePossibleToRetrievePreviouslyStoredAuthorizeResponse() {
        CachingCredentialsManager manager = new CachingCredentialsManager();
        AuthorizeResponse inputResponse = new AuthorizeResponse();
        manager.storeAuthorizeResponse(ACCOUNT_ID, APPLICATION_KEY, inputResponse);
        Optional<AuthorizeResponse> maybeResponse = manager.getAuthorizeResponse(ACCOUNT_ID, APPLICATION_KEY);
        assertThat(maybeResponse.isPresent(), equalTo(true));
        assertThat(maybeResponse.get(), equalTo(inputResponse));
    }

    @Test
    public void getAuthorizeResponseShouldReturnEmptyOptionalIfCredentialsWereNotStored() {
        CachingCredentialsManager manager = new CachingCredentialsManager();
        Optional<AuthorizeResponse> maybeResponse = manager.getAuthorizeResponse(ACCOUNT_ID, APPLICATION_KEY);
        assertThat(maybeResponse.isPresent(), equalTo(false));
    }

    @Test
    public void itShouldBePossibleToRetrievePreviouslyStoredBucketId() {
        CachingCredentialsManager manager = new CachingCredentialsManager();
        String bucketName = "bukhet";
        String bucketId = "8ad3jh3ui4aiu3";
        manager.storeBucketId(ACCOUNT_ID, APPLICATION_KEY, bucketName, bucketId);
        Optional<String> maybeBucketId = manager.getBucketId(ACCOUNT_ID, APPLICATION_KEY, bucketName);
        assertThat(maybeBucketId.isPresent(), equalTo(true));
        assertThat(maybeBucketId.get(), equalTo(bucketId));
    }

    @Test
    public void getBucketIdShouldReturnEmptyOptionalIfCredentialsWereNotStored() {
        CachingCredentialsManager manager = new CachingCredentialsManager();
        Optional<String> maybeBucketId = manager.getBucketId(ACCOUNT_ID, APPLICATION_KEY, "bukhet");
        assertThat(maybeBucketId.isPresent(), equalTo(false));
    }

    @Test
    public void managerShouldReturnEmptyOptionalForForgottenCredentials() {
        CachingCredentialsManager manager = new CachingCredentialsManager();
        AuthorizeResponse inputResponse = new AuthorizeResponse();
        manager.storeAuthorizeResponse(ACCOUNT_ID, APPLICATION_KEY, inputResponse);
        String bucketName = "bukhet";
        String bucketId = "8ad3jh3ui4aiu3";
        manager.storeBucketId(ACCOUNT_ID, APPLICATION_KEY, bucketName, bucketId);

        manager.forgetCredentials(ACCOUNT_ID, APPLICATION_KEY);

        assertThat(manager.getAuthorizeResponse(ACCOUNT_ID, APPLICATION_KEY).isPresent(), equalTo(false));
        assertThat(manager.getBucketId(ACCOUNT_ID, APPLICATION_KEY, bucketName).isPresent(), equalTo(false));
    }
}