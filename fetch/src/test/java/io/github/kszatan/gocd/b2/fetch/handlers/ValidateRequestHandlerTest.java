/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.fetch.handlers;

import com.thoughtworks.go.plugin.api.request.DefaultGoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.github.kszatan.gocd.b2.fetch.handlers.bodies.TaskConfigurationValidationResponse;
import io.github.kszatan.gocd.b2.utils.json.GsonService;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class ValidateRequestHandlerTest {
    private ValidateRequestHandler handler;
    private DefaultGoPluginApiRequest request;

    @Before
    public void setUp() throws Exception {
        handler = new ValidateRequestHandler();
        request = new DefaultGoPluginApiRequest("task", "1.0", "validate");
    }

    @Test
    public void handleShouldReturnNonNullResponseForValidateConfigurationRequest() throws Exception {
        String requestBody = "{\n" +
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
        request.setRequestBody(requestBody);
        assertNotNull(handler.handle(request));
    }

    @Test
    public void handleShouldReturnErrorResponseForEmptyRepositoryName() throws Exception {
        String requestBody = "{\n" +
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
                "    \"value\": \"\",\n" +
                "    \"required\": false\n" +
                "  }\n" +
                "}";
        request.setRequestBody(requestBody);
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        TaskConfigurationValidationResponse r = GsonService.fromJson(response.responseBody(), TaskConfigurationValidationResponse.class);
        assertThat(r.errors.size(), equalTo(1));
        assertThat(r.errors.get("repositoryName"), equalTo("Missing repository name"));
    }


    @Test
    public void handleShouldReturnErrorResponseForEmptyPackageName() throws Exception {
        String requestBody = "{\n" +
                "  \"destination\": {\n" +
                "    \"secure\": false,\n" +
                "    \"value\": \"\",\n" +
                "    \"required\": false\n" +
                "  },\n" +
                "  \"packageName\": {\n" +
                "    \"secure\": false,\n" +
                "    \"value\": \"\",\n" +
                "    \"required\": false\n" +
                "  },\n" +
                "  \"repositoryName\": {\n" +
                "    \"secure\": false,\n" +
                "    \"value\": \"repository\",\n" +
                "    \"required\": false\n" +
                "  }\n" +
                "}";
        request.setRequestBody(requestBody);
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        TaskConfigurationValidationResponse r = GsonService.fromJson(response.responseBody(), TaskConfigurationValidationResponse.class);
        assertThat(r.errors.size(), equalTo(1));
        assertThat(r.errors.get("packageName"), equalTo("Missing package name"));
    }

    @Test
    public void handleShouldReturnErrorResponseWhenGivenInvalidJson() {
        request.setRequestBody("Invalid JSON");
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.INTERNAL_ERROR));
    }
}