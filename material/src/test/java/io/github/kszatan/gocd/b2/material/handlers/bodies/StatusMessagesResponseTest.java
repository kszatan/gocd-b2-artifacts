package io.github.kszatan.gocd.b2.material.handlers.bodies;

import io.github.kszatan.gocd.b2.utils.json.GsonService;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class StatusMessagesResponseTest {
    @Test
    public void successShouldReturnScmConnectionResultInstance() throws Exception {
        assertThat(StatusMessagesResponse.success(Collections.singletonList("")),
                instanceOf(StatusMessagesResponse.class));
    }

    @Test
    public void successShouldSetCorrectStatus() throws Exception {
        StatusMessagesResponse result = StatusMessagesResponse.success(Collections.singletonList(""));
        assertThat(result.status, equalTo("success"));
    }

    @Test
    public void successShouldSetCorrectMessagesList() throws Exception {
        List<String> messages = Arrays.asList("first", "second");
        StatusMessagesResponse result = StatusMessagesResponse.success(messages);
        assertThat(result.messages, equalTo(messages));
    }

    @Test
    public void failureShouldReturnScmConnectionResultInstance() throws Exception {
        assertThat(StatusMessagesResponse.failure(Collections.singletonList("")),
                instanceOf(StatusMessagesResponse.class));
    }

    @Test
    public void failureShouldSetCorrectStatus() throws Exception {
        StatusMessagesResponse result = StatusMessagesResponse.failure(Collections.singletonList(""));
        assertThat(result.status, equalTo("failure"));
    }


    @Test
    public void failureShouldSetCorrectMessagesList() throws Exception {
        List<String> messages = Arrays.asList("first", "second");
        StatusMessagesResponse result = StatusMessagesResponse.failure(messages);
        assertThat(result.messages, equalTo(messages));
    }

    @Test
    public void toJsonShouldIncludeStatusAndMessages() throws Exception {
        List<String> messages = Arrays.asList("first", "second");
        StatusMessagesResponse result = StatusMessagesResponse.failure(messages);
        String json = result.toJson();
        StatusMessagesResponse resultFromJson = GsonService.fromJson(json, StatusMessagesResponse.class);
        assertThat(resultFromJson, equalTo(result));
    }

}