/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.fetch.handlers.bodies;

import java.util.HashMap;
import java.util.Map;

public class TaskContext {
    public Map<String, String> environmentVariables;
    public String workingDirectory;

    private static final String GO_REPO = "GO_REPO_";
    private static final String GO_PACKAGE = "GO_PACKAGE_";
    private static final String BUCKET_NAME = "_BUCKETNAME";
    private static final String ACCOUNT_ID = "_ACCOUNTID";
    private static final String APPLICATION_KEY = "_APPLICATIONKEY";
    private static final String PIPELINE_NAME = "_PIPELINENAME";
    private static final String STAGE_NAME = "_STAGENAME";
    private static final String JOB_NAME = "_JOBNAME";
    private static final String LABEL = "_LABEL";

    public TaskContext() {
        environmentVariables = new HashMap<>();
        workingDirectory = new String();
    }

    public String getAccountId(String repositoryName, String packageName) {
        String accountId = environmentVariables.get(GO_REPO + repositoryPackageEnvPart(repositoryName, packageName) + ACCOUNT_ID);
        return accountId != null ? accountId : "";
    }

    public String getApplicationKey(String repositoryName, String packageName) {
        String applicationKey = environmentVariables.get(GO_REPO + repositoryPackageEnvPart(repositoryName, packageName) + APPLICATION_KEY);
        return applicationKey != null ? applicationKey : "";
    }

    public String getBucketName(String repositoryName, String packageName) {
        String applicationId = environmentVariables.get(GO_REPO + repositoryPackageEnvPart(repositoryName, packageName) + BUCKET_NAME);
        return applicationId != null ? applicationId : "";
    }

    public String getPipelineName(String repositoryName, String packageName) {
        String pipelineName = environmentVariables.get(GO_PACKAGE + repositoryPackageEnvPart(repositoryName, packageName) + PIPELINE_NAME);
        return pipelineName != null ? pipelineName : "";
    }

    public String getStageName(String repositoryName, String packageName) {
        String stageName = environmentVariables.get(GO_PACKAGE + repositoryPackageEnvPart(repositoryName, packageName) + STAGE_NAME);
        return stageName != null ? stageName : "";
    }

    public String getJobName(String repositoryName, String packageName) {
        String jobName = environmentVariables.get(GO_PACKAGE + repositoryPackageEnvPart(repositoryName, packageName) + JOB_NAME);
        return jobName != null ? jobName : "";
    }

    public String getLabel(String repositoryName, String packageName) {
        String label = environmentVariables.get(GO_PACKAGE + repositoryPackageEnvPart(repositoryName, packageName) + LABEL);
        return label != null ? label : "";
    }

    private String repositoryPackageEnvPart(String repositoryName, String packageName) {
        return repositoryName.toUpperCase() + "_" + packageName.toUpperCase();
    }
}
