/*
 * Copyright (c) 2018. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.fetch.handlers.bodies;

import io.github.kszatan.gocd.b2.utils.json.IncompleteJson;
import io.github.kszatan.gocd.b2.utils.json.InvalidJson;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class ExecuteRequestTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final String defaultRequestJson = "{\n" +
            "  \"context\": {\n" +
            "    \"workingDirectory\": \"pipelines/up42\",\n" +
            "    \"environmentVariables\": {\n" +
            "      \"GO_PACKAGE_BUKHET_UP42_JOBNAME\": \"up42_job\",\n" +
            "      \"GO_PACKAGE_BUKHET_UP42_STAGENAME\": \"up42_stage\",\n" +
            "      \"GO_SERVER_URL\": \"https://localhost:8154/go\",\n" +
            "      \"GO_REPO_BUKHET_UP42_ACCOUNTID\": \"account_id\",\n" +
            "      \"GO_PIPELINE_LABEL\": \"65\",\n" +
            "      \"GO_STAGE_NAME\": \"stejdz\",\n" +
            "      \"GO_PACKAGE_BUKHET_UP42_LABEL\": \"63.1\",\n" +
            "      \"GO_PIPELINE_NAME\": \"up42\",\n" +
            "      \"GO_STAGE_COUNTER\": \"1\",\n" +
            "      \"GO_PACKAGE_BUKHET_UP42_PIPELINENAME\": \"up42\",\n" +
            "      \"GO_PIPELINE_COUNTER\": \"65\",\n" +
            "      \"GO_JOB_NAME\": \"dzob\",\n" +
            "      \"B2_ACCOUNT_ID\": \"account_id\",\n" +
            "      \"GO_TRIGGER_USER\": \"changes\",\n" +
            "      \"GO_REPO_BUKHET_UP42_APPLICATIONKEY\": \"appkey\",\n" +
            "      \"GO_REPO_BUKHET_UP42_BUCKETNAME\": \"bukhet\",\n" +
            "      \"B2_APPLICATION_KEY\": \"application_key\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"config\": {\n" +
            "    \"destination\": {\n" +
            "      \"secure\": false,\n" +
            "      \"value\": \"dest\",\n" +
            "      \"required\": false\n" +
            "    },\n" +
            "    \"packageName\": {\n" +
            "      \"secure\": false,\n" +
            "      \"value\": \"up42\",\n" +
            "      \"required\": false\n" +
            "    },\n" +
            "    \"repositoryName\": {\n" +
            "      \"secure\": false,\n" +
            "      \"value\": \"bukhet\",\n" +
            "      \"required\": false\n" +
            "    }\n" +
            "  }\n" +
            "}";

    @Test
    public void constructorShouldParseCorrectJsonString() throws Exception {
        ExecuteRequest request = ExecuteRequest.create(defaultRequestJson);
        TaskConfiguration configuration = request.getTaskConfiguration();
        assertThat(configuration.getRepositoryName(), equalTo("bukhet"));
        assertThat(configuration.getPackageName(), equalTo("up42"));
        assertThat(configuration.getDestination(), equalTo("dest"));
        TaskContext context = request.getTaskContext();
        assertThat(context.workingDirectory, equalTo("pipelines/up42"));
        assertThat(context.environmentVariables.size(), equalTo(17));
        assertThat(context.environmentVariables.get("GO_PACKAGE_BUKHET_UP42_JOBNAME"), equalTo("up42_job"));
        assertThat(context.environmentVariables.get("GO_PACKAGE_BUKHET_UP42_STAGENAME"), equalTo("up42_stage"));
        assertThat(context.environmentVariables.get("GO_SERVER_URL"), equalTo("https://localhost:8154/go"));
        assertThat(context.environmentVariables.get("GO_REPO_BUKHET_UP42_ACCOUNTID"), equalTo("account_id"));
        assertThat(context.environmentVariables.get("GO_PIPELINE_LABEL"), equalTo("65"));
        assertThat(context.environmentVariables.get("GO_STAGE_NAME"), equalTo("stejdz"));
        assertThat(context.environmentVariables.get("GO_PACKAGE_BUKHET_UP42_LABEL"), equalTo("63.1"));
        assertThat(context.environmentVariables.get("GO_PIPELINE_NAME"), equalTo("up42"));
        assertThat(context.environmentVariables.get("GO_PIPELINE_COUNTER"), equalTo("65"));
        assertThat(context.environmentVariables.get("GO_STAGE_COUNTER"), equalTo("1"));
        assertThat(context.environmentVariables.get("GO_PACKAGE_BUKHET_UP42_PIPELINENAME"), equalTo("up42"));
        assertThat(context.environmentVariables.get("GO_PIPELINE_COUNTER"), equalTo("65"));
        assertThat(context.environmentVariables.get("GO_JOB_NAME"), equalTo("dzob"));
        assertThat(context.environmentVariables.get("B2_ACCOUNT_ID"), equalTo("account_id"));
        assertThat(context.environmentVariables.get("GO_TRIGGER_USER"), equalTo("changes"));
        assertThat(context.environmentVariables.get("GO_REPO_BUKHET_UP42_APPLICATIONKEY"), equalTo("appkey"));
        assertThat(context.environmentVariables.get("GO_REPO_BUKHET_UP42_BUCKETNAME"), equalTo("bukhet"));
        assertThat(context.environmentVariables.get("B2_APPLICATION_KEY"), equalTo("application_key"));
    }

    @Test
    public void constructorShouldThrowWhenConfigFieldIsMissing() throws Exception {
        final String noConfigJson = "{\n" +
                "  \"context\": {\n" +
                "    \"workingDirectory\": \"pipelines/up42\",\n" +
                "    \"environmentVariables\": {\n" +
                "      \"GO_REPO_BUKHET_UP42_BUCKETNAME\": \"bukhet\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        thrown.expect(IncompleteJson.class);
        thrown.expectMessage("Missing fields: ");
        ExecuteRequest.create(noConfigJson);
    }

    @Test
    public void constructorShouldThrowWhenContextFieldIsMissing() throws Exception {
        final String noContextJson = "{\n" +
                "  \"config\": {\n" +
                "    \"destination\": {\n" +
                "      \"secure\": false,\n" +
                "      \"value\": \"dest\",\n" +
                "      \"required\": false\n" +
                "    },\n" +
                "    \"packageName\": {\n" +
                "      \"secure\": false,\n" +
                "      \"value\": \"up42\",\n" +
                "      \"required\": false\n" +
                "    },\n" +
                "    \"repositoryName\": {\n" +
                "      \"secure\": false,\n" +
                "      \"value\": \"bukhet\",\n" +
                "      \"required\": false\n" +
                "    }\n" +
                "  }\n" +
                "}";
        thrown.expect(IncompleteJson.class);
        thrown.expectMessage("Missing fields: ");
        ExecuteRequest.create(noContextJson);
    }
    
    @Test
    public void constructorShouldThrowGivenInvalidJson() throws Exception {
        thrown.expect(InvalidJson.class);
        thrown.expectMessage("Malformed JSON: ");
        String json = "Invalid JSON";
        ExecuteRequest.create(json);;
    }
}