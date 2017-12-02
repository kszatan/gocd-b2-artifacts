/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers.bodies;

import io.github.kszatan.gocd.b2.publish.json.GsonService;
import io.github.kszatan.gocd.b2.publish.json.IncompleteJson;
import io.github.kszatan.gocd.b2.publish.json.InvalidJson;

import java.util.Arrays;
import java.util.Collection;

public class ExecuteRequest {
    private TaskConfiguration config;
    private TaskContext context;

    static public ExecuteRequest create(String json) throws InvalidJson, IncompleteJson {
        Collection<String> missing = GsonService.validate(json,
                Arrays.asList("config", "context"));
        if (!missing.isEmpty()) {
            throw new IncompleteJson("Missing fields: " + missing.toString());
        }
        return GsonService.fromJson(json, ExecuteRequest.class);
    }

    public TaskConfiguration getTaskConfiguration() {
        return config;
    }

    public TaskContext getTaskContext() { return context; }
}
