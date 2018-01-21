/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * op-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers.bodies;

import com.google.gson.JsonSyntaxException;

import java.util.List;
import java.util.regex.Pattern;

public class ConfigurationValidator {
    private static final String BUCKET_ID_REGEX = "(?!b2-)[a-zA-Z0-9\\-]{6,50}";
    public static final Pattern pattern = Pattern.compile(BUCKET_ID_REGEX);

    public TaskConfigurationValidationResponse validate(TaskConfiguration configuration) {
        TaskConfigurationValidationResponse result = new TaskConfigurationValidationResponse();
        if (configuration.getSourceDestinations().isEmpty()) {
            result.errors.put("sourceDestinations", "Empty source destinations");
        } else {
            try {
                List<SourceDestination> sourceDestinations = configuration.getSourceDestinationsAsList();
                if (sourceDestinations == null || sourceDestinations.isEmpty()) {
                    result.errors.put("sourceDestinations", "Empty source destinations");
                } else if (sourceDestinations.stream().anyMatch(sd -> sd.source.isEmpty())){
                    result.errors.put("sourceDestinations", "Source cannot be empty");
                }
            } catch (JsonSyntaxException ex) {
                result.errors.put("sourceDestinations", "Unable to deserialize sourceDestination JSON: " + ex.getMessage());
            }
        }
        if (!validateBucketName(configuration.getBucketName())) {
            result.errors.put("bucketName", "Invalid bucket name format");
        }
        return result;
    }

    public Boolean validateBucketName(String bucketName) {
        return bucketName.isEmpty() || pattern.matcher(bucketName).matches();
    }
}
