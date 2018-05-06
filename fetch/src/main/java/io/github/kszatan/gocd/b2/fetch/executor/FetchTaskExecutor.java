/*
 * Copyright (c) 2018. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.fetch.executor;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;
import io.github.kszatan.gocd.b2.fetch.handlers.bodies.ExecuteResponse;
import io.github.kszatan.gocd.b2.fetch.handlers.bodies.TaskConfiguration;
import io.github.kszatan.gocd.b2.fetch.handlers.bodies.TaskContext;
import io.github.kszatan.gocd.b2.utils.storage.*;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FetchTaskExecutor implements TaskExecutor, ProgressObserver {
    public JobConsoleLogger console = new JobConsoleLogger() {
    };
    private Logger logger = Logger.getLoggerFor(FetchTaskExecutor.class);
    private final Storage storage;

    public FetchTaskExecutor(Storage storage) {
        this.storage = storage;
        storage.addProgressObserver(this);
    }

    @Override
    public ExecuteResponse execute(TaskConfiguration configuration, TaskContext context) {
        ExecuteResponse response = ExecuteResponse.success("Success");
        String repositoryName = configuration.getRepositoryName();
        String packageName = configuration.getPackageName();
        try {
            validateContext(repositoryName, packageName, context);
            final int maxAttempts = 5;
            int nthTry = 0;
            while (true) {
                try {
                    authorize(context, repositoryName, packageName);
                    String packagePrefix = getPackagePrefix(repositoryName, packageName, context);
                    LinkedList<FileName> fileNames = fetchFileNames(packagePrefix);
                    if (fileNames.isEmpty()) {
                        throw new StorageException("No files found under '" + packagePrefix + "' path.");
                    }
                    Path downloadPath = getDownloadPath(context, configuration);
                    downloadFiles(fileNames, downloadPath);
                    break;
                } catch (UnauthorizedCallException e) {
                    notify(e.getMessage());
                    nthTry++;
                    if (nthTry > maxAttempts) {
                        throw e;
                    }
                }
            }
        } catch (StorageException | RuntimeException e) {
            response = ExecuteResponse.failure(e.getMessage());
        }
        return response;
    }

    private void authorize(TaskContext context, String repositoryName, String packageName) throws StorageException {
        String accountId = context.getAccountId(repositoryName, packageName);
        String applicationKey = context.getApplicationKey(repositoryName, packageName);
        if (!storage.authorize(accountId, applicationKey)) {
            throw new StorageException("Failed to authorize: " + storage.getLastErrorMessage());
        }
    }

    private LinkedList<FileName> fetchFileNames(String prefix) throws StorageException {
        LinkedList<FileName> fileNames = new LinkedList<>();
        String nextFileName = null;
        do {
            Optional<ListFileNamesResponse> maybeResponse = storage.listFiles(nextFileName, prefix, "*");
            if (maybeResponse.isPresent()) {
                ListFileNamesResponse response = maybeResponse.get();
                fileNames.addAll(response.fileNames);
                nextFileName = response.nextFileName;
            }
        } while (nextFileName != null);
        return fileNames;
    }

    private void downloadFiles(Queue<FileName> fileNames, Path downloadPath) throws StorageException {
        while (!fileNames.isEmpty()) {
            FileName fileName = fileNames.peek();
            storage.download(fileName.fileName, downloadPath);
            notify("Successfully downloaded " + fileName.fileName + " to " + downloadPath + ".");
            fileNames.remove();
        }
    }

    @Override
    public void notify(String notification) {
        console.printLine(notification);
    }

    private void validateContext(String repositoryName, String packageName, TaskContext context) {
        List<String> errors = new ArrayList<>();
        if (context.getAccountId(repositoryName, packageName).isEmpty()
                || context.getApplicationKey(repositoryName, packageName).isEmpty()) {
            errors.add("Missing B2 credentials for '"
                    + repositoryName + "' repository and '"
                    + packageName + "' package.");
        }
        if (context.getBucketName(repositoryName, packageName).isEmpty()) {
            errors.add("Missing bucket name for '"
                    + repositoryName + "' repository and '"
                    + packageName + "' package.");
        }
        if (!errors.isEmpty()) {
            throw new RuntimeException("Configuration failure: " + StringUtils.join(errors, "; "));
        }
    }

    private String getPackagePrefix(String repositoryName, String packageName, TaskContext context) {
        return context.getPipelineName(repositoryName, packageName) + "/"
                + context.getStageName(repositoryName, packageName) + "/"
                + context.getJobName(repositoryName, packageName) + "/"
                + context.getLabel(repositoryName, packageName) + "/";
    }

    private Path getDownloadPath(TaskContext context, TaskConfiguration configuration) {
        return Paths.get(context.workingDirectory, configuration.getDestination()).toAbsolutePath();
    }
}
