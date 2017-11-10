/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * op-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers.bodies;

import com.google.gson.annotations.SerializedName;

public class TaskConfiguration {
    private Entry destinationPrefix;
    private Entry bucketId;

    public TaskConfiguration() {
        destinationPrefix = new Entry();
        bucketId = new Entry();
    }

    public String getDestinationPrefix() {
        return destinationPrefix.value;
    }

    public String getBucketId() {
        return bucketId.value;
    }

    public void setDestinationPrefix(String destinationPrefix) {
        this.destinationPrefix.value = destinationPrefix;
    }

    public void setBucketId(String bucketId) {
        this.bucketId.value = bucketId;
    }
}
