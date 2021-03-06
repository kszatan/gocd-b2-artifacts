/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers.bodies;

import io.github.kszatan.gocd.b2.utils.json.IncompleteJson;
import io.github.kszatan.gocd.b2.utils.json.InvalidJson;
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
        final String json = "{\n" +
                "  \"sourceDestinations\": {\n" +
                "    \"value\": \"[{\\\"source\\\":\\\"asdf\\\", \\\"destination\\\":\\\"fdsa\\\"}]\"\n" +
                "  },\n" +
                "  \"destinationPrefix\": {\n" +
                "    \"value\": \"destination/prefix\"\n" +
                "  },\n" +
                "  \"bucketName\": {\n" +
                "    \"value\": \"kszatan-bucket\"\n" +
                "  }\n" +
                "}";
        TaskConfigurationRequest request = new TaskConfigurationRequest(json);
        TaskConfiguration configuration = request.getConfiguration();
        assertThat(configuration.getSourceDestinations(), equalTo("[{\"source\":\"asdf\", \"destination\":\"fdsa\"}]"));
        assertThat(configuration.getSourceDestinationsAsList().size(), equalTo(1));
        SourceDestination sourceDestination = configuration.getSourceDestinationsAsList().iterator().next();
        assertThat(sourceDestination.source, equalTo("asdf"));
        assertThat(sourceDestination.destination, equalTo("fdsa"));
        assertThat(configuration.getDestinationPrefix(), equalTo("destination/prefix"));
        assertThat(configuration.getBucketName(), equalTo("kszatan-bucket"));
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