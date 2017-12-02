/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.github.kszatan.gocd.b2.publish.handlers.bodies.ConfigurationValidator;
import io.github.kszatan.gocd.b2.publish.handlers.bodies.TaskConfiguration;
import io.github.kszatan.gocd.b2.publish.handlers.bodies.ExecuteRequest;
import io.github.kszatan.gocd.b2.publish.handlers.bodies.TaskConfigurationValidationResponse;
import io.github.kszatan.gocd.b2.publish.json.IncompleteJson;
import io.github.kszatan.gocd.b2.publish.json.InvalidJson;

public class ExecuteRequestHandler implements RequestHandler {
    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) {
        GoPluginApiResponse response;
        try {
            ExecuteRequest configurationRequest = ExecuteRequest.create(request.requestBody());
            TaskConfiguration configuration = configurationRequest.getTaskConfiguration();
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
