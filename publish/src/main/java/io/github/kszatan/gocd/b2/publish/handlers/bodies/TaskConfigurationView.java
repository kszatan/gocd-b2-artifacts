/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * op-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers.bodies;

public class TaskConfigurationView {
    public String displayValue;
    public String template;

    public TaskConfigurationView(String displayValue, String template) {
        this.displayValue = displayValue;
        this.template = template;
    }
}
