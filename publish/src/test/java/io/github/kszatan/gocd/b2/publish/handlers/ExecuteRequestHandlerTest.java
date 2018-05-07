/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers;

import com.thoughtworks.go.plugin.api.request.DefaultGoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.github.kszatan.gocd.b2.publish.executor.TaskExecutor;
import io.github.kszatan.gocd.b2.publish.handlers.bodies.ExecuteResponse;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExecuteRequestHandlerTest {
    static private final String validRequestBody = "{\n" +
            "  \"context\": {\n" +
            "    \"workingDirectory\": \"pipelines/pipeline_name\",\n" +
            "    \"environmentVariables\": {\n" +
            "      \"GO_FROM_REVISION_MATERIAL\": \"743f18f79c3ce6d765159f02f26c65d57062436c\",\n" +
            "      \"GO_SERVER_URL\": \"https://localhost:8154/go\",\n" +
            "      \"GO_PIPELINE_LABEL\": \"10\",\n" +
            "      \"GO_STAGE_NAME\": \"stage_name\",\n" +
            "      \"GO_PIPELINE_NAME\": \"pipeline_name\",\n" +
            "      \"GO_STAGE_COUNTER\": \"1\",\n" +
            "      \"GO_PIPELINE_COUNTER\": \"10\",\n" +
            "      \"GO_JOB_NAME\": \"good_job\",\n" +
            "      \"B2_ACCOUNT_ID\": \"4abcdefgaf77\",\n" +
            "      \"GO_TRIGGER_USER\": \"changes\",\n" +
            "      \"GO_REVISION_ASDF\": \"743f18f79c3ce6d765159f02f26c65d57062436c\",\n" +
            "      \"GO_TO_REVISION_MATERIAL\": \"743f18f79c3ce6d765159f02f26c65d57062436c\",\n" +
            "      \"B2_APPLICATION_KEY\": \"caca85ed4e7a3404db0b08bb8256d00d84e247e46\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"config\": {\n" +
            "    \"sourceDestinations\": {\n" +
            "      \"secure\": false,\n" +
            "      \"value\": \"[{\\\"source\\\": \\\"**/file*\\\", \\\"destination\\\": \\\"desti/nation\\\"}, {\\\"source\\\": \\\"**\\\", \\\"destination\\\": \\\"\\\"}]\",\n" +
            "      \"required\": false\n" +
            "    },\n" +
            "    \"destinationPrefix\": {\n" +
            "      \"secure\": false,\n" +
            "      \"value\": \"OTHERS\",\n" +
            "      \"required\": false\n" +
            "    },\n" +
            "    \"bucketName\": {\n" +
            "      \"secure\": false,\n" +
            "      \"value\": \"bukhet\",\n" +
            "      \"required\": false\n" +
            "    }\n" +
            "  }\n" +
            "}";
    static private final DefaultGoPluginApiRequest validRequest = new DefaultGoPluginApiRequest("task", "1.0", "execute");
    private ExecuteRequestHandler handler;
    private TaskExecutor executor;

    public ExecuteRequestHandlerTest() {
        validRequest.setRequestBody(validRequestBody);
    }

    @Before
    public void setUp() throws Exception {
        handler = new ExecuteRequestHandler();
        executor = mock(TaskExecutor.class);
        handler.setExecutor(executor);
    }

    @Test
    public void handleShouldReturnSuccessResponseForValidRequest() throws Exception {
        when(executor.execute(any(), any())).thenReturn(ExecuteResponse.success("Success"));
        GoPluginApiResponse response = handler.handle(validRequest);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        assertThat(response.responseBody(), equalTo("{\"success\":true,\"message\":\"Success\"}"));

        when(executor.execute(any(), any())).thenReturn(ExecuteResponse.failure("Failure"));
        response = handler.handle(validRequest);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        assertThat(response.responseBody(), equalTo("{\"success\":false,\"message\":\"Failure\"}"));
    }

    @Test
    public void handleShouldReturnErrorResponseWhenGivenInvalidJson() {
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("task", "1.0", "execute");
        request.setRequestBody("Invalid JSON");
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), Matchers.equalTo(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
    }

    @Test
    public void handleShouldReturnErrorResponseGivenEmptyRequestBody() {
        DefaultGoPluginApiRequest request = validRequest;
        request.setRequestBody(null);
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
    }
    
    @Test
    public void handleShouldReturnErrorResponseForMissingBucketName() {
        final String bodyWithMissingBucketName = "{\n" +
                "  \"context\": {\n" +
                "    \"workingDirectory\": \"pipelines/pipeline_name\",\n" +
                "    \"environmentVariables\": {\n" +
                "      \"GO_FROM_REVISION_MATERIAL\": \"743f18f79c3ce6d765159f02f26c65d57062436c\",\n" +
                "      \"GO_SERVER_URL\": \"https://localhost:8154/go\",\n" +
                "      \"GO_PIPELINE_LABEL\": \"10\",\n" +
                "      \"GO_STAGE_NAME\": \"stage_name\",\n" +
                "      \"GO_PIPELINE_NAME\": \"pipeline_name\",\n" +
                "      \"GO_STAGE_COUNTER\": \"1\",\n" +
                "      \"GO_PIPELINE_COUNTER\": \"10\",\n" +
                "      \"GO_JOB_NAME\": \"good_job\",\n" +
                "      \"B2_ACCOUNT_ID\": \"4abcdefgaf77\",\n" +
                "      \"GO_TRIGGER_USER\": \"changes\",\n" +
                "      \"GO_REVISION_ASDF\": \"743f18f79c3ce6d765159f02f26c65d57062436c\",\n" +
                "      \"GO_TO_REVISION_MATERIAL\": \"743f18f79c3ce6d765159f02f26c65d57062436c\",\n" +
                "      \"B2_APPLICATION_KEY\": \"caca85ed4e7a3404db0b08bb8256d00d84e247e46\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"config\": {\n" +
                "    \"sourceDestinations\": {\n" +
                "      \"secure\": false,\n" +
                "      \"value\": \"[{\\\"source\\\": \\\"**/file*\\\", \\\"destination\\\": \\\"desti/nation\\\"}, {\\\"source\\\": \\\"**\\\", \\\"destination\\\": \\\"\\\"}]\",\n" +
                "      \"required\": false\n" +
                "    },\n" +
                "    \"destinationPrefix\": {\n" +
                "      \"secure\": false,\n" +
                "      \"value\": \"OTHERS\",\n" +
                "      \"required\": false\n" +
                "    }\n" +
                "  }\n" +
                "}";
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("task", "1.0", "execute");
        request.setRequestBody(bodyWithMissingBucketName);
        handler.setExecutor(null);
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
    }
}