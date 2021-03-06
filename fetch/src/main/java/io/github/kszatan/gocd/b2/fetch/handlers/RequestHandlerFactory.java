/*
 * Copyright (c) 2018. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.fetch.handlers;

import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;

public interface RequestHandlerFactory {
    String CONFIGURATION = "configuration";
    String VIEW = "view";
    String VALIDATE = "validate";
    String EXECUTE = "execute";

    RequestHandler create(String requestType) throws UnhandledRequestTypeException;
}
