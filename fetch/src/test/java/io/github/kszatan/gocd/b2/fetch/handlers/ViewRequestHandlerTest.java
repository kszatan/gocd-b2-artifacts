/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.fetch.handlers;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.github.kszatan.gocd.b2.fetch.handlers.bodies.TaskConfigurationView;
import io.github.kszatan.gocd.b2.utils.json.GsonService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class ViewRequestHandlerTest {
    private ViewRequestHandler handler;
    @Before
    public void setUp() throws Exception {
        handler = new ViewRequestHandler();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void handleShouldReturnSuccessResponseCode() throws Exception {
        GoPluginApiResponse response = handler.handle(mock(GoPluginApiRequest.class));
        assertThat(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE, equalTo(response.responseCode()));
    }

    @Test
    public void handleShouldReturnValidView() throws Exception {
        GoPluginApiResponse response = handler.handle(mock(GoPluginApiRequest.class));
        TaskConfigurationView configuration = GsonService.fromJson(response.responseBody(), TaskConfigurationView.class);
        assertThat(configuration.displayValue, notNullValue());
        assertThat(configuration.template, notNullValue());
    }
}