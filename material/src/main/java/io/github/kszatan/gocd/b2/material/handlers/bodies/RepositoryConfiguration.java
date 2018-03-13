/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers.bodies;

public class RepositoryConfiguration {
    private Entry url;
    private Entry bucketName;
    private Entry accountId;
    private Entry applicationKey;

    public RepositoryConfiguration() {
        url = new Entry();
        bucketName = new Entry();
        accountId = new Entry();
        applicationKey = new Entry();
    }

    public String getUrl() {
        return url.value;
    }

    public String getBucketName() {
        return bucketName.value;
    }

    public String getAccountId() {
        return accountId.value;
    }

    public String getApplicationKey() {
        return applicationKey.value;
    }
}
