/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers.bodies;

import io.github.kszatan.gocd.b2.publish.json.GsonService;

import java.util.Objects;

public class ExecuteResponse {
    public Boolean success;
    public String message;

    public static ExecuteResponse success(String message) {
        return create(true, message);
    }

    public static ExecuteResponse failure(String message) {
        return create(false, message);
    }

    private static ExecuteResponse create(Boolean success, String message) {
        ExecuteResponse result = new ExecuteResponse();
        result.success = success;
        result.message = message;
        return result;
    }

    public String toJson() {
        return GsonService.toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecuteResponse that = (ExecuteResponse) o;
        return Objects.equals(success, that.success) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, message);
    }    
}
