/*
 * Copyright (c) 2018. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.fetch.executor;

import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;
import io.github.kszatan.gocd.b2.fetch.handlers.bodies.ExecuteResponse;
import io.github.kszatan.gocd.b2.fetch.handlers.bodies.TaskConfiguration;
import io.github.kszatan.gocd.b2.fetch.handlers.bodies.TaskContext;
import io.github.kszatan.gocd.b2.utils.storage.FileName;
import io.github.kszatan.gocd.b2.utils.storage.ListFileNamesResponse;
import io.github.kszatan.gocd.b2.utils.storage.Storage;
import io.github.kszatan.gocd.b2.utils.storage.StorageException;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class FetchTaskExecutorTest {
    private Storage storage;
    private FetchTaskExecutor executor;

    private final String PIPELINE_NAME = "pajplajn";
    private final String STAGE_NAME = "stejdz";
    private final String JOB_NAME = "dzob";
    private final String PIPELINE_COUNTER = "10";
    private final String STAGE_COUNTER = "5";

    private final String REPOSITORY_NAME = "lepo";
    private final String PACKAGE_NAME = "pakedz";
    private final String DESTINATION = "path/to/dest";

    private TaskContext getDefaultTaskContext() {
        TaskContext context = new TaskContext();
        context.workingDirectory = "pipelines/pajplajn/";
        String accountId = "4abcdefgaf77";
        String applicationKey = "caca85ed4e7a3404db0b08bb8256d00d84e247e46";
        context.environmentVariables.put("B2_ACCOUNT_ID", accountId);
        context.environmentVariables.put("B2_APPLICATION_KEY", applicationKey);
        context.environmentVariables.put("GO_TRIGGER_USER", "luser");
        context.environmentVariables.put("GO_SERVER_URL", "https://localhost:8154/go");
        setDefaultPipelineInfo(context);
        setDefaultRepositoryInfo(context);
        setDefaultPackageInfo(context);
        return context;
    }

    private TaskConfiguration getDefaultTaskConfiguration() {
        TaskConfiguration configuration = new TaskConfiguration();
        configuration.setRepositoryName(REPOSITORY_NAME);
        configuration.setPackageName(PACKAGE_NAME);
        configuration.setDestination(DESTINATION);
        return configuration;
    }

    private void setDefaultPipelineInfo(TaskContext context) {
        context.environmentVariables.put("GO_PIPELINE_NAME", PIPELINE_NAME);
        context.environmentVariables.put("GO_STAGE_NAME", STAGE_NAME);
        context.environmentVariables.put("GO_JOB_NAME", JOB_NAME);
        context.environmentVariables.put("GO_PIPELINE_COUNTER", PIPELINE_COUNTER);
        context.environmentVariables.put("GO_STAGE_COUNTER", STAGE_COUNTER);
    }

    private void setDefaultRepositoryInfo(TaskContext context) {
        context.environmentVariables.put("GO_REPO_LEPO_PAKEDZ_ACCOUNTID", "4aa43473af77");
        context.environmentVariables.put("GO_REPO_LEPO_PAKEDZ_APPLICATIONKEY", "daca85ed4e7a3404db58ab582536d00d84e247e46");
        context.environmentVariables.put("GO_REPO_LEPO_PAKEDZ_BUCKETNAME", "bukhet");
    }
    private void setDefaultPackageInfo(TaskContext context) {
        context.environmentVariables.put("GO_PACKAGE_LEPO_PAKEDZ_PIPELINENAME", "up42");
        context.environmentVariables.put("GO_PACKAGE_LEPO_PAKEDZ_STAGENAME", "up42_stage");
        context.environmentVariables.put("GO_PACKAGE_LEPO_PAKEDZ_JOBNAME", "up42_job");
        context.environmentVariables.put("GO_PACKAGE_LEPO_PAKEDZ_LABEL", "63.1");
    }


    @Before
    public void setUp() {
        storage = mock(Storage.class);
        executor = new FetchTaskExecutor(storage);
        executor.console = mock(JobConsoleLogger.class);
    }

    @Test
    public void executorShouldAddItselfToStorageProgressObservers() {
        FetchTaskExecutor executor = new FetchTaskExecutor(storage);
        verify(storage).addProgressObserver(executor);
    }

    @Test
    public void executeShouldReturnErrorForEmptyBucketName() {
        TaskConfiguration configuration = getDefaultTaskConfiguration();
        TaskContext context = getDefaultTaskContext();
        context.environmentVariables.remove("GO_REPO_LEPO_PAKEDZ_BUCKETNAME");
        ExecuteResponse response = executor.execute(configuration, context);
        assertThat(response.success, equalTo(false));
        assertThat(response.message, equalTo("Configuration failure: Missing bucket name for 'lepo' repository and 'pakedz' package."));
    }

    @Test
    public void executeShouldReturnErrorForEmptyAccountId() {
        TaskConfiguration configuration = getDefaultTaskConfiguration();
        TaskContext context = getDefaultTaskContext();
        context.environmentVariables.remove("GO_REPO_LEPO_PAKEDZ_ACCOUNTID");
        ExecuteResponse response = executor.execute(configuration, context);
        assertThat(response.success, equalTo(false));
        assertThat(response.message, equalTo("Configuration failure: Missing B2 credentials for 'lepo' repository and 'pakedz' package."));
    }

    @Test
    public void executeShouldReturnErrorForEmptyApplicationKey() {
        TaskConfiguration configuration = getDefaultTaskConfiguration();
        TaskContext context = getDefaultTaskContext();
        context.environmentVariables.remove("GO_REPO_LEPO_PAKEDZ_APPLICATIONKEY");
        ExecuteResponse response = executor.execute(configuration, context);
        assertThat(response.success, equalTo(false));
        assertThat(response.message, equalTo("Configuration failure: Missing B2 credentials for 'lepo' repository and 'pakedz' package."));
    }

    @Test
    public void executeShouldAuthorizeWithCredentialForGivenRepositoryAndPackage() throws Exception {
        TaskConfiguration configuration = getDefaultTaskConfiguration();
        TaskContext context = getDefaultTaskContext();
        String accountId = "4aa43473af77";
        String applicationKey = "daca85ed4e7a3404db58ab582536d00d84e247e46";
        context.environmentVariables.put("GO_REPO_LEPO_PAKEDZ_ACCOUNTID", accountId);
        context.environmentVariables.put("GO_REPO_LEPO_PAKEDZ_APPLICATIONKEY", applicationKey);
        executor.execute(configuration, context);
        verify(storage).authorize();
    }

    @Test
    public void executeShouldFetchFileListForCorrectPrefix() throws Exception {
        doReturn(true).when(storage).authorize();
        TaskConfiguration configuration = getDefaultTaskConfiguration();
        TaskContext context = getDefaultTaskContext();
        executor.execute(configuration, context);

        verify(storage).listFiles(null, "up42/up42_stage/up42_job/63.1/", "*");
    }

    @Test
    public void executeShouldFetchFileListSecondTimeIfNextFileNamePresent() throws Exception {
        doReturn(true).when(storage).authorize();
        ListFileNamesResponse listFileNamesResponse = new ListFileNamesResponse();
        listFileNamesResponse.nextFileName = "nextFile";
        doReturn(Optional.of(listFileNamesResponse))
                .doReturn(Optional.of(new ListFileNamesResponse()))
                .when(storage).listFiles(any(), any(), any());
        TaskConfiguration configuration = getDefaultTaskConfiguration();
        TaskContext context = getDefaultTaskContext();
        executor.execute(configuration, context);

        verify(storage).listFiles(null, "up42/up42_stage/up42_job/63.1/", "*");
        verify(storage).listFiles("nextFile", "up42/up42_stage/up42_job/63.1/", "*");
    }

    @Test
    public void executorShouldReturnErrorWhenFileListEmpty() throws StorageException {
        doReturn(true).when(storage).authorize();
        doReturn(Optional.of(new ListFileNamesResponse())).when(storage).listFiles(any(), any(), any());
        TaskConfiguration configuration = getDefaultTaskConfiguration();
        TaskContext context = getDefaultTaskContext();
        ExecuteResponse response = executor.execute(configuration, context);
        assertThat(response.success, equalTo(false));
        assertThat(response.message, equalTo("No files found under 'up42/up42_stage/up42_job/63.1/' path."));
    }

    @Test
    public void executorShouldDownloadFilesToAppropriateDestinations() throws Exception {
        doReturn(true).when(storage).authorize();
        ListFileNamesResponse listFileNamesResponse = new ListFileNamesResponse();
        List<String> fileNames = Arrays.asList("up42/up42_stage/up42_job/63.1/a/file1",
                "up42/up42_stage/up42_job/63.1/b/file2",
                "up42/up42_stage/up42_job/63.1/file3",
                "up42/up42_stage/up42_job/63.1/c/d/e/f/file4.txt");
        listFileNamesResponse.fileNames = fileNames.stream().map(name -> {
            FileName fileName = new FileName();
            fileName.fileName = name;
            return fileName;
        }).collect(Collectors.toList());
        doReturn(Optional.of(listFileNamesResponse)).when(storage).listFiles(any(), any(), any());
        TaskConfiguration configuration = getDefaultTaskConfiguration();
        TaskContext context = getDefaultTaskContext();
        ExecuteResponse response = executor.execute(configuration, context);
        assertThat(response.success, equalTo(true));
        Path destination = Paths.get("pipelines/pajplajn/path/to/dest").toAbsolutePath();
        verify(storage).download("a/file1", destination, "up42/up42_stage/up42_job/63.1/");
        verify(storage).download("b/file2", destination, "up42/up42_stage/up42_job/63.1/");
        verify(storage).download("file3", destination, "up42/up42_stage/up42_job/63.1/");
        verify(storage).download("c/d/e/f/file4.txt", destination, "up42/up42_stage/up42_job/63.1/");
    }
}