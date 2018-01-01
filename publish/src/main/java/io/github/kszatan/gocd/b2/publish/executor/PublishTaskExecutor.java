/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.executor;

import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;
import io.github.kszatan.gocd.b2.publish.handlers.bodies.*;
import io.github.kszatan.gocd.b2.publish.storage.Storage;
import io.github.kszatan.gocd.b2.publish.storage.StorageException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.kszatan.gocd.b2.publish.Constants.GO_ARTIFACTS_B2_BUCKET;

public class PublishTaskExecutor implements TaskExecutor {
    public JobConsoleLogger console = new JobConsoleLogger() {};
    private final Storage storage;
    private final DirectoryScanner scanner;

    public PublishTaskExecutor(Storage storage, DirectoryScanner scanner) {
        this.storage = storage;
        this.scanner = scanner;
    }
    
    @Override
    public ExecuteResponse execute(TaskConfiguration configuration, TaskContext context) {
        ExecuteResponse response = ExecuteResponse.success("Success");
        try {
            List<String> errors = validateEnvironment(context.environmentVariables);
            if (!errors.isEmpty()) {
                response = ExecuteResponse.failure("Failure: " + StringUtils.join(errors, "; "));
            } else {
                storage.authorize(context.getAccountId(), context.getApplicationKey());
                List<SourceDestination> expandSources =
                        expandSources(configuration.getSourceDestinationsAsList(), context.workingDirectory,
                                configuration.getDestinationPrefix());
                for (SourceDestination sd : expandSources) {
                    storage.upload(sd.source, sd.destination);
                    console.printLine("Uploaded: " + sd.source + " to: " + sd.destination);
                }
            }
        } catch (StorageException | RuntimeException e ) {
            response = ExecuteResponse.failure(e.getMessage());
        }
        return response;
    }

    private List<String> validateEnvironment(Map<String, String> environment) {
        List<String> errors = new ArrayList<>();
        ConfigurationValidator validator = new ConfigurationValidator();
        String bucketId = environment.get(GO_ARTIFACTS_B2_BUCKET);
        if (bucketId != null && !validator.validateBucketId(bucketId)) {
            errors.add("Invalid Bucket ID format in GO_ARTIFACTS_B2_BUCKET environmental variable");
        }
        return errors;
    }

    private List<SourceDestination> expandSources(List<SourceDestination> sourceDestinations, String workingDirectory,
                                                  String destinationPrefix) {
        List<SourceDestination> expanded = new ArrayList<>();
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