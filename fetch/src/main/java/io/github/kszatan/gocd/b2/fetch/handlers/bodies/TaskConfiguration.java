/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.fetch.handlers.bodies;

import com.google.gson.reflect.TypeToken;
import io.github.kszatan.gocd.b2.utils.json.GsonService;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TaskConfiguration {
    private Entry repositoryName;
    private Entry packageName;
    private Entry destination;

    public TaskConfiguration() {
        repositoryName = new Entry();
        packageName = new Entry();
        destination = new Entry();
    }

    public String getRepositoryName() { return repositoryName.value; }

    public String getPackageName() {
        return packageName.value;
    }

    public String getDestination() {
        return destination.value;
    }

    public void setRepositoryName(String repositoryName) { this.repositoryName.value = repositoryName; }

    public void setPackageName(String packageName) {
        this.packageName.value = packageName;
    }

    public void setDestination(String destination) {
        this.destination.value = destination;
    }
}
