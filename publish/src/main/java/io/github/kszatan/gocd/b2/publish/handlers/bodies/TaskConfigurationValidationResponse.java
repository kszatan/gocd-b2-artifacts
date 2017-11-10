/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * op-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers.bodies;

import io.github.kszatan.gocd.b2.publish.json.GsonService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TaskConfigurationValidationResponse {
    public Map<String, String> errors = new HashMap<>();

    public String toJson() {
        return GsonService.toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskConfigurationValidationResponse that = (TaskConfigurationValidationResponse) o;
        return Objects.equals(errors, that.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errors);
    }
}
