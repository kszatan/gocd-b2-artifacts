/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers.bodies;

public class SourceDestination {
    public String source = "";
    public String destination = "";

    public SourceDestination() { }

    public SourceDestination(String source, String destination) {
        this.source = source;
        this.destination = destination;
    }
}