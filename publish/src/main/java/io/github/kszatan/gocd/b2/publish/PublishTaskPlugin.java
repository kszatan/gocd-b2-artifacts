/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish;

import com.thoughtworks.go.plugin.api.AbstractGoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.github.kszatan.gocd.b2.publish.handlers.DefaultRequestHandlerFactory;
import io.github.kszatan.gocd.b2.publish.handlers.RequestHandler;
import io.github.kszatan.gocd.b2.publish.handlers.RequestHandlerFactory;

import java.util.Collections;
import java.util.List;

@Extension
public class PublishTaskPlugin extends AbstractGoPlugin {
    private static final String EXTENSION_NAME = "task";
    private static final List<String> supportedExtensionVersions = Collections.singletonList("1.0");
    private final RequestHandlerFactory requestHandlerFactory;

    public PublishTaskPlugin() {
        requestHandlerFactory = new DefaultRequestHandlerFactory();
    }

    public PublishTaskPlugin(RequestHandlerFactory requestHandlerFactory) {
        this.requestHandlerFactory = requestHandlerFactory;
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) {
        GoPluginApiResponse response;
        try {
            RequestHandler requestHandler = requestHandlerFactory.create(request.requestName());
            response = requestHandler.handle(request);
        } catch (UnhandledRequestTypeException e) {
            response = DefaultGoPluginApiResponse.badRequest("Invalid request name: " + request.requestName());
        } catch (Exception e) {
            response = DefaultGoPluginApiResponse.error("Unknown error during request processing: " + e.getMessage());
        }
        return response;
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier(EXTENSION_NAME, supportedExtensionVersions);
    }
}
