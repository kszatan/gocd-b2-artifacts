/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers;

import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import io.github.kszatan.gocd.b2.utils.storage.Storage;

import java.io.IOException;

public class DefaultRequestHandlerFactory implements RequestHandlerFactory {
    private Storage storage;
    public DefaultRequestHandlerFactory(Storage storage) {
        this.storage = storage;
    }

    public RequestHandler create(String requestType) throws UnhandledRequestTypeException {
        RequestHandler handler;
        switch (requestType) {
            case CHECK_PACKAGE_CONNECTION:
                handler = new CheckPackageConnectionRequestHandler(storage);
                break;
            case CHECK_REPOSITORY_CONNECTION:
                handler = new CheckRepositoryConnectionRequestHandler(storage);
                break;
            case LATEST_REVISION:
                handler = new LatestRevisionRequestHandler(storage);
                break;
            case LATEST_REVISION_SINCE:
                handler = new LatestRevisionSinceRequestHandler(storage);
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
