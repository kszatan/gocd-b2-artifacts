/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * op-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers.bodies;

import io.github.kszatan.gocd.b2.publish.json.IncompleteJson;
import io.github.kszatan.gocd.b2.publish.json.InvalidJson;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class TaskConfigurationRequestTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void constructorShouldParseCorrectJsonString() throws Exception {
        String json = "{\"destinationPrefix\":{\"value\":\"destination/prefix\"},\"bucketId\":{\"value\":\"kszatan-bucket\"}}";
        TaskConfigurationRequest request = new TaskConfigurationRequest(json);
        TaskConfiguration configuration = request.getConfiguration();
        assertThat(configuration.getDestinationPrefix(), equalTo("destination/prefix"));
        assertThat(configuration.getBucketId(), equalTo("kszatan-bucket"));
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