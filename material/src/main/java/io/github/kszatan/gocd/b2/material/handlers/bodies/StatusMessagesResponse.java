package io.github.kszatan.gocd.b2.material.handlers.bodies;

import io.github.kszatan.gocd.b2.utils.json.GsonService;

import java.util.Collection;
import java.util.Objects;

public class StatusMessagesResponse {
    public String status;
    public Collection<String> messages;

    public static StatusMessagesResponse success(Collection<String> messages) {
        return create("success", messages);
    }

    public static StatusMessagesResponse failure(Collection<String> messages) {
        return create("failure", messages);
    }

    private static StatusMessagesResponse create(String status, Collection<String> messages) {
        StatusMessagesResponse result = new StatusMessagesResponse();
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
        StatusMessagesResponse that = (StatusMessagesResponse) o;
        return Objects.equals(status, that.status) &&
                Objects.equals(messages, that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, messages);
    }
}
