package io.github.kszatan.gocd.b2.material.handlers.bodies;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class CheckRepositoryConnectionRequestTest {
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

    @Test
    public void requestObjectShouldReturnCorrectConfigurationAfterConstruction() throws Exception {
        CheckRepositoryConnectionRequest request = new CheckRepositoryConnectionRequest(defaultRequestJson);
        RepositoryConfiguration configuration = request.getConfiguration();
        assertThat(configuration.getBucketName(), equalTo("bukhet"));
        assertThat(configuration.getAccountId(), equalTo("30f20426f0b1"));
        assertThat(configuration.getApplicationKey(), equalTo("caca85ed4e7a3404db0b08bb8256d00d84e247e46"));
    }
}