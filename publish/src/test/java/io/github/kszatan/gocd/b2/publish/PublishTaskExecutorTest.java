/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish;

import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;
import io.github.kszatan.gocd.b2.publish.executor.DirectoryScanner;
import io.github.kszatan.gocd.b2.publish.executor.PublishTaskExecutor;
import io.github.kszatan.gocd.b2.publish.handlers.bodies.ExecuteResponse;
import io.github.kszatan.gocd.b2.publish.handlers.bodies.TaskConfiguration;
import io.github.kszatan.gocd.b2.publish.handlers.bodies.TaskContext;
import io.github.kszatan.gocd.b2.publish.storage.Storage;
import io.github.kszatan.gocd.b2.publish.storage.StorageException;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static io.github.kszatan.gocd.b2.publish.Constants.GO_ARTIFACTS_B2_BUCKET;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class PublishTaskExecutorTest {
    private Storage storage;
    private DirectoryScanner scanner;
    private PublishTaskExecutor executor;

    @Before
    public void setUp() throws Exception {
        storage = mock(Storage.class);
        scanner = mock(DirectoryScanner.class);
        executor = new PublishTaskExecutor(storage, scanner);
        executor.console = mock(JobConsoleLogger.class);
    }

    @Test
    public void executorShouldAddItselfToStorageProgressObservers() {
        PublishTaskExecutor executor = new PublishTaskExecutor(storage, scanner);
        verify(storage).addProgressObserver(executor);
    }
    
    @Test
    public void executeShouldReturnErrorForInvalidEnvironmentBucketName() {
        TaskConfiguration configuration = new TaskConfiguration();
        TaskContext context = new TaskContext();
        context.environmentVariables.put(GO_ARTIFACTS_B2_BUCKET, "buk");
        ExecuteResponse response = executor.execute(configuration, context);
        assertThat(response.success, equalTo(false));
        assertThat(response.message, equalTo("Configuration failure: Invalid bucket name format in GO_ARTIFACTS_B2_BUCKET environmental variable"));
    }

    @Test
    public void executeShouldAuthorizeWithGivenCredentials() throws Exception {
        TaskConfiguration configuration = new TaskConfiguration();
        TaskContext context = new TaskContext();
        context.environmentVariables.put(GO_ARTIFACTS_B2_BUCKET, "bukiet");
        String accountId = "4abcdefgaf77";
        String applicationKey = "caca85ed4e7a3404db0b08bb8256d00d84e247e46";
        context.environmentVariables.put("B2_ACCOUNT_ID", accountId);
        context.environmentVariables.put("B2_APPLICATION_KEY", applicationKey);
        executor.execute(configuration, context);
        verify(storage).authorize(accountId, applicationKey);
    }

    @Test
    public void executeShouldUploadPrefixedFilesReturnedFromScanner() throws StorageException {
        when(storage.authorize(any(), any())).thenReturn(true);
        when(scanner.getIncludedFiles()).thenReturn(Arrays.asList("a/file1", "file2", "b/c/file3"));
        TaskConfiguration configuration = new TaskConfiguration();
        configuration.setSourceDestinations("[{\"source\": \"**\", \"destination\": \"desti/nation\"}]");
        TaskContext context = new TaskContext();
        context.environmentVariables.put(GO_ARTIFACTS_B2_BUCKET, "bukiet");
        context.workingDirectory = "gocd/agent/";
        executor.execute(configuration, context);
        final Path workDirPath = Paths.get(context.workingDirectory).toAbsolutePath();
        
        verify(storage).upload(workDirPath, "a/file1", "desti/nation");
        verify(storage).upload(workDirPath, "file2", "desti/nation");
        verify(storage).upload(workDirPath, "b/c/file3", "desti/nation");
    }

    @Test
    public void executorShouldPassAllSourcesToSccanner() throws StorageException {
        when(storage.authorize(any(), any())).thenReturn(true);
        when(scanner.getIncludedFiles()).thenReturn(Arrays.asList("a/file1", "file2", "b/c/file3"));
        TaskConfiguration configuration = new TaskConfiguration();
        configuration.setSourceDestinations("[{\"source\": \"source1/*\", \"destination\": \"\"},{\"source\": \"source2/*\", \"destination\": \"\"}]");
        TaskContext context = new TaskContext();
        context.environmentVariables.put(GO_ARTIFACTS_B2_BUCKET, "bukiet");
        executor.execute(configuration, context);

        verify(scanner).scan("source1/*");
        verify(scanner).scan("source2/*");
    }

    @Test
    public void executorShouldUploadFilesToAppropriateDestinations() throws StorageException {
        when(storage.authorize(any(), any())).thenReturn(true);
        when(scanner.getIncludedFiles()).thenReturn(Arrays.asList("file1", "file2"));
        TaskConfiguration configuration = new TaskConfiguration();
        configuration.setSourceDestinations("[{\"source\": \"**\", \"destination\": \"dest1\"},{\"source\": \"**\", \"destination\": \"dest2\"}]");
        TaskContext context = new TaskContext();
        context.environmentVariables.put(GO_ARTIFACTS_B2_BUCKET, "bukiet");
        context.workingDirectory = "gocd/agent/";
        executor.execute(configuration, context);
        final Path workDirPath = Paths.get(context.workingDirectory).toAbsolutePath();

        verify(storage).upload(workDirPath, "file1", "dest1");
        verify(storage).upload(workDirPath, "file2", "dest1");
        verify(storage).upload(workDirPath, "file1", "dest2");
        verify(storage).upload(workDirPath, "file2", "dest2");
    }

    @Test
    public void scannerShouldGetWorkDirPassedInContext() throws StorageException {
        when(storage.authorize(any(), any())).thenReturn(true);
        String workDir = "base/dir";
        TaskConfiguration configuration = new TaskConfiguration();
        TaskContext context = new TaskContext();
        context.environmentVariables.put(GO_ARTIFACTS_B2_BUCKET, "bukiet");
        context.workingDirectory = workDir;
        executor.execute(configuration, context);

        verify(scanner).setBaseDir(workDir);
    }
}