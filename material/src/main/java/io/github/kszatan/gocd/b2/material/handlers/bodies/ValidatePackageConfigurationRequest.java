/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers.bodies;

import io.github.kszatan.gocd.b2.utils.json.GsonService;
import io.github.kszatan.gocd.b2.utils.json.IncompleteJson;
import io.github.kszatan.gocd.b2.utils.json.InvalidJson;

import java.util.Arrays;
import java.util.Collection;

public class ValidatePackageConfigurationRequest {
    private final PackageConfiguration packageConfiguration;
    private final RepositoryConfiguration repositoryConfiguration;

    public ValidatePackageConfigurationRequest(final String json) throws InvalidJson, IncompleteJson {
        ValidateRepositoryConfigurationRequest validateRepository = new ValidateRepositoryConfigurationRequest(json);
        repositoryConfiguration = validateRepository.getConfiguration();
        Collection<String> missing = GsonService.validate(json,
                Arrays.asList("package-configuration"));
        if (!missing.isEmpty()) {
            throw new IncompleteJson("Missing fields: " + missing.toString());
        }
        String configurationJson = GsonService.getField(json, "package-configuration");
        missing.addAll(GsonService.validate(configurationJson,
                Arrays.asList("pipelineName", "stageName", "jobName")));
        if (!missing.isEmpty()) {
            throw new IncompleteJson("Missing fields: " + missing.toString());
        }
        packageConfiguration = GsonService.fromJson(configurationJson, PackageConfiguration.class);
    }

    public RepositoryConfiguration getRepositoryConfiguration() {
        return repositoryConfiguration;
    }

    public PackageConfiguration getPackageConfiguration() {
        return packageConfiguration;
    }
}
