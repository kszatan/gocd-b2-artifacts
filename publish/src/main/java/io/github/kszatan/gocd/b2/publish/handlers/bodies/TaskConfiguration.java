/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * op-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers.bodies;

import com.google.gson.reflect.TypeToken;
import io.github.kszatan.gocd.b2.publish.json.GsonService;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TaskConfiguration {
    private Entry sourceDestinations;
    private Entry destinationPrefix;
    private Entry bucketId;

    public TaskConfiguration() {
        sourceDestinations = new Entry();
        destinationPrefix = new Entry();
        bucketId = new Entry();
    }

    public String getSourceDestinations() { return sourceDestinations.value; }

    public List<SourceDestination> getSourceDestinationsAsList() {
        Type type = new TypeToken<ArrayList<SourceDestination>>() {}.getType();
        return new GsonService().fromJson(sourceDestinations.value, type);
    }

    public String getDestinationPrefix() {
        return destinationPrefix.value;
    }

    public String getBucketId() {
        return bucketId.value;
    }

    public void setSourceDestinations(String sourceDestinations) { this.sourceDestinations.value = sourceDestinations; }

    public void setDestinationPrefix(String destinationPrefix) {
        this.destinationPrefix.value = destinationPrefix;
    }

    public void setBucketId(String bucketId) {
        this.bucketId.value = bucketId;
    }
}
