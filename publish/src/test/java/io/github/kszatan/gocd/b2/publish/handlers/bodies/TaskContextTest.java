/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers.bodies;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class TaskContextTest {
    @Test
    public void getAccountId() throws Exception {
        TaskContext context = new TaskContext();
        String accountId = "my_account_id";
        context.environmentVariables.put("B2_ACCOUNT_ID", accountId);
        assertThat(context.getAccountId(), equalTo(accountId));
    }

    @Test
    public void getApplicationId() throws Exception {
        TaskContext context = new TaskContext();
        String applicationKey = "my_application_key";
        context.environmentVariables.put("B2_APPLICATION_KEY", applicationKey);
        assertThat(context.getApplicationKey(), equalTo(applicationKey));
    }

    @Test
    public void getPipelineName() throws Exception {
        TaskContext context = new TaskContext();
        String pipelineName = "pipeline name";
        context.environmentVariables.put("GO_PIPELINE_NAME", pipelineName);
        assertThat(context.getPipelineName(), equalTo(pipelineName));
    }

    @Test
    public void getStageName() throws Exception {
        TaskContext context = new TaskContext();
        String stageName = "stage name";
        context.environmentVariables.put("GO_STAGE_NAME", stageName);
        assertThat(context.getStageName(), equalTo(stageName));
    }

    @Test
    public void getJobName() throws Exception {
        TaskContext context = new TaskContext();
        String jobName = "job name";
        context.environmentVariables.put("GO_JOB_NAME", jobName);
        assertThat(context.getJobName(), equalTo(jobName));
    }

    @Test
    public void getPipelineCounter() throws Exception {
        TaskContext context = new TaskContext();
        String pipelineCounter = "3";
        context.environmentVariables.put("GO_PIPELINE_COUNTER", pipelineCounter);
        assertThat(context.getPipelineCounter(), equalTo(pipelineCounter));
    }

    @Test
    public void getStageCounter() throws Exception {
        TaskContext context = new TaskContext();
        String stageCounter = "10";
        context.environmentVariables.put("GO_STAGE_COUNTER", stageCounter);
        assertThat(context.getStageCounter(), equalTo(stageCounter));
    }





}