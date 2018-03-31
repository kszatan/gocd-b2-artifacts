/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers;

import com.thoughtworks.go.plugin.api.request.DefaultGoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.github.kszatan.gocd.b2.material.handlers.bodies.CheckConnectionResponse;
import io.github.kszatan.gocd.b2.utils.json.GsonService;
import io.github.kszatan.gocd.b2.utils.storage.BackblazeStorage;
import io.github.kszatan.gocd.b2.utils.storage.Storage;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class CheckRepositoryConnectionRequestHandlerTest {
    static private final String defaultRequestJson = "{\n" +
            "  \"repository-configuration\": {\n" +
            "    \"bucketName\": {\n" +
            "      \"value\": \"bukhet\"\n" +
            "    },\n" +
            "    \"accountId\": {\n" +
            "      \"value\": \"30f20426f0b1\"\n" +
            "    },\n" +
            "    \"applicationKey\": {\n" +
            "      \"value\": \"caca85ed4e7a3404db0b08bb8256d00d84e247e46\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private CheckRepositoryConnectionRequestHandler handler;
    private DefaultGoPluginApiRequest request;
    private Storage storage;

    @Before
    public void setUp() throws Exception {
        storage = mock(Storage.class);
        handler = new CheckRepositoryConnectionRequestHandler(storage);
        request = new DefaultGoPluginApiRequest("package-repository", "1.0", "check-repository-connection");
    }

    @Test
    public void handleShouldReturnNonNullResponseForValidCheckRepositoryConfigurationRequest() {
        request.setRequestBody(defaultRequestJson);
        assertNotNull(handler.handle(request));
    }

    @Test
    public void handleShouldReturnValidationFailedResponseWhenGivenIncompleteJson() {
        request.setRequestBody("{}");
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.VALIDATION_FAILED));
        assertThat(response.responseBody(), equalTo("Missing fields: [repository-configuration]"));
    }

    @Test
    public void handleShouldReturnInfoAboutMalformedJsonResponseWhenGivenInvalidJson() {
        request.setRequestBody("Invalid JSON");
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        CheckConnectionResponse CheckConnectionResponse = GsonService.fromJson(response.responseBody(), CheckConnectionResponse.class);
        assertThat(CheckConnectionResponse.status, equalTo("failure"));
        assertThat(CheckConnectionResponse.messages.size(), equalTo(1));
        assertThat(CheckConnectionResponse.messages.iterator().next(), equalTo("Malformed JSON: Invalid JSON"));
    }
}