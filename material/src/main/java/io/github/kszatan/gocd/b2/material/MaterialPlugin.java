/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material;

import com.thoughtworks.go.plugin.api.AbstractGoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.github.kszatan.gocd.b2.material.handlers.DefaultRequestHandlerFactory;
import io.github.kszatan.gocd.b2.material.handlers.RequestHandler;
import io.github.kszatan.gocd.b2.material.handlers.RequestHandlerFactory;
import io.github.kszatan.gocd.b2.utils.storage.BackblazeStorage;
import io.github.kszatan.gocd.b2.utils.storage.CachingCredentialsManager;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Extension
public class MaterialPlugin extends AbstractGoPlugin {
    private static final String EXTENSION_NAME = "package-repository";
    private static final List<String> supportedExtensionVersions = Collections.singletonList("1.0");
    private RequestHandlerFactory requestHandlerFactory;

    private Logger logger = Logger.getLoggerFor(MaterialPlugin.class);

    public MaterialPlugin() {
        try {
            requestHandlerFactory = new DefaultRequestHandlerFactory(
                    new BackblazeStorage(new CachingCredentialsManager()));
        } catch (IOException e) {
            logger.error("Unable to create request handler: " + e.getMessage());
        }
    }

    public MaterialPlugin(RequestHandlerFactory requestHandlerFactory) {
        this.requestHandlerFactory = requestHandlerFactory;
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) {
        GoPluginApiResponse response;
        try {
            logger.debug(request.requestName() + ": " + request.requestBody());
            RequestHandler requestHandler = requestHandlerFactory.create(request.requestName());
            response = requestHandler.handle(request);
        } catch (UnhandledRequestTypeException e) {
            response = DefaultGoPluginApiResponse.badRequest("Invalid request name: " + request.requestName());
        } catch (Exception e) {
            response = DefaultGoPluginApiResponse.error("Unknown error during request processing: " + e.getMessage());
        }
        logger.debug(response.responseCode() + ": " + response.responseBody());
        return response;
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier(EXTENSION_NAME, supportedExtensionVersions);
    }
}
