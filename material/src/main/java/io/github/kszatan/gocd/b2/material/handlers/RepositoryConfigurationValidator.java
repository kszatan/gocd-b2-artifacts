package io.github.kszatan.gocd.b2.material.handlers;

import io.github.kszatan.gocd.b2.material.handlers.bodies.ConfigurationValidationResponse;
import io.github.kszatan.gocd.b2.material.handlers.bodies.RepositoryConfiguration;

public class RepositoryConfigurationValidator {
    public ConfigurationValidationResponse validate(RepositoryConfiguration configuration) {
        ConfigurationValidationResponse result = new ConfigurationValidationResponse();
        if (configuration.getUrl().isEmpty()) {
            result.errors.put("url", "Empty repository URL");
        }
        if (configuration.getBucketName().isEmpty()) {
            result.errors.put("bucketName", "Empty bucket name");
        }
        if (configuration.getAccountId().isEmpty()) {
            result.errors.put("accountId", "Empty account ID");
        }
        if (configuration.getApplicationKey().isEmpty()) {
            result.errors.put("applicationKey", "Empty application key");
        }
        return result;
    }
}
