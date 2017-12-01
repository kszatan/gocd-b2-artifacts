/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * op-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers.bodies;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class ConfigurationValidatorTest {
    private String bucketId;
    private String sourceDestinations;
    private Boolean valid;

    @Parameterized.Parameters
    public static Collection data() {
        final String validSD = "[{\"source\": \"asdf\", \"destination\": \"fdsa\"}]";
        final String emptySource = "[{\"source\": \"\", \"destination\": \"fdsa\"}]";
        return Arrays.asList(new Object[][]{
                {"short", validSD, false},
                {"123456789012345678901234567890123456789012345678901", validSD, false},
                {"12345678901234567890123456789012345678901234567890", validSD, true},
                {"b2-bucket", validSD, false},
                {"asdf-b2-asdf", validSD, true},
                {"ADSDF2342fda-", validSD, true},
                {"asdf_fdsa", validSD, false},
                {"ADSDF2342fda-", "", false},
                {"ADSDF2342fda-", "[]", false},
                {"ADSDF2342fda-", emptySource, false},
                {"ADSDF2342fda-", "//", false}
        });
    }

    public ConfigurationValidatorTest(String bucketId, String sourceDestinations, Boolean valid) {
        this.bucketId = bucketId;
        this.sourceDestinations = sourceDestinations;
        this.valid = valid;
    }

    @Test
    public void validateShouldWorkForRemoteUrls() throws Exception {
        ConfigurationValidator validator = new ConfigurationValidator();
        TaskConfiguration configuration = new TaskConfiguration();
        configuration.setBucketId(bucketId);
        configuration.setSourceDestinations(sourceDestinations);
        assertThat(validator.validate(configuration).errors.isEmpty(), is(valid));
    }
}