package io.github.kszatan.gocd.b2.material.handlers;

import io.github.kszatan.gocd.b2.material.handlers.bodies.ConfigurationValidationError;
import io.github.kszatan.gocd.b2.material.handlers.bodies.ConfigurationValidationResponse;
import io.github.kszatan.gocd.b2.material.handlers.bodies.RepositoryConfiguration;
import io.github.kszatan.gocd.b2.utils.json.GsonService;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class RepositoryConfigurationValidatorTest {
    private static final String correctRequestJson = "{\n" +
            "  \"bucketName\": {\n" +
            "    \"value\": \"bukhet\"\n" +
            "  },\n" +
            "  \"accountId\": {\n" +
            "    \"value\": \"30f20426f0b1\"\n" +
            "  },\n" +
            "  \"applicationKey\": {\n" +
            "    \"value\": \"caca85ed4e7a3404db0b08bb8256d00d84e247e46\"\n" +
            "  }\n" +
            "}";

    private static final String emptyBucketNameJson = "{\n" +
            "  \"bucketName\": {\n" +
            "    \"value\": \"\"\n" +
            "  },\n" +
            "  \"accountId\": {\n" +
            "    \"value\": \"30f20426f0b1\"\n" +
            "  },\n" +
            "  \"applicationKey\": {\n" +
            "    \"value\": \"caca85ed4e7a3404db0b08bb8256d00d84e247e46\"\n" +
            "  }\n" +
            "}";

    private static final String emptyAccountIdJson = "{\n" +
            "  \"bucketName\": {\n" +
            "    \"value\": \"bukhet\"\n" +
            "  },\n" +
            "  \"accountId\": {\n" +
            "    \"value\": \"\"\n" +
            "  },\n" +
            "  \"applicationKey\": {\n" +
            "    \"value\": \"caca85ed4e7a3404db0b08bb8256d00d84e247e46\"\n" +
            "  }\n" +
            "}";

    private static final String emptyApplicationKeyJson = "{\n" +
            "  \"bucketName\": {\n" +
            "    \"value\": \"bukhet\"\n" +
            "  },\n" +
            "  \"accountId\": {\n" +
            "    \"value\": \"30f20426f0b1\"\n" +
            "  },\n" +
            "  \"applicationKey\": {\n" +
            "    \"value\": \"\"\n" +
            "  }\n" +
            "}";

    private static final String allEmptyFieldsJson = "{\n" +
            "  \"bucketName\": {\n" +
            "    \"value\": \"\"\n" +
            "  },\n" +
            "  \"accountId\": {\n" +
            "    \"value\": \"\"\n" +
            "  },\n" +
            "  \"applicationKey\": {\n" +
            "    \"value\": \"\"\n" +
            "  }\n" +
            "}";


    private RepositoryConfigurationValidator validator = new RepositoryConfigurationValidator();

    @Test
    public void validatorShouldNotReportAnyErrorsForCorrectRequest() throws Exception {
        RepositoryConfiguration configuration = GsonService.fromJson(correctRequestJson, RepositoryConfiguration.class);
        ConfigurationValidationResponse response = validator.validate(configuration);
        assertThat(response.errors.size(), equalTo(0));
    }

    @Test
    public void validatorShouldReportErrorOnEmptyBucketName() throws Exception {
        RepositoryConfiguration configuration = GsonService.fromJson(emptyBucketNameJson, RepositoryConfiguration.class);
        ConfigurationValidationResponse response = validator.validate(configuration);
        assertThat(response.errors.size(), equalTo(1));
        ConfigurationValidationError error = response.errors.get(0);
        assertThat(error.key, equalTo("bucketName"));
        assertThat(error.message, equalTo("Empty bucket name"));
    }

    @Test
    public void validatorShouldReportErrorOnEmptyAccountId() throws Exception {
        RepositoryConfiguration configuration = GsonService.fromJson(emptyAccountIdJson, RepositoryConfiguration.class);
        ConfigurationValidationResponse response = validator.validate(configuration);
        assertThat(response.errors.size(), equalTo(1));
        ConfigurationValidationError error = response.errors.get(0);
        assertThat(error.key, equalTo("accountId"));
        assertThat(error.message, equalTo("Empty account ID"));
    }

    @Test
    public void validatorShouldReportErrorOnEmptyApplicationKey() throws Exception {
        RepositoryConfiguration configuration = GsonService.fromJson(emptyApplicationKeyJson, RepositoryConfiguration.class);
        ConfigurationValidationResponse response = validator.validate(configuration);
        assertThat(response.errors.size(), equalTo(1));
        ConfigurationValidationError error = response.errors.get(0);
        assertThat(error.key, equalTo("applicationKey"));
        assertThat(error.message, equalTo("Empty application key"));
    }

    @Test
    public void validatorShouldReportErrorsForAllEmptyFields() throws Exception {
        RepositoryConfiguration configuration = GsonService.fromJson(allEmptyFieldsJson, RepositoryConfiguration.class);
        ConfigurationValidationResponse response = validator.validate(configuration);
        assertThat(response.errors.size(), equalTo(3));
    }
}