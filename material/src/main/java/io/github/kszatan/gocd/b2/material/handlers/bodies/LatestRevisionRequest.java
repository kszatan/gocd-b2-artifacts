/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers.bodies;

import io.github.kszatan.gocd.b2.utils.json.GsonService;
import io.github.kszatan.gocd.b2.utils.json.IncompleteJson;
import io.github.kszatan.gocd.b2.utils.json.InvalidJson;

import java.util.Arrays;
import java.util.Collection;

public class LatestRevisionRequest {
    private final PackageConfiguration packageConfiguration;
    private final RepositoryConfiguration repositoryConfiguration;

    public LatestRevisionRequest(final String json) throws InvalidJson, IncompleteJson {
        ValidatePackageConfigurationRequest validator = new ValidatePackageConfigurationRequest(json);
        repositoryConfiguration = validator.getRepositoryConfiguration();
        packageConfiguration = validator.getPackageConfiguration();
    }

    public RepositoryConfiguration getRepositoryConfiguration() {
        return repositoryConfiguration;
    }

    public PackageConfiguration getPackageConfiguration() {
        return packageConfiguration;
    }
}
