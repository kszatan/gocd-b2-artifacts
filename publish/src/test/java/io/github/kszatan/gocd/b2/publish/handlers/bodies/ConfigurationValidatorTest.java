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
    private Boolean valid;

    @Parameterized.Parameters
    public static Collection data() {
        return Arrays.asList(new Object[][]{
                {"short", false},
                {"123456789012345678901234567890123456789012345678901", false},
                {"12345678901234567890123456789012345678901234567890", true},
                {"b2-bucket", false},
                {"asdf-b2-asdf", true},
                {"ADSDF2342fda-", true},
                {"asdf_fdsa", false}
        });
    }

    public ConfigurationValidatorTest(String bucketId, Boolean valid) {
        this.bucketId = bucketId;
        this.valid = valid;
    }

    @Test
    public void validateShouldWorkForRemoteUrls() throws Exception {
        ConfigurationValidator validator = new ConfigurationValidator();
        TaskConfiguration configuration = new TaskConfiguration();
        configuration.setBucketId(bucketId);
        assertThat(validator.validate(configuration).errors.isEmpty(), is(valid));
    }
}