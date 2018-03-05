/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.executor;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;
import io.github.kszatan.gocd.b2.publish.handlers.bodies.*;
import io.github.kszatan.gocd.b2.publish.storage.ProgressObserver;
import io.github.kszatan.gocd.b2.publish.storage.Storage;
import io.github.kszatan.gocd.b2.publish.storage.StorageException;
import io.github.kszatan.gocd.b2.publish.storage.UnauthorizedCallException;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import static io.github.kszatan.gocd.b2.publish.Constants.GO_ARTIFACTS_B2_BUCKET;

public class PublishTaskExecutor implements TaskExecutor, ProgressObserver {
    public JobConsoleLogger console = new JobConsoleLogger() {
    };
    private Logger logger = Logger.getLoggerFor(PublishTaskExecutor.class);
    private final Storage storage;
    private final DirectoryScanner scanner;

    public PublishTaskExecutor(Storage storage, DirectoryScanner scanner) {
        this.storage = storage;
        this.scanner = scanner;
        storage.addProgressObserver(this);
    }

    @Override
    public ExecuteResponse execute(TaskConfiguration configuration, TaskContext context) {
        ExecuteResponse response = ExecuteResponse.success("Success");
        try {
            List<String> errors = validateContext(context);
            if (!errors.isEmpty()) {
                response = ExecuteResponse.failure("Configuration failure: " + StringUtils.join(errors, "; "));
            } else {
                final int maxAttempts = 5;
                int nthTry = 0;
                Queue<SourceDestination> scannedSourcesQueue =
                        scanSources(configuration.getSourceDestinationsAsList(), context.workingDirectory,
                                configuration.getDestinationPrefix());
                Path absoluteWorkDir = Paths.get(context.workingDirectory).toAbsolutePath();
                while (true) {
                    try {
                        if (!storage.authorize(context.getAccountId(), context.getApplicationKey())) {
                            return ExecuteResponse.failure("Failed to authorize: " + storage.getLastErrorMessage());
                        }
                        while (!scannedSourcesQueue.isEmpty()) {
                            SourceDestination sd = scannedSourcesQueue.poll();
                            storage.upload(absoluteWorkDir, sd.source, sd.destination);
                        }
                        break;
                    } catch (UnauthorizedCallException e) {
                        notify(e.getMessage());
                        nthTry++;
                        if (nthTry > maxAttempts) {
                            throw e;
                        }
                    }
                }
            }
        } catch (GeneralSecurityException | StorageException | RuntimeException e) {
            response = ExecuteResponse.failure(e.getMessage());
        }
        return response;
    }

    @Override
    public void notify(String notification) {
        console.printLine(notification);
    }

    private List<String> validateContext(TaskContext context) {
        List<String> errors = new ArrayList<>();
        ConfigurationValidator validator = new ConfigurationValidator();
        String bucketName = context.environmentVariables.get(GO_ARTIFACTS_B2_BUCKET);
        if (bucketName != null && !validator.validateBucketName(bucketName)) {
            errors.add("Invalid bucket name format in GO_ARTIFACTS_B2_BUCKET environmental variable.");
        }
        if (context.getAccountId().isEmpty() || context.getApplicationKey().isEmpty()) {
            errors.add("Missing B2 credentials. Please set B2_ACCOUNT_ID and B2_APPLICATION_KEY environmental variables.");
        }
        return errors;
    }

    private LinkedList<SourceDestination> scanSources(List<SourceDestination> sourceDestinations, String workingDirectory,
                                                      String destinationPrefix) {
        LinkedList<SourceDestination> expanded = new LinkedList<>();
        scanner.setBaseDir(workingDirectory);
        for (SourceDestination sd : sourceDestinations) {
            scanner.scan(sd.source);
            String destination = sd.destination.isEmpty() ? destinationPrefix : sd.destination;
            List<SourceDestination> prefixed_included = scanner.getIncludedFiles().stream()
                    .map(f -> new SourceDestination(f, destination))
                    .collect(Collectors.toList());
            expanded.addAll(prefixed_included);
        }
        return expanded;
    }
}
