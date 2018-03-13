/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers.bodies;

import io.github.kszatan.gocd.b2.utils.json.GsonService;
import io.github.kszatan.gocd.b2.utils.json.IncompleteJson;
import io.github.kszatan.gocd.b2.utils.json.InvalidJson;

import java.util.*;

public class ValidateRepositoryConfigurationRequest {
    private final RepositoryConfiguration configuration;

    public ValidateRepositoryConfigurationRequest(final String json) throws InvalidJson, IncompleteJson {
        Collection<String> missing = GsonService.validate(json,
                Arrays.asList("repository-configuration"));
        if (!missing.isEmpty()) {
            throw new IncompleteJson("Missing fields: " + missing.toString());
        }
        String configurationJson = GsonService.getField(json, "repository-configuration");
        missing.addAll(GsonService.validate(configurationJson,
                Arrays.asList("url", "bucketName", "accountId", "applicationKey")));
        if (!missing.isEmpty()) {
            throw new IncompleteJson("Missing fields: " + missing.toString());
        }
        configuration = GsonService.fromJson(configurationJson, RepositoryConfiguration.class);
    }

    public RepositoryConfiguration getConfiguration() {
        return configuration;
    }
}
