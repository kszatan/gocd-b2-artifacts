/*
 * Copyright (c) 2017 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.fetch.handlers.bodies;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ConfigurationValidatorTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void validateShouldReportEmptyRepositoryName() throws Exception {
        ConfigurationValidator validator = new ConfigurationValidator();
        TaskConfiguration configuration = new TaskConfiguration();
        configuration.setRepositoryName("");
        configuration.setPackageName("package");
        Map<String, String> errors = validator.validate(configuration).errors;
        assertThat(errors.size(), equalTo(1));
        assertThat(errors.get("repositoryName"), equalTo("Missing repository name"));
    }

    @Test
    public void validateShouldReportEmptyPackageName() throws Exception {
        ConfigurationValidator validator = new ConfigurationValidator();
        TaskConfiguration configuration = new TaskConfiguration();
        configuration.setRepositoryName("repository");
        configuration.setPackageName("");
        Map<String, String> errors = validator.validate(configuration).errors;
        assertThat(errors.size(), equalTo(1));
        assertThat(errors.get("packageName"), equalTo("Missing package name"));
    }
}