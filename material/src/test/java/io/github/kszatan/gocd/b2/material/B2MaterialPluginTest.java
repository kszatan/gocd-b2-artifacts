/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material;

import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.request.DefaultGoPluginApiRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.github.kszatan.gocd.b2.material.handlers.RequestHandler;
import io.github.kszatan.gocd.b2.material.handlers.RequestHandlerFactory;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class B2MaterialPluginTest {
    private B2MaterialPlugin plugin;
    private RequestHandlerFactory requestHandlerFactory;
    private RequestHandler requestHandler;

    @Before
    public void setUp() throws Exception {
        requestHandlerFactory = mock(RequestHandlerFactory.class);
        plugin = new B2MaterialPlugin(requestHandlerFactory);
        requestHandler = mock(RequestHandler.class);
    }

    @Test
    public void handleShouldReturnBadRequestResponseInCaseOfUnhandledRequestTypeException() throws Exception {
        when(requestHandlerFactory.create(anyString())).thenThrow(UnhandledRequestTypeException.class);

        GoPluginApiRequest request = new DefaultGoPluginApiRequest("task", "1.0", "unhandled-request");
        GoPluginApiResponse response = plugin.handle(request);

        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.BAD_REQUEST));
    }

    @Test
    public void handleShouldReturnErrorResponseInCaseOfUnknownException() throws Exception {
        when(requestHandlerFactory.create(anyString())).thenThrow(Exception.class);

        GoPluginApiRequest request = new DefaultGoPluginApiRequest("task", "1.0", RequestHandlerFactory.CHECK_PACKAGE_CONNECTION);
        GoPluginApiResponse response = plugin.handle(request);

        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.INTERNAL_ERROR));
    }

    @Test
    public void handleShouldReturnOriginalResponseInAbsenceOfExceptions() throws Exception {
        GoPluginApiResponse preparedResponse = DefaultGoPluginApiResponse.success("Body");
        GoPluginApiRequest request = new DefaultGoPluginApiRequest("task", "1.0", RequestHandlerFactory.CHECK_PACKAGE_CONNECTION);
        when(requestHandler.handle(any(GoPluginApiRequest.class))).thenReturn(preparedResponse);
        when(requestHandlerFactory.create(anyString())).thenReturn(requestHandler);

        GoPluginApiResponse returnedResponse = plugin.handle(request);

        assertThat(returnedResponse, equalTo(preparedResponse));
    }

    @Test
    public void pluginIdentifierShouldReturnCorrectPluginInfo() {
        B2MaterialPlugin plugin = new B2MaterialPlugin();
        GoPluginIdentifier identifier = plugin.pluginIdentifier();
        assertNotNull(identifier);
        assertEquals("invalid type of extension", "package-repository", identifier.getExtension());
    }
}