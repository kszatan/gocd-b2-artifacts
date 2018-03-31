/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers.bodies;

import com.google.gson.reflect.TypeToken;
import io.github.kszatan.gocd.b2.utils.json.GsonService;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConfigurationValidationResponse {
    public List<ConfigurationValidationError> errors = new ArrayList<>();

    static public ConfigurationValidationResponse fromJson(String json) {
        ConfigurationValidationResponse response = new ConfigurationValidationResponse();
        Type type = new TypeToken<List<ConfigurationValidationError>>() {}.getType();
        response.errors = GsonService.fromJson(json, type);
        return response;
    }

    public String toJson() {
        return GsonService.toJson(errors);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurationValidationResponse that = (ConfigurationValidationResponse) o;
        return Objects.equals(errors, that.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errors);
    }
}
