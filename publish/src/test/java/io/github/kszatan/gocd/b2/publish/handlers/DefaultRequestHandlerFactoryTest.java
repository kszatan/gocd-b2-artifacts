/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class DefaultRequestHandlerFactoryTest {
    private RequestHandlerFactory requestHandlerFactory;
    @Before
    public void setUp() {
        requestHandlerFactory = new DefaultRequestHandlerFactory();
    }

    @Test
    public void shouldCreateRequestHandlerForConfigurationRequest() throws Exception {
        RequestHandler handler = requestHandlerFactory.create(RequestHandlerFactory.CONFIGURATION);
        assertThat(handler instanceof ConfigurationRequestHandler, is(true));
    }

    @Test
    public void shouldCreateRequestHandlerForViewRequest() throws Exception {
        RequestHandler handler = requestHandlerFactory.create(RequestHandlerFactory.VIEW);
        assertThat(handler instanceof ViewRequestHandler, is(true));
    }

    @Test
    public void shouldCreateRequestHandlerForValidateConfigurationRequest() throws Exception {
        RequestHandler handler = requestHandlerFactory.create(RequestHandlerFactory.VALIDATE);
        assertThat(handler instanceof ValidateRequestHandler, is(true));
    }

    @Test
    public void shouldCreateRequestHandlerForCheckConnectionRequest() throws Exception {
        RequestHandler handler = requestHandlerFactory.create(RequestHandlerFactory.EXECUTE);
        assertThat(handler instanceof ExecuteRequestHandler, is(true));
    }

    @Test
    public void shouldThrowExceptionForUnknownRequest() throws Exception {
        String request = "unknown-request";
        try {
            requestHandlerFactory.create(request);
            fail("should have failed");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("This is an invalid request type :" + request));
        }
    }
}