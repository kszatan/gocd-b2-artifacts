/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers.bodies;

import java.util.HashMap;
import java.util.Map;

public class TaskContext {
    public Map<String, String> environmentVariables;
    public String workingDirectory;

    public TaskContext() {
        environmentVariables = new HashMap<>();
        workingDirectory = new String();
    }
}
