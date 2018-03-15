package io.github.kszatan.gocd.b2.material.handlers.bodies;

import io.github.kszatan.gocd.b2.utils.json.GsonService;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class CheckConnectionResponseTest {
    @Test
    public void successShouldReturnScmConnectionResultInstance() throws Exception {
        assertThat(CheckConnectionResponse.success(Collections.singletonList("")),
                instanceOf(CheckConnectionResponse.class));
    }

    @Test
    public void successShouldSetCorrectStatus() throws Exception {
        CheckConnectionResponse result = CheckConnectionResponse.success(Collections.singletonList(""));
        assertThat(result.status, equalTo("success"));
    }

    @Test
    public void successShouldSetCorrectMessagesList() throws Exception {
        List<String> messages = Arrays.asList("first", "second");
        CheckConnectionResponse result = CheckConnectionResponse.success(messages);
        assertThat(result.messages, equalTo(messages));
    }

    @Test
    public void failureShouldReturnScmConnectionResultInstance() throws Exception {
        assertThat(CheckConnectionResponse.failure(Collections.singletonList("")),
                instanceOf(CheckConnectionResponse.class));
    }

    @Test
    public void failureShouldSetCorrectStatus() throws Exception {
        CheckConnectionResponse result = CheckConnectionResponse.failure(Collections.singletonList(""));
        assertThat(result.status, equalTo("failure"));
    }


    @Test
    public void failureShouldSetCorrectMessagesList() throws Exception {
        List<String> messages = Arrays.asList("first", "second");
        CheckConnectionResponse result = CheckConnectionResponse.failure(messages);
        assertThat(result.messages, equalTo(messages));
    }

    @Test
    public void toJsonShouldIncludeStatusAndMessages() throws Exception {
        List<String> messages = Arrays.asList("first", "second");
        CheckConnectionResponse result = CheckConnectionResponse.failure(messages);
        String json = result.toJson();
        CheckConnectionResponse resultFromJson = GsonService.fromJson(json, CheckConnectionResponse.class);
        assertThat(resultFromJson, equalTo(result));
    }

}