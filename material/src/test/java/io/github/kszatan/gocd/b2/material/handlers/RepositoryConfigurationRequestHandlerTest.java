/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.github.kszatan.gocd.b2.material.handlers.bodies.RepositoryConfigurationDefinition;
import io.github.kszatan.gocd.b2.utils.json.GsonService;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

public class RepositoryConfigurationRequestHandlerTest {
    private RepositoryConfigurationRequestHandler handler;
    
    @Before
    public void setUp() throws Exception {
        handler = new RepositoryConfigurationRequestHandler();
    }

    @Test
    public void handleShouldReturnSuccessResponseCode() throws Exception {
        GoPluginApiResponse response = handler.handle(mock(GoPluginApiRequest.class));
        assertThat(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE, equalTo(response.responseCode()));
    }

    @Test
    public void handleShouldReturnValidConfiguration() throws Exception {
        GoPluginApiResponse response = handler.handle(mock(GoPluginApiRequest.class));
        RepositoryConfigurationDefinition definition = GsonService.fromJson(response.responseBody(), RepositoryConfigurationDefinition.class);
        assertThat(definition.accountId, notNullValue());
        assertThat(definition.applicationKey, notNullValue());
        assertThat(definition.bucketName, notNullValue());
    }

}
