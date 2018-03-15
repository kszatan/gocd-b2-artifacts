package io.github.kszatan.gocd.b2.material.handlers.bodies;

import io.github.kszatan.gocd.b2.utils.json.GsonService;

import java.util.Collection;
import java.util.Objects;

public class CheckConnectionResponse {
    public String status;
    public Collection<String> messages;

    public static CheckConnectionResponse success(Collection<String> messages) {
        return create("success", messages);
    }

    public static CheckConnectionResponse failure(Collection<String> messages) {
        return create("failure", messages);
    }

    private static CheckConnectionResponse create(String status, Collection<String> messages) {
        CheckConnectionResponse result = new CheckConnectionResponse();
        result.status = status;
        result.messages = messages;
        return result;
    }

    public String toJson() {
        return GsonService.toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckConnectionResponse that = (CheckConnectionResponse) o;
        return Objects.equals(status, that.status) &&
                Objects.equals(messages, that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, messages);
    }
}
