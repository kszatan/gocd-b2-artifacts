/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers.bodies;

import java.util.Objects;

public class ConfigurationValidationError {
    public String key;
    public String message;

    public ConfigurationValidationError() {
    }

    public ConfigurationValidationError(String key, String message) {
        this.key = key;
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurationValidationError that = (ConfigurationValidationError) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, message);
    }

}
