/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.fetch.handlers.bodies;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TaskContextTest {
    private final String REPOSITORY_NAME = "repository";
    private final String PACKAGE_NAME = "package";
    private TaskContext context;

    @Before
    public void SetUp() {
        context = new TaskContext();
    }

    @Test
    public void getAccountId() throws Exception {
        String accountId = "my_account_id";
        context.environmentVariables.put("GO_REPO_REPOSITORY_PACKAGE_ACCOUNTID", accountId);
        assertThat(context.getAccountId(REPOSITORY_NAME, PACKAGE_NAME), equalTo(accountId));
    }

    @Test
    public void getApplicationKey() throws Exception {
        String applicationKey = "my_application_key";
        context.environmentVariables.put("GO_REPO_REPOSITORY_PACKAGE_APPLICATIONKEY", applicationKey);
        assertThat(context.getApplicationKey(REPOSITORY_NAME, PACKAGE_NAME), equalTo(applicationKey));
    }

    @Test
    public void getBucketName() throws Exception {
        String bucketName = "bukhet";
        context.environmentVariables.put("GO_REPO_REPOSITORY_PACKAGE_BUCKETNAME", bucketName);
        assertThat(context.getBucketName(REPOSITORY_NAME, PACKAGE_NAME), equalTo(bucketName));
    }

    @Test
    public void getPipelineName() throws Exception {
        String pipelineName = "pajplajn";
        context.environmentVariables.put("GO_PACKAGE_REPOSITORY_PACKAGE_PIPELINENAME", pipelineName);
        assertThat(context.getPipelineName(REPOSITORY_NAME, PACKAGE_NAME), equalTo(pipelineName));
    }

    @Test
    public void getStageName() throws Exception {
        String stageName = "stejdz";
        context.environmentVariables.put("GO_PACKAGE_REPOSITORY_PACKAGE_STAGENAME", stageName);
        assertThat(context.getStageName(REPOSITORY_NAME, PACKAGE_NAME), equalTo(stageName));
    }

    @Test
    public void getJobName() throws Exception {
        String jobName = "dzob";
        context.environmentVariables.put("GO_PACKAGE_REPOSITORY_PACKAGE_JOBNAME", jobName);
        assertThat(context.getJobName(REPOSITORY_NAME, PACKAGE_NAME), equalTo(jobName));
    }

    @Test
    public void getLabel() throws Exception {
        String label = "lejbel";
        context.environmentVariables.put("GO_PACKAGE_REPOSITORY_PACKAGE_LABEL", label);
        assertThat(context.getLabel(REPOSITORY_NAME, PACKAGE_NAME), equalTo(label));
    }
}