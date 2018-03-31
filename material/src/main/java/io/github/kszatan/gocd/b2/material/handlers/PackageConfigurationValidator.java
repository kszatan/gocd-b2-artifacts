package io.github.kszatan.gocd.b2.material.handlers;

import io.github.kszatan.gocd.b2.material.handlers.bodies.ConfigurationValidationError;
import io.github.kszatan.gocd.b2.material.handlers.bodies.ConfigurationValidationResponse;
import io.github.kszatan.gocd.b2.material.handlers.bodies.PackageConfiguration;
import io.github.kszatan.gocd.b2.material.handlers.bodies.RepositoryConfiguration;

public class PackageConfigurationValidator {
    public ConfigurationValidationResponse validate(PackageConfiguration configuration) {
        ConfigurationValidationResponse result = new ConfigurationValidationResponse();
        if (configuration.getPipelineName().isEmpty()) {
            result.errors.add(new ConfigurationValidationError("pipelineName", "Empty pipeline name"));
        }
        if (configuration.getStageName().isEmpty()) {
            result.errors.add(new ConfigurationValidationError("stageName", "Empty stage name"));
        }
        if (configuration.getJobName().isEmpty()) {
            result.errors.add(new ConfigurationValidationError("jobName", "Empty job name"));
        }
        return result;
    }
}
