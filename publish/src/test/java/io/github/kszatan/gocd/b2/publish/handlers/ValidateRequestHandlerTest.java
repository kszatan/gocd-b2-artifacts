/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * op-level directory of this distribution.
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
    @Before
    public void setUp() throws Exception {
        handler = new ValidateRequestHandler();
    }

    @Test
    public void handleShouldReturnNonNullResponseForValidateConfigurationRequest() throws UnhandledRequestTypeException {
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("task", "1.0", "validate");
        request.setRequestBody("{\"destination-prefix\":{\"value\":\"destination-prefix\"},\"bucket-id\":{\"value\":\"kszatan-bucket\"}}");
        assertNotNull(handler.handle(request));
    }

    @Test
    public void handleShouldReturnErrorResponseForInvalidBucketId() throws UnhandledRequestTypeException {
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("task", "1.0", "validate");
        request.setRequestBody("{\"destination-prefix\":{\"value\":\"destination-prefix\"},\"bucket-id\":{\"value\":\"b2-bucket\"}}");
        assertNotNull(handler.handle(request));
    }

    @Test
    public void handleShouldReturnErrorResponseWhenGivenInvalidJson() {
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("task", "1.0", "validate");
        request.setRequestBody("Invalid JSON");
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.INTERNAL_ERROR));
    }
}