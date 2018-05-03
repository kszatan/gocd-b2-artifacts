/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.fetch.handlers.bodies;

import io.github.kszatan.gocd.b2.utils.json.IncompleteJson;
import io.github.kszatan.gocd.b2.utils.json.InvalidJson;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class TaskConfigurationRequestTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final String defaultRequestJson = "{\n" +
            "  \"destination\": {\n" +
            "    \"secure\": false,\n" +
            "    \"value\": \"dest\",\n" +
            "    \"required\": false\n" +
            "  },\n" +
            "  \"packageName\": {\n" +
            "    \"secure\": false,\n" +
            "    \"value\": \"package\",\n" +
            "    \"required\": false\n" +
            "  },\n" +
            "  \"repositoryName\": {\n" +
            "    \"secure\": false,\n" +
            "    \"value\": \"repository\",\n" +
            "    \"required\": false\n" +
            "  }\n" +
            "}";

    @Test
    public void constructorShouldParseCorrectJsonString() throws Exception {
        TaskConfigurationRequest request = new TaskConfigurationRequest(defaultRequestJson);
        TaskConfiguration configuration = request.getConfiguration();
        assertThat(configuration.getRepositoryName(), equalTo("repository"));
        assertThat(configuration.getPackageName(), equalTo("package"));
        assertThat(configuration.getDestination(), equalTo("dest"));
    }

    @Test
    public void constructorShouldThrowGivenIncompleteJson() throws Exception {
        thrown.expect(IncompleteJson.class);
        thrown.expectMessage("Missing fields: ");
        String json = "{}";
        new TaskConfigurationRequest(json);
    }

    @Test
    public void constructorShouldThrowGivenInvalidJson() throws Exception {
        thrown.expect(InvalidJson.class);
        thrown.expectMessage("Malformed JSON: ");
        String json = "Invalid JSON";
        new TaskConfigurationRequest(json);
    }
}