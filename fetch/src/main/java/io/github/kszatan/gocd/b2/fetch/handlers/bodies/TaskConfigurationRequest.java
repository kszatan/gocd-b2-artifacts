/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.fetch.handlers.bodies;

import io.github.kszatan.gocd.b2.utils.json.GsonService;
import io.github.kszatan.gocd.b2.utils.json.IncompleteJson;
import io.github.kszatan.gocd.b2.utils.json.InvalidJson;

import java.util.Arrays;
import java.util.Collection;

public class TaskConfigurationRequest {
    private final TaskConfiguration configuration;

    public TaskConfigurationRequest(String json) throws InvalidJson, IncompleteJson {
        Collection<String> missing = GsonService.validate(json,
                Arrays.asList("repositoryName", "packageName", "destination"));
        if (!missing.isEmpty()) {
            throw new IncompleteJson("Missing fields: " + missing.toString());
        }
        configuration = GsonService.fromJson(json, TaskConfiguration.class);
    }

    public TaskConfiguration getConfiguration() {
        return configuration;
    }
}
