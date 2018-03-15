/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers;

import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;

import java.io.IOException;

public class DefaultRequestHandlerFactory implements  RequestHandlerFactory {
    public RequestHandler create(String requestType) throws UnhandledRequestTypeException, IOException {
        RequestHandler handler = null;
        switch (requestType) {
            case CHECK_PACKAGE_CONNECTION:
//                handler = new ConfigurationRequestHandler();
                break;
            case CHECK_REPOSITORY_CONNECTION:
                handler = new CheckRepositoryConnectionRequestHandler();
                break;
            case LATEST_REVISION:
//                handler = new ValidateRequestHandler();
                break;
            case LATEST_REVISION_SINCE:
//                handler = new ExecuteRequestHandler();
                break;
            case PACKAGE_CONFIGURATION:
                handler = new PackageConfigurationRequestHandler();
                break;
            case REPOSITORY_CONFIGURATION:
                handler = new RepositoryConfigurationRequestHandler();
                break;
            case VALIDATE_PACKAGE_CONFIGURATION:
                handler = new ValidatePackageConfigurationRequestHandler();
                break;
            case VALIDATE_REPOSITORY_CONFIGURATION:
                handler = new ValidateRepositoryConfigurationRequestHandler();
                break;
            default:
                throw new UnhandledRequestTypeException(requestType);
        }
        return handler;
    }
}
