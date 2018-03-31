package io.github.kszatan.gocd.b2.material.handlers;

import io.github.kszatan.gocd.b2.material.handlers.bodies.ConfigurationValidationError;
import io.github.kszatan.gocd.b2.material.handlers.bodies.ConfigurationValidationResponse;
import io.github.kszatan.gocd.b2.material.handlers.bodies.RepositoryConfiguration;

public class RepositoryConfigurationValidator {
    public ConfigurationValidationResponse validate(RepositoryConfiguration configuration) {
        ConfigurationValidationResponse result = new ConfigurationValidationResponse();
        if (configuration.getBucketName().isEmpty()) {
            result.errors.add(new ConfigurationValidationError("bucketName", "Empty bucket name"));
        }
        if (configuration.getAccountId().isEmpty()) {
            result.errors.add(new ConfigurationValidationError("accountId", "Empty account ID"));
        }
        if (configuration.getApplicationKey().isEmpty()) {
            result.errors.add(new ConfigurationValidationError("applicationKey", "Empty application key"));
        }
        return result;
    }
}
