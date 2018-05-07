/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers;

import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.request.DefaultGoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class ValidateRequestHandlerTest {
    private ValidateRequestHandler handler;
    private DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("task", "1.0", "validate");
    
    @Before
    public void setUp() throws Exception {
        handler = new ValidateRequestHandler();
    }

    @Test
    public void handleShouldReturnNonNullResponseForValidateConfigurationRequest() throws Exception {
        final String requestBody = "{\n" +
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
        request.setRequestBody(requestBody);
        assertNotNull(handler.handle(request));
    }

    @Test
    public void handleShouldReturnErrorResponseForInvalidBucketName() throws Exception {
        final String requestBody = "{\n" +
                "  \"sourceDestinations\": {\n" +
                "    \"value\": \"[{\\\"source\\\":\\\"asdf\\\", \\\"destination\\\":\\\"fdsa\\\"}]\"\n" +
                "  },\n" +
                "  \"destination-prefix\": {\n" +
                "    \"value\": \"destination-prefix\"\n" +
                "  },\n" +
                "  \"bucket-id\": {\n" +
                "    \"value\": \"b2-bucket\"\n" +
                "  }\n" +
                "}";
        request.setRequestBody(requestBody);
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.VALIDATION_FAILED));
    }
    
    @Test
    public void handleShouldReturnErrorResponseForEmptySourceDestinations() throws Exception {
        final String requestBody = "{\n" +
                "  \"sourceDestinations\": {\n" +
                "    \"value\": \"[]\"\n" +
                "  },\n" +
                "  \"destination-prefix\": {\n" +
                "    \"value\": \"destination-prefix\"\n" +
                "  },\n" +
                "  \"bucket-id\": {\n" +
                "    \"value\": \"b2-bucket\"\n" +
                "  }\n" +
                "}";
        request.setRequestBody(requestBody);
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.VALIDATION_FAILED));
    }

    @Test
    public void handleShouldReturnErrorResponseWhenGivenInvalidJson() {
        request.setRequestBody("Invalid JSON");
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.INTERNAL_ERROR));
    }
}