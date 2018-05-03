/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.fetch.handlers;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.github.kszatan.gocd.b2.fetch.handlers.bodies.ConfigurationValidator;
import io.github.kszatan.gocd.b2.fetch.handlers.bodies.TaskConfiguration;
import io.github.kszatan.gocd.b2.fetch.handlers.bodies.TaskConfigurationRequest;
import io.github.kszatan.gocd.b2.fetch.handlers.bodies.TaskConfigurationValidationResponse;
import io.github.kszatan.gocd.b2.utils.json.IncompleteJson;
import io.github.kszatan.gocd.b2.utils.json.InvalidJson;

public class ValidateRequestHandler implements RequestHandler {
    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) {
        GoPluginApiResponse response;
        try {
            TaskConfigurationRequest configurationRequest = new TaskConfigurationRequest(request.requestBody());
            TaskConfiguration configuration = configurationRequest.getConfiguration();
            ConfigurationValidator validator = new ConfigurationValidator();
            TaskConfigurationValidationResponse validationResult = validator.validate(configuration);
            response = DefaultGoPluginApiResponse.success(validationResult.toJson());
        } catch (InvalidJson e) {
            response = DefaultGoPluginApiResponse.error("InvalidJSON: " + e.getMessage());
        } catch (IncompleteJson e) {
            response = DefaultGoPluginApiResponse.incompleteRequest(e.getMessage());
        }
        return response;
    }
}
