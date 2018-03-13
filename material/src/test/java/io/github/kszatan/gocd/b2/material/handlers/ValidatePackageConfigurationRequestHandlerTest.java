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

public class ValidatePackageConfigurationRequestHandlerTest {
    static private final String correctRequestJson = "{\n" +
            "  \"repository-configuration\": {\n" +
            "    \"url\": {\n" +
            "      \"value\": \"https://repository/url\"\n" +
            "    },\n" +
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
            "      \"value\": \"pajplajn\"\n" +
            "    },\n" +
            "    \"stageName\": {\n" +
            "      \"value\": \"stejdz\"\n" +
            "    },\n" +
            "    \"jobName\": {\n" +
            "      \"value\": \"dzob\"\n" +
            "    }\n" +
            "  }" +
            "}";

    static private final String emptyUrlRequestJson = "{\n" +
            "  \"repository-configuration\": {\n" +
            "    \"url\": {\n" +
            "      \"value\": \"\"\n" +
            "    },\n" +
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
            "      \"value\": \"pajplajn\"\n" +
            "    },\n" +
            "    \"stageName\": {\n" +
            "      \"value\": \"stejdz\"\n" +
            "    },\n" +
            "    \"jobName\": {\n" +
            "      \"value\": \"dzob\"\n" +
            "    }\n" +
            "  }" +
            "}";

    static private final String emptyPipelineNameRequestJson = "{\n" +
            "  \"repository-configuration\": {\n" +
            "    \"url\": {\n" +
            "      \"value\": \"https://repository/url\"\n" +
            "    },\n" +
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
            "      \"value\": \"\"\n" +
            "    },\n" +
            "    \"stageName\": {\n" +
            "      \"value\": \"stejdz\"\n" +
            "    },\n" +
            "    \"jobName\": {\n" +
            "      \"value\": \"dzob\"\n" +
            "    }\n" +
            "  }" +
            "}";

    static private final String emptyStageNameRequestJson = "{\n" +
            "  \"repository-configuration\": {\n" +
            "    \"url\": {\n" +
            "      \"value\": \"https://repository/url\"\n" +
            "    },\n" +
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
            "      \"value\": \"pajplajn\"\n" +
            "    },\n" +
            "    \"stageName\": {\n" +
            "      \"value\": \"\"\n" +
            "    },\n" +
            "    \"jobName\": {\n" +
            "      \"value\": \"dzob\"\n" +
            "    }\n" +
            "  }" +
            "}";

    static private final String emptyJobNameRequestJson = "{\n" +
            "  \"repository-configuration\": {\n" +
            "    \"url\": {\n" +
            "      \"value\": \"https://repository/url\"\n" +
            "    },\n" +
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
            "      \"value\": \"pajplajn\"\n" +
            "    },\n" +
            "    \"stageName\": {\n" +
            "      \"value\": \"stejdz\"\n" +
            "    },\n" +
            "    \"jobName\": {\n" +
            "      \"value\": \"\"\n" +
            "    }\n" +
            "  }" +
            "}";

    private ValidatePackageConfigurationRequestHandler handler;
    private DefaultGoPluginApiRequest request;

    @Before
    public void setUp() throws Exception {
        handler = new ValidatePackageConfigurationRequestHandler();
        request = new DefaultGoPluginApiRequest("package-repository", "1.0", "validate-repository-configuration");
    }

    @Test
    public void handleShouldReturnNonNullResponseForValidateConfigurationRequest() throws UnhandledRequestTypeException {
        request.setRequestBody(correctRequestJson);
        assertNotNull(handler.handle(request));
    }

    @Test
    public void handleShouldReturnErrorResponseForRepositoryEmptyUrl() throws UnhandledRequestTypeException {
        request.setRequestBody(emptyUrlRequestJson);
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        ConfigurationValidationResponse validation =
                GsonService.fromJson(response.responseBody(), ConfigurationValidationResponse.class);
        assertThat(validation.errors.size(), equalTo(1));
    }

    @Test
    public void handleShouldReturnErrorResponseForEmptyPipelineName() throws UnhandledRequestTypeException {
        request.setRequestBody(emptyPipelineNameRequestJson);
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        ConfigurationValidationResponse validation =
                GsonService.fromJson(response.responseBody(), ConfigurationValidationResponse.class);
        assertThat(validation.errors.size(), equalTo(1));
    }

    @Test
    public void handleShouldReturnErrorResponseForEmptyStageName() throws UnhandledRequestTypeException {
        request.setRequestBody(emptyStageNameRequestJson);
        GoPluginApiResponse response = handler.handle(request);
        assertThat(response.responseCode(), equalTo(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        ConfigurationValidationResponse validation =
                GsonService.fromJson(response.responseBody(), ConfigurationValidationResponse.class);
        assertThat(validation.errors.size(), equalTo(1));
    }

    @Test
    public void handleShouldReturnErrorResponseForEmptyJobName() throws UnhandledRequestTypeException {
        request.setRequestBody(emptyJobNameRequestJson);
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