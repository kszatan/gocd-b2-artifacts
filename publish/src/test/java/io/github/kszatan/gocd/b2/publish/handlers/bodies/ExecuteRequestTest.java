/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers.bodies;

import io.github.kszatan.gocd.b2.utils.json.IncompleteJson;
import io.github.kszatan.gocd.b2.utils.json.InvalidJson;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class ExecuteRequestTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void constructorShouldParseCorrectJsonString() throws Exception {
        String json = "{\"config\": {\"sourceDestinations\":{\"value\":\"[{\\\"source\\\":\\\"asdf\\\", \\\"destination\\\":\\\"fdsa\\\"}]\"},\"destinationPrefix\":{\"value\":\"destination/prefix\"},\"bucketName\":{\"value\":\"kszatan-bucket\"}}, \"context\": { \"workingDirectory\": \"working-dir\", \"environmentVariables\": { \"ENV1\":\"VAL1\", \"ENV2\": \"VAL2\" }}}";
        ExecuteRequest request = ExecuteRequest.create(json);
        TaskConfiguration configuration = request.getTaskConfiguration();
        assertThat(configuration.getSourceDestinations(), Matchers.equalTo("[{\"source\":\"asdf\", \"destination\":\"fdsa\"}]"));
        assertThat(configuration.getSourceDestinationsAsList().size(), Matchers.equalTo(1));
        SourceDestination sourceDestination = configuration.getSourceDestinationsAsList().iterator().next();
        assertThat(sourceDestination.source, Matchers.equalTo("asdf"));
        assertThat(sourceDestination.destination, Matchers.equalTo("fdsa"));
        assertThat(configuration.getDestinationPrefix(), Matchers.equalTo("destination/prefix"));
        assertThat(configuration.getBucketName(), Matchers.equalTo("kszatan-bucket"));
        TaskContext context = request.getTaskContext();
        assertThat(context.workingDirectory, equalTo("working-dir"));
        assertThat(context.environmentVariables.size(), equalTo(2));
        assertThat(context.environmentVariables.get("ENV1"), equalTo("VAL1"));
        assertThat(context.environmentVariables.get("ENV2"), equalTo("VAL2"));
    }

    @Test
    public void constructorShouldThrowWhenDestinationFolderFieldIsMissing() throws Exception {
        thrown.expect(IncompleteJson.class);
        thrown.expectMessage("Missing fields: ");
        String json = "{\"scm-configuration\":{\"url\":{\"value\":\"https://github.com/kszatan/gocd-phabricator-staging-material.git\"}},\"revision\": {\"revision\": \"revision-1\",\"timestamp\": \"2011-07-14T19:43:37.100Z\",\"data\":{}}}";
        ExecuteRequest.create(json);;
    }

    @Test
    public void constructorShouldThrowWhenRevisionFieldIsMissing() throws Exception {
        thrown.expect(IncompleteJson.class);
        thrown.expectMessage("Missing fields: ");
        String json = "{\"scm-configuration\":{\"url\":{\"value\":\"https://github.com/kszatan/gocd-phabricator-staging-material.git\"}},\"destination-folder\":\"/var/lib/go-agent/pipelines/pipeline-name/destination\"}";
        ExecuteRequest.create(json);
    }

    @Test
    public void constructorShouldThrowWhenScmConfigurationFieldIsMissing() throws Exception {
        thrown.expect(IncompleteJson.class);
        thrown.expectMessage("Missing fields: ");
        String json = "{\"destination-folder\":\"/var/lib/go-agent/pipelines/pipeline-name/destination\",\"revision\": {\"revision\": \"revision-1\",\"timestamp\": \"2011-07-14T19:43:37.100Z\",\"data\":{}}}";
        ExecuteRequest.create(json);
    }

    @Test
    public void constructorShouldThrowGivenInvalidJson() throws Exception {
        thrown.expect(InvalidJson.class);
        thrown.expectMessage("Malformed JSON: ");
        String json = "Invalid JSON";
        ExecuteRequest.create(json);;
    }
}