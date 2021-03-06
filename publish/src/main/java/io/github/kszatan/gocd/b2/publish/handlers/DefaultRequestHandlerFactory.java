/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers;

import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;

public class DefaultRequestHandlerFactory implements  RequestHandlerFactory {
    public RequestHandler create(String requestType) throws UnhandledRequestTypeException {
        RequestHandler handler;
        switch (requestType) {
            case CONFIGURATION:
                handler = new ConfigurationRequestHandler();
                break;
            case VIEW:
                handler = new ViewRequestHandler();
                break;
            case VALIDATE:
                handler = new ValidateRequestHandler();
                break;
            case EXECUTE:
                handler = new ExecuteRequestHandler();
                break;
            default:
                throw new UnhandledRequestTypeException(requestType);
        }
        return handler;
    }
}
