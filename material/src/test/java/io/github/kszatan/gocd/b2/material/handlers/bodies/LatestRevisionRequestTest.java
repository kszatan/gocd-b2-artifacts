/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers.bodies;

import io.github.kszatan.gocd.b2.utils.json.IncompleteJson;
import io.github.kszatan.gocd.b2.utils.json.InvalidJson;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class LatestRevisionRequestTest {
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

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void constructorShouldParseCorrectJsonString() throws Exception {
        LatestRevisionRequest request = new LatestRevisionRequest(defaultRequestJson);
        RepositoryConfiguration repositoryConf = request.getRepositoryConfiguration();
        assertThat(repositoryConf.getBucketName(), equalTo("bukhet"));
        assertThat(repositoryConf.getAccountId(), equalTo("30f20426f0b1"));
        assertThat(repositoryConf.getApplicationKey(), equalTo("caca85ed4e7a3404db0b08bb8256d00d84e247e46"));
        PackageConfiguration packageConf = request.getPackageConfiguration();
        assertThat(packageConf.getPipelineName(), equalTo("pajplajn"));
        assertThat(packageConf.getStageName(), equalTo("stejdz"));
        assertThat(packageConf.getJobName(), equalTo("dzob"));
    }

    @Test
    public void constructorShouldThrowGivenIncompleteJson() throws Exception {
        thrown.expect(IncompleteJson.class);
        thrown.expectMessage("Missing fields: ");
        String json = "{}";
        new ValidateRepositoryConfigurationRequest(json);
    }

    @Test
    public void constructorShouldThrowGivenInvalidJson() throws Exception {
        thrown.expect(InvalidJson.class);
        thrown.expectMessage("Malformed JSON: ");
        String json = "Invalid JSON";
        new ValidateRepositoryConfigurationRequest(json);
    }

}