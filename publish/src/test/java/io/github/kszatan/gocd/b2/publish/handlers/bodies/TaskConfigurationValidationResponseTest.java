/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers.bodies;

import com.google.gson.Gson;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class TaskConfigurationValidationResponseTest {
    @Test
    public void toJsonShouldIncludeAllErrors() throws Exception {
        TaskConfigurationValidationResponse result = new TaskConfigurationValidationResponse();
        result.errors.put("Key1", "Message1");
        result.errors.put("Key2", "Message2");
        String json = result.toJson();

        Gson gson = new Gson();

        TaskConfigurationValidationResponse resultFromJson = gson.fromJson(json, TaskConfigurationValidationResponse.class);
        assertThat(resultFromJson, equalTo(result));
    }
}