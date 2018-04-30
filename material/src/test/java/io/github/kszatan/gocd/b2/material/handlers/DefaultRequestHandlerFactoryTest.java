/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.InstanceOf;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class DefaultRequestHandlerFactoryTest {
    private RequestHandlerFactory requestHandlerFactory;

    @Before
    public void setUp() {
        requestHandlerFactory = new DefaultRequestHandlerFactory();
    }

    @Test
    public void shouldCreateRequestHandlerForCheckPackageConnectionRequest() throws Exception {
        RequestHandler handler = requestHandlerFactory.create(RequestHandlerFactory.CHECK_PACKAGE_CONNECTION);
        assertThat(handler, new InstanceOf(CheckPackageConnectionRequestHandler.class));
    }

    @Test
    public void shouldCreateRequestHandlerForCheckRepositoryConnectionRequest() throws Exception {
        RequestHandler handler = requestHandlerFactory.create(RequestHandlerFactory.CHECK_REPOSITORY_CONNECTION);
        assertThat(handler, new InstanceOf(CheckRepositoryConnectionRequestHandler.class));
    }
    @Test
    public void shouldCreateRequestHandlerForLatestRevisionRequest() throws Exception {
        RequestHandler handler = requestHandlerFactory.create(RequestHandlerFactory.LATEST_REVISION);
        assertThat(handler, new InstanceOf(LatestRevisionRequestHandler.class));
    }

    @Test
    public void shouldCreateRequestHandlerForLatestRevisionSinceRequest() throws Exception {
        RequestHandler handler = requestHandlerFactory.create(RequestHandlerFactory.LATEST_REVISION_SINCE);
        assertThat(handler, new InstanceOf(LatestRevisionSinceRequestHandler.class));
    }

    @Test
    public void shouldCreateRequestHandlerForPackageConfigurationRequest() throws Exception {
        RequestHandler handler = requestHandlerFactory.create(RequestHandlerFactory.PACKAGE_CONFIGURATION);
        assertThat(handler, new InstanceOf(PackageConfigurationRequestHandler.class));
    }

    @Test
    public void shouldCreateRequestHandlerForRepositoryConfigurationRequest() throws Exception {
        RequestHandler handler = requestHandlerFactory.create(RequestHandlerFactory.REPOSITORY_CONFIGURATION);
        assertThat(handler, new InstanceOf(RepositoryConfigurationRequestHandler.class));
    }

    @Test
    public void shouldCreateRequestHandlerForValidatePackageConfigurationRequest() throws Exception {
        RequestHandler handler = requestHandlerFactory.create(RequestHandlerFactory.VALIDATE_PACKAGE_CONFIGURATION);
        assertNotNull(handler);
    }

    @Test
    public void shouldCreateRequestHandlerForValidateRepositoryConfigurationRequest() throws Exception {
        RequestHandler handler = requestHandlerFactory.create(RequestHandlerFactory.VALIDATE_REPOSITORY_CONFIGURATION);
        assertNotNull(handler);
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