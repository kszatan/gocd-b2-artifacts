package io.github.kszatan.gocd.b2.material.handlers.bodies;

import io.github.kszatan.gocd.b2.utils.json.GsonService;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class PackageConfigurationTest {
    private static final String defaultRequestJson = "{\n" +
            "  \"pipelineName\": {\n" +
            "    \"value\": \"pajplajn\"\n" +
            "  },\n" +
            "  \"stageName\": {\n" +
            "    \"value\": \"stejdz\"\n" +
            "  },\n" +
            "  \"jobName\": {\n" +
            "    \"value\": \"dzob\"\n" +
            "  }\n" +
            "}";

    @Test
    public void gettersShouldReturnCorrectValuesFromJsonRequest() throws Exception {
        PackageConfiguration configuration = GsonService.fromJson(defaultRequestJson, PackageConfiguration.class);
        assertThat(configuration.getPipelineName(), equalTo("pajplajn"));
        assertThat(configuration.getStageName(), equalTo("stejdz"));
        assertThat(configuration.getJobName(), equalTo("dzob"));
    }
}