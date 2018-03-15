/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers.bodies;

import io.github.kszatan.gocd.b2.utils.json.IncompleteJson;
import io.github.kszatan.gocd.b2.utils.json.InvalidJson;

public class CheckRepositoryConnectionRequest {
    private final RepositoryConfiguration configuration;

    public CheckRepositoryConnectionRequest(final String json) throws InvalidJson, IncompleteJson {
        ValidateRepositoryConfigurationRequest validator = new ValidateRepositoryConfigurationRequest(json);
        configuration = validator.getConfiguration();
    }

    public RepositoryConfiguration getConfiguration() {
        return configuration;
    }
}
