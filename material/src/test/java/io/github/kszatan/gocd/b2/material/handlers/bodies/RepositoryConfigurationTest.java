package io.github.kszatan.gocd.b2.material.handlers.bodies;

import io.github.kszatan.gocd.b2.utils.json.GsonService;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class RepositoryConfigurationTest {
    private static final String defaultRequestJson = "{\n" +
            "  \"url\": {\n" +
            "    \"value\": \"https://repository/url\"\n" +
            "  },\n" +
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

    @Test
    public void gettersShouldReturnCorrectValuesFromJsonRequest() throws Exception {
        RepositoryConfiguration configuration = GsonService.fromJson(defaultRequestJson, RepositoryConfiguration.class);
        assertThat(configuration.getUrl(), equalTo("https://repository/url"));
        assertThat(configuration.getBucketName(), equalTo("bukhet"));
        assertThat(configuration.getAccountId(), equalTo("30f20426f0b1"));
        assertThat(configuration.getApplicationKey(), equalTo("caca85ed4e7a3404db0b08bb8256d00d84e247e46"));
    }
}