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
import io.github.kszatan.gocd.b2.material.handlers.bodies.PackageConfiguration;
import io.github.kszatan.gocd.b2.material.handlers.bodies.RepositoryConfiguration;
import io.github.kszatan.gocd.b2.material.handlers.bodies.ValidatePackageConfigurationRequest;
import io.github.kszatan.gocd.b2.utils.json.IncompleteJson;
import io.github.kszatan.gocd.b2.utils.json.InvalidJson;

public class ValidatePackageConfigurationRequestHandler implements RequestHandler {
    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) {
        GoPluginApiResponse response;
        try {
            ValidatePackageConfigurationRequest configurationRequest =
                    new ValidatePackageConfigurationRequest(request.requestBody());
            PackageConfiguration packageConfiguration = configurationRequest.getPackageConfiguration();
            RepositoryConfiguration repositoryConfiguration = configurationRequest.getRepositoryConfiguration();
            RepositoryConfigurationValidator repositoryValidator = new RepositoryConfigurationValidator();
            ConfigurationValidationResponse repositoryValidationResult = repositoryValidator.validate(repositoryConfiguration);
            if (!repositoryValidationResult.errors.isEmpty()) {
                response = DefaultGoPluginApiResponse.success(repositoryValidationResult.toJson());
            } else {
                PackageConfigurationValidator packageValidator = new PackageConfigurationValidator();
                ConfigurationValidationResponse packageValidationResult = packageValidator.validate(packageConfiguration);
                response = DefaultGoPluginApiResponse.success(packageValidationResult.toJson());
            }
        } catch (InvalidJson e) {
            response = DefaultGoPluginApiResponse.error("InvalidJSON: " + e.getMessage());
        } catch (IncompleteJson e) {
            response = DefaultGoPluginApiResponse.incompleteRequest(e.getMessage());
        }
        return response;
    }
}
