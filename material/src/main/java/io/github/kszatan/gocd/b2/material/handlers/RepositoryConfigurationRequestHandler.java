/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.github.kszatan.gocd.b2.material.handlers.bodies.RepositoryConfigurationDefinition;
import io.github.kszatan.gocd.b2.utils.json.GsonService;

public class RepositoryConfigurationRequestHandler implements RequestHandler {
    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) {
        return DefaultGoPluginApiResponse.success(
                GsonService.toJson(new RepositoryConfigurationDefinition()));
    }
}
