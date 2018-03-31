package io.github.kszatan.gocd.b2.material.handlers;

import io.github.kszatan.gocd.b2.material.handlers.bodies.ConfigurationValidationError;
import io.github.kszatan.gocd.b2.material.handlers.bodies.ConfigurationValidationResponse;
import io.github.kszatan.gocd.b2.material.handlers.bodies.PackageConfiguration;
import io.github.kszatan.gocd.b2.utils.json.GsonService;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class PackageConfigurationValidatorTest {
    private static final String correctRequestJson = "{\n" +
            "  \"pipelineName\": {\n" +
            "      \"value\": \"pajplajn\"\n" +
            "  },\n" +
            "  \"stageName\": {\n" +
            "      \"value\": \"stejdz\"\n" +
            "  },\n" +
            "  \"jobName\": {\n" +
            "      \"value\": \"dzob\"\n" +
            "  }\n" +
            "}";

    private static final String emptyPipelineRequestJson = "{\n" +
            "  \"pipelineName\": {\n" +
            "      \"value\": \"\"\n" +
            "  },\n" +
            "  \"stageName\": {\n" +
            "      \"value\": \"stejdz\"\n" +
            "  },\n" +
            "  \"jobName\": {\n" +
            "      \"value\": \"dzob\"\n" +
            "  }\n" +
            "}";

    private static final String emptyStageRequestJson = "{\n" +
            "  \"pipelineName\": {\n" +
            "      \"value\": \"pajplajn\"\n" +
            "  },\n" +
            "  \"stageName\": {\n" +
            "      \"value\": \"\"\n" +
            "  },\n" +
            "  \"jobName\": {\n" +
            "      \"value\": \"dzob\"\n" +
            "  }\n" +
            "}";

    private static final String emptyJobRequestJson = "{\n" +
            "  \"pipelineName\": {\n" +
            "      \"value\": \"pajplajn\"\n" +
            "  },\n" +
            "  \"stageName\": {\n" +
            "      \"value\": \"stejdz\"\n" +
            "  },\n" +
            "  \"jobName\": {\n" +
            "      \"value\": \"\"\n" +
            "  }\n" +
            "}";

    private static final String allEmptyFieldsJson = "{\n" +
            "  \"pipelineName\": {\n" +
            "      \"value\": \"\"\n" +
            "  },\n" +
            "  \"stageName\": {\n" +
            "      \"value\": \"\"\n" +
            "  },\n" +
            "  \"jobName\": {\n" +
            "      \"value\": \"\"\n" +
            "  }\n" +
            "}";

    private PackageConfigurationValidator validator = new PackageConfigurationValidator();

    @Test
    public void validatorShouldNotReportAnyErrorsForCorrectRequest() throws Exception {
        PackageConfiguration configuration = GsonService.fromJson(correctRequestJson, PackageConfiguration.class);
        ConfigurationValidationResponse response = validator.validate(configuration);
        assertThat(response.errors.size(), equalTo(0));
    }

    @Test
    public void validatorShouldReportErrorOnEmptyPipeline() throws Exception {
        PackageConfiguration configuration = GsonService.fromJson(emptyPipelineRequestJson, PackageConfiguration.class);
        ConfigurationValidationResponse response = validator.validate(configuration);
        assertThat(response.errors.size(), equalTo(1));
        ConfigurationValidationError error = response.errors.get(0);
        assertThat(error.key, equalTo("pipelineName"));
        assertThat(error.message, equalTo("Empty pipeline name"));
    }

    @Test
    public void validatorShouldReportErrorOnEmptyStage() throws Exception {
        PackageConfiguration configuration = GsonService.fromJson(emptyStageRequestJson, PackageConfiguration.class);
        ConfigurationValidationResponse response = validator.validate(configuration);
        assertThat(response.errors.size(), equalTo(1));
        ConfigurationValidationError error = response.errors.get(0);
        assertThat(error.key, equalTo("stageName"));
        assertThat(error.message, equalTo("Empty stage name"));
    }

    @Test
    public void validatorShouldReportErrorOnEmptyJob() throws Exception {
        PackageConfiguration configuration = GsonService.fromJson(emptyJobRequestJson, PackageConfiguration.class);
        ConfigurationValidationResponse response = validator.validate(configuration);
        assertThat(response.errors.size(), equalTo(1));
        ConfigurationValidationError error = response.errors.get(0);
        assertThat(error.key, equalTo("jobName"));
        assertThat(error.message, equalTo("Empty job name"));
    }

    @Test
    public void validatorShouldReportErrorsForAllEmptyFields() throws Exception {
        PackageConfiguration configuration = GsonService.fromJson(allEmptyFieldsJson, PackageConfiguration.class);
        ConfigurationValidationResponse response = validator.validate(configuration);
        assertThat(response.errors.size(), equalTo(3));
    }
}