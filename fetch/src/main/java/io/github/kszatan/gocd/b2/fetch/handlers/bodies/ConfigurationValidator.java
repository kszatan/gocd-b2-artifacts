/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.fetch.handlers.bodies;

public class ConfigurationValidator {
    public TaskConfigurationValidationResponse validate(TaskConfiguration configuration) {
        TaskConfigurationValidationResponse result = new TaskConfigurationValidationResponse();
        String repositoryName = configuration.getRepositoryName();
        if (repositoryName == null || repositoryName.isEmpty()) {
            result.errors.put("repositoryName", "Missing repository name");
        }
        String packageName = configuration.getPackageName();
        if (packageName == null || packageName.isEmpty()) {
            result.errors.put("packageName", "Missing package name");
        }
        return result;
    }
}
