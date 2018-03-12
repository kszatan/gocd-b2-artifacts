/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * op-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers;

import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;

public interface RequestHandlerFactory {
    String REPOSITORY_CONFIGURATION = "repository-configuration";
    String PACKAGE_CONFIGURATION = "package-configuration";
    String VALIDATE_REPOSITORY_CONFIGURATION = "validate-repository-configuration";
    String VALIDATE_PACKAGE_CONFIGURATION = "validate-package-configuration";
    String CHECK_REPOSITORY_CONNECTION = "check-repository-connection";
    String CHECK_PACKAGE_CONNECTION = "check-package-connection";
    String LATEST_REVISION = "latest-revision";
    String LATEST_REVISION_SINCE = "latest-revision-since";

    RequestHandler create(String requestType) throws UnhandledRequestTypeException;
}
