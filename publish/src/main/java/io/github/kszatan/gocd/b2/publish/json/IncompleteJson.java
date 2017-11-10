/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * op-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.json;

public class IncompleteJson extends Exception {
    public IncompleteJson(String s) {
        super(s);
    }
}