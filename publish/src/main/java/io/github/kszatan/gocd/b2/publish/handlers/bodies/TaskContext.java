/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers.bodies;

import java.util.HashMap;
import java.util.Map;

public class TaskContext {
    public Map<String, String> environmentVariables;
    public String workingDirectory;

    private static final String B2_ACCOUNT_ID = "B2_ACCOUNT_ID";
    private static final String B2_APPLICATION_KEY = "B2_APPLICATION_KEY";
    private static final String GO_PIPELINE_NAME = "GO_PIPELINE_NAME";
    private static final String GO_STAGE_NAME = "GO_STAGE_NAME";
    private static final String GO_JOB_NAME = "GO_JOB_NAME";
    private static final String GO_PIPELINE_COUNTER = "GO_PIPELINE_COUNTER";
    private static final String GO_STAGE_COUNTER = "GO_STAGE_COUNTER";

    public TaskContext() {
        environmentVariables = new HashMap<>();
        workingDirectory = new String();
    }

    public String getAccountId() {
        String accountId = environmentVariables.get(B2_ACCOUNT_ID);
        return accountId != null ? accountId : "";
    }
    
    public String getApplicationKey() {
        String applicationId = environmentVariables.get(B2_APPLICATION_KEY);
        return applicationId != null ? applicationId : "";
    }

    public String getPipelineName() {
        String applicationId = environmentVariables.get(GO_PIPELINE_NAME);
        return applicationId != null ? applicationId : "";
    }

    public String getStageName() {
        String applicationId = environmentVariables.get(GO_STAGE_NAME);
        return applicationId != null ? applicationId : "";
    }

    public String getJobName() {
        String applicationId = environmentVariables.get(GO_JOB_NAME);
        return applicationId != null ? applicationId : "";
    }

    public String getPipelineCounter() {
        String applicationId = environmentVariables.get(GO_PIPELINE_COUNTER);
        return applicationId != null ? applicationId : "";
    }

    public String getStageCounter() {
        String applicationId = environmentVariables.get(GO_STAGE_COUNTER);
        return applicationId != null ? applicationId : "";
    }
}
