/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.json;

import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class GsonServiceTest {
    private static String json = "{\"revision\": \"revision-1\", \"timestamp\": \"2011-07-14T19:43:37.100Z\", \"user\": \"some-user\", \"revisionComment\": \"comment\", \"data\": {}, \"modifiedFiles\": [{\"fileName\": \"file-1\", \"action\": \"added\"} ] }";
    @Test
    public void validateShouldReturnEmptyCollectionWhenGivenEmptyRequiredFields() throws Exception {
        List<String> requiredFields = new ArrayList<>();
        assertThat(GsonService.validate(json, requiredFields), equalTo(new ArrayList<String>()));
    }

    @Test
    public void validateShouldReturnMissingFieldWhenGivenMissingField() throws Exception {
        List<String> requiredFields = Collections.singletonList("magnetic");
        Collection<String> missing = GsonService.validate(json, requiredFields);
        assertThat(missing.size(), is(1));
        String field = missing.iterator().next();
        assertThat(field, equalTo("magnetic"));
    }

    @Test
    public void validateShouldReturnEmptyCollectionWhenGivenPresentFields() throws Exception {
        List<String> requiredFields = Arrays.asList("revision", "timestamp", "user", "data", "modifiedFiles");
        assertThat(GsonService.validate(json, requiredFields).isEmpty(), is(true));
    }

    @Test
    public void getFieldShouldReturnJsonPartForGivenField() {
        String json = "{\"scm-configuration\":{\"url\":{\"value\":\"repourl\"},\"username\":{\"value\":\"kszatan\"},\"password\":{\"value\":\"hunter2\"}}}";
        String configurationJson = GsonService.getField(json, "scm-configuration");
        assertThat(configurationJson, equalTo("{\"url\":{\"value\":\"repourl\"},\"username\":{\"value\":\"kszatan\"},\"password\":{\"value\":\"hunter2\"}}"));
    }
}