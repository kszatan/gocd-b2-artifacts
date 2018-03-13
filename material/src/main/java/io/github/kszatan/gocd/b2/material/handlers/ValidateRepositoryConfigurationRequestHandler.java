/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.github.kszatan.gocd.b2.material.handlers.bodies.ConfigurationValidationResponse;
import io.github.kszatan.gocd.b2.material.handlers.bodies.RepositoryConfiguration;
import io.github.kszatan.gocd.b2.material.handlers.bodies.ValidateRepositoryConfigurationRequest;
import io.github.kszatan.gocd.b2.utils.json.IncompleteJson;
import io.github.kszatan.gocd.b2.utils.json.InvalidJson;

public class ValidateRepositoryConfigurationRequestHandler implements RequestHandler {
    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) {
        GoPluginApiResponse response;
        try {
            ValidateRepositoryConfigurationRequest validateConfigurationRequest =
                    new ValidateRepositoryConfigurationRequest(request.requestBody());
            RepositoryConfiguration configuration = validateConfigurationRequest.getConfiguration();
            RepositoryConfigurationValidator validator = new RepositoryConfigurationValidator();
            ConfigurationValidationResponse validationResult = validator.validate(configuration);
            response = DefaultGoPluginApiResponse.success(validationResult.toJson());
        } catch (InvalidJson e) {
            response = DefaultGoPluginApiResponse.error("InvalidJSON: " + e.getMessage());
        } catch (IncompleteJson e) {
            response = DefaultGoPluginApiResponse.incompleteRequest(e.getMessage());
        }
        return response;
    }
}
