/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers;

import com.thoughtworks.go.plugin.api.request.DefaultGoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.github.kszatan.gocd.b2.utils.storage.FileName;
import io.github.kszatan.gocd.b2.utils.storage.ListFileNamesResponse;
import io.github.kszatan.gocd.b2.utils.storage.Storage;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class LatestRevisionSinceRequestHandlerTest {
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
            "  },\n" +
            "  \"package-configuration\": {\n" +
            "    \"pipelineName\": {\n" +
            "      \"value\": \"up42\"\n" +
            "    },\n" +
            "    \"stageName\": {\n" +
            "      \"value\": \"up42_stage\"\n" +
            "    },\n" +
            "    \"jobName\": {\n" +
            "      \"value\": \"up42_job\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"previous-revision\": {\n" +
            "      \"revision\": \"50.1\",\n" +
            "      \"timestamp\": \"2011-07-14T19:43:37.100Z\",\n" +
            "      \"data\": {}\n" +
            "  }\n" +
            "}";

    private LatestRevisionSinceRequestHandler handler;
    private DefaultGoPluginApiRequest request;
    private Storage storage;

    @Before
    public void setUp() throws Exception {
        storage = mock(Storage.class);
        handler = new LatestRevisionSinceRequestHandler(storage);
        request = new DefaultGoPluginApiRequest("package-repository", "1.0", "latest-revision-since");
    }

    @Test
    public void handleShouldReturnNonNullResponseForValidCheckPackageConfigurationRequest() {
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
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.INTERNAL_ERROR));
        assertThat(response.responseBody(), equalTo("Malformed JSON: Invalid JSON"));
    }

    @Test
    public void handleShouldReturnErrorResponseWithLastStorageErrorMessageWhenAuthorizeFails() throws Exception {
        request.setRequestBody(defaultRequestJson);
        doReturn(false).when(storage).authorize(any(), any());
        doReturn("Unauthorized").when(storage).getLastErrorMessage();
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.INTERNAL_ERROR));
        assertThat(response.responseBody(), equalTo("Unauthorized"));
    }

    @Test
    public void handleShouldReturnEmptySuccessResponseWhenNoPackageFound() throws Exception {
        request.setRequestBody(defaultRequestJson);
        doReturn(true).when(storage).authorize(any(), any());
        doReturn(Optional.of(new ListFileNamesResponse())).when(storage).listFiles(any(), any(), any());
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        assertThat(response.responseBody(), equalTo("{}"));
    }

    @Test
    public void handleShouldReturnSuccessResponseWithLatestRevisionIfAnyFound() throws Exception {
        List<String> fileNames = Arrays.asList("up42/up42_stage/up42_job/9.1/",
                "up42/up42_stage/up42_job/24.1/",
                "up42/up42_stage/up42_job/25.1/",
                "up42/up42_stage/up42_job/27.1/",
                "up42/up42_stage/up42_job/28.1/",
                "up42/up42_stage/up42_job/47.1/",
                "up42/up42_stage/up42_job/48.1/",
                "up42/up42_stage/up42_job/49.1/",
                "up42/up42_stage/up42_job/50.1/",
                "up42/up42_stage/up42_job/53.1/",
                "up42/up42_stage/up42_job/55.1/",
                "up42/up42_stage/up42_job/57.1/",
                "up42/up42_stage/up42_job/57.2/");
        List<Long> timesSinceEpoch = Arrays.asList(525510976000L,
                1525540976000L,
                2525590976000L);
        request.setRequestBody(defaultRequestJson);
        doReturn(true).when(storage).authorize(any(), any());
        ListFileNamesResponse firstListFileNamesResponse = new ListFileNamesResponse();
        firstListFileNamesResponse.fileNames = fileNames.stream().map(name -> {
            FileName fileName = new FileName();
            fileName.fileName = name;
            return fileName;
        }).collect(Collectors.toList());
        ListFileNamesResponse secondListFileNamesResponse = new ListFileNamesResponse();
        secondListFileNamesResponse.fileNames = timesSinceEpoch.stream().map(timestamp -> {
            FileName fileName = new FileName();
            fileName.fileName = "file";
            fileName.uploadTimestamp = timestamp;
            return fileName;
        }).collect(Collectors.toList());
        doReturn(Optional.of(firstListFileNamesResponse))
                .doReturn(Optional.of(secondListFileNamesResponse))
                .when(storage).listFiles(any(), any(), any());
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        assertThat(response.responseBody(), equalTo("{\"revision\":\"57.2\",\"timestamp\":\"2050-01-12T10:02:56.000Z\",\"data\":{\"pipelineName\":\"up42\",\"stageName\":\"up42_stage\",\"jobName\":\"up42_job\",\"label\":\"57.2\"}}"));
    }

    @Test
    public void handleShouldReturnSuccessResponseWithEmptyResponseIfNoNewerFound() throws Exception {
        List<String> fileNames = Arrays.asList("up42/up42_stage/up42_job/9.1/",
                "up42/up42_stage/up42_job/24.1/",
                "up42/up42_stage/up42_job/25.1/",
                "up42/up42_stage/up42_job/27.1/",
                "up42/up42_stage/up42_job/28.1/",
                "up42/up42_stage/up42_job/47.1/",
                "up42/up42_stage/up42_job/48.1/",
                "up42/up42_stage/up42_job/49.1/",
                "up42/up42_stage/up42_job/50.1/");
        request.setRequestBody(defaultRequestJson);
        doReturn(true).when(storage).authorize(any(), any());
        ListFileNamesResponse listFileNamesResponse = new ListFileNamesResponse();
        listFileNamesResponse.fileNames = fileNames.stream().map(name -> {
            FileName fileName = new FileName();
            fileName.fileName = name;
            return fileName;
        }).collect(Collectors.toList());
        doReturn(Optional.of(listFileNamesResponse)).when(storage).listFiles(any(), any(), any());
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        assertThat(response.responseBody(), equalTo("{}"));
    }
}