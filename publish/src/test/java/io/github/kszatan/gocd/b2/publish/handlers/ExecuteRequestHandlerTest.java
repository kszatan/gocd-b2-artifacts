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
    static private final String validRequestBody = "{\"context\":{\"workingDirectory\":\"pipelines/pipeline_name\",\"environmentVariables\":{\"GO_FROM_REVISION_MATERIAL\":\"743f18f79c3ce6d765159f02f26c65d57062436c\",\"GO_SERVER_URL\":\"https://localhost:8154/go\",\"GO_PIPELINE_LABEL\":\"10\",\"GO_STAGE_NAME\":\"stage_name\",\"GO_PIPELINE_NAME\":\"pipeline_name\",\"GO_STAGE_COUNTER\":\"1\",\"GO_PIPELINE_COUNTER\":\"10\",\"GO_JOB_NAME\":\"good_job\",\"B2_ACCOUNT_ID\":\"4abcdefgaf77\",\"GO_TRIGGER_USER\":\"changes\",\"GO_REVISION_ASDF\":\"743f18f79c3ce6d765159f02f26c65d57062436c\",\"GO_TO_REVISION_MATERIAL\":\"743f18f79c3ce6d765159f02f26c65d57062436c\",\"B2_APPLICATION_KEY\":\"caca85ed4e7a3404db0b08bb8256d00d84e247e46\"}},\"config\":{\"sourceDestinations\":{\"secure\":false,\"value\":\"[{\\\"source\\\": \\\"**/file*\\\", \\\"destination\\\": \\\"desti/nation\\\"}, {\\\"source\\\": \\\"**\\\", \\\"destination\\\": \\\"\\\"}]\",\"required\":false},\"destinationPrefix\":{\"secure\":false,\"value\":\"OTHERS\",\"required\":false},\"bucketId\":{\"secure\":false,\"value\":\"bukhet\",\"required\":false}}}";
    static private final DefaultGoPluginApiRequest validRequest = new DefaultGoPluginApiRequest("task", "1.0", "checkout");
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
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("task", "1.0", "validate");
        request.setRequestBody("Invalid JSON");
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), Matchers.equalTo(DefaultGoPluginApiResponse.INTERNAL_ERROR));
    }

    @Test
    public void handleShouldReturnErrorResponseGivenEmptyRequestBody() {
        DefaultGoPluginApiRequest request = validRequest;
        request.setRequestBody(null);
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.INTERNAL_ERROR));
    }
    
    @Test
    public void handleShouldReturnErrorResponseForMissingBucketId() {
        final String bodyWithMissingBucketId = "{\"context\":{\"workingDirectory\":\"pipelines/pipeline_name\",\"environmentVariables\":{\"GO_FROM_REVISION_MATERIAL\":\"743f18f79c3ce6d765159f02f26c65d57062436c\",\"GO_SERVER_URL\":\"https://localhost:8154/go\",\"GO_PIPELINE_LABEL\":\"10\",\"GO_STAGE_NAME\":\"stage_name\",\"GO_PIPELINE_NAME\":\"pipeline_name\",\"GO_STAGE_COUNTER\":\"1\",\"GO_PIPELINE_COUNTER\":\"10\",\"GO_JOB_NAME\":\"good_job\",\"B2_ACCOUNT_ID\":\"4abcdefgaf77\",\"GO_TRIGGER_USER\":\"changes\",\"GO_REVISION_ASDF\":\"743f18f79c3ce6d765159f02f26c65d57062436c\",\"GO_TO_REVISION_MATERIAL\":\"743f18f79c3ce6d765159f02f26c65d57062436c\",\"B2_APPLICATION_KEY\":\"caca85ed4e7a3404db0b08bb8256d00d84e247e46\"}},\"config\":{\"sourceDestinations\":{\"secure\":false,\"value\":\"[{\\\"source\\\": \\\"**/file*\\\", \\\"destination\\\": \\\"desti/nation\\\"}, {\\\"source\\\": \\\"**\\\", \\\"destination\\\": \\\"\\\"}]\",\"required\":false},\"destinationPrefix\":{\"secure\":false,\"value\":\"OTHERS\",\"required\":false}}}";
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("task", "1.0", "publish");
        request.setRequestBody(bodyWithMissingBucketId);
        handler.setExecutor(null);
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.INTERNAL_ERROR));
    }
}