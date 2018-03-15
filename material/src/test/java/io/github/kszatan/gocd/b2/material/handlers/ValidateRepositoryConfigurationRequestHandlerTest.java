package io.github.kszatan.gocd.b2.material.handlers;

import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.request.DefaultGoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.github.kszatan.gocd.b2.material.handlers.bodies.ConfigurationValidationResponse;
import io.github.kszatan.gocd.b2.utils.json.GsonService;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class ValidateRepositoryConfigurationRequestHandlerTest {
    static private final String correctRequestJson = "{\n" +
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

    static private final String emptyBucketNameRequestJson = "{\n" +
            "  \"repository-configuration\": {\n" +
            "    \"bucketName\": {\n" +
            "      \"value\": \"\"\n" +
            "    },\n" +
            "    \"accountId\": {\n" +
            "      \"value\": \"30f20426f0b1\"\n" +
            "    },\n" +
            "    \"applicationKey\": {\n" +
            "      \"value\": \"caca85ed4e7a3404db0b08bb8256d00d84e247e46\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

    static private final String emptyAccountIdRequestJson = "{\n" +
            "  \"repository-configuration\": {\n" +
            "    \"bucketName\": {\n" +
            "      \"value\": \"bukhet\"\n" +
            "    },\n" +
            "    \"accountId\": {\n" +
            "      \"value\": \"\"\n" +
            "    },\n" +
            "    \"applicationKey\": {\n" +
            "      \"value\": \"caca85ed4e7a3404db0b08bb8256d00d84e247e46\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

    static private final String emptyApplicationKeyRequestJson = "{\n" +
            "  \"repository-configuration\": {\n" +
            "    \"bucketName\": {\n" +
            "      \"value\": \"bukhet\"\n" +
            "    },\n" +
            "    \"accountId\": {\n" +
            "      \"value\": \"30f20426f0b1\"\n" +
            "    },\n" +
            "    \"applicationKey\": {\n" +
            "      \"value\": \"\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private ValidateRepositoryConfigurationRequestHandler handler;
    private DefaultGoPluginApiRequest request;

    @Before
    public void setUp() throws Exception {
        handler = new ValidateRepositoryConfigurationRequestHandler();
        request = new DefaultGoPluginApiRequest("package-repository", "1.0", "validate-repository-configuration");
    }

    @Test
    public void handleShouldReturnNonNullResponseForValidateConfigurationRequest() throws UnhandledRequestTypeException {
        request.setRequestBody(correctRequestJson);
        assertNotNull(handler.handle(request));
    }

    @Test
    public void handleShouldReturnErrorResponseForEmptyBucketName() throws UnhandledRequestTypeException {
        request.setRequestBody(emptyBucketNameRequestJson);
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        ConfigurationValidationResponse validation =
                GsonService.fromJson(response.responseBody(), ConfigurationValidationResponse.class);
        assertThat(validation.errors.size(), equalTo(1));
    }

    @Test
    public void handleShouldReturnErrorResponseForEmptyAccountId() throws UnhandledRequestTypeException {
        request.setRequestBody(emptyAccountIdRequestJson);
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        ConfigurationValidationResponse validation =
                GsonService.fromJson(response.responseBody(), ConfigurationValidationResponse.class);
        assertThat(validation.errors.size(), equalTo(1));
    }

    @Test
    public void handleShouldReturnErrorResponseForEmptyApplicationKey() throws UnhandledRequestTypeException {
        request.setRequestBody(emptyApplicationKeyRequestJson);
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        ConfigurationValidationResponse validation =
                GsonService.fromJson(response.responseBody(), ConfigurationValidationResponse.class);
        assertThat(validation.errors.size(), equalTo(1));
    }

    @Test
    public void handleShouldReturnErrorResponseWhenGivenInvalidJson() {
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("task", "1.0", "validate");
        request.setRequestBody("Invalid JSON");
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.INTERNAL_ERROR));
    }
}