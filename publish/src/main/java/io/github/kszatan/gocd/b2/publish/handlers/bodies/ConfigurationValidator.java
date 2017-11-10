/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * op-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers.bodies;

import java.util.regex.Pattern;

public class ConfigurationValidator {
    private static final String BUCKET_ID_REGEX = "(?!b2-)[a-zA-Z0-9\\-]{6,50}";
    public static final Pattern pattern = Pattern.compile(BUCKET_ID_REGEX);

    public TaskConfigurationValidationResponse validate(TaskConfiguration configuration) {
        TaskConfigurationValidationResponse result = new TaskConfigurationValidationResponse();
        if (!validateBucketId(configuration.getBucketId())) {
            result.errors.put("bucketId", "Invalid Bucket ID format");
        }
        return result;
    }

    private Boolean validateBucketId(String bucketId) {
        return bucketId.isEmpty() || pattern.matcher(bucketId).matches();
    }
}
