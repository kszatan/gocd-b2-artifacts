/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.fetch.handlers.bodies;

import io.github.kszatan.gocd.b2.utils.json.GsonService;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

public class ExecuteResponseTest {
    @Test
    public void successShouldReturnExecuteResponseInstance() throws Exception {
        assertThat(ExecuteResponse.success(""),
                instanceOf(ExecuteResponse.class));
    }

    @Test
    public void successShouldSetSuccessFieldToTrue() throws Exception {
        ExecuteResponse result = ExecuteResponse.success("");
        assertThat(result.success, equalTo(true));
    }

    @Test
    public void successShouldSetPassedMessage() throws Exception {
        String message = "message";
        ExecuteResponse result = ExecuteResponse.success(message);
        assertThat(result.message, equalTo(message));
    }

    @Test
    public void failureShouldReturnExecuteResponseInstance() throws Exception {
        assertThat(ExecuteResponse.failure(""),
                instanceOf(ExecuteResponse.class));
    }

    @Test
    public void failureShouldSetSuccessFieldToFalse() throws Exception {
        ExecuteResponse result = ExecuteResponse.failure("");
        assertThat(result.success, equalTo(false));
    }


    @Test
    public void failureShouldSetPassedMessage() throws Exception {
        String message = "message";
        ExecuteResponse result = ExecuteResponse.failure(message);
        assertThat(result.message, equalTo(message));
    }

    @Test
    public void toJsonShouldIncludeSuccessAndMessage() throws Exception {
        String message = "message";
        ExecuteResponse result = ExecuteResponse.failure(message);
        String json = result.toJson();
        ExecuteResponse resultFromJson = GsonService.fromJson(json, ExecuteResponse.class);
        assertThat(resultFromJson, equalTo(result));
    }
}