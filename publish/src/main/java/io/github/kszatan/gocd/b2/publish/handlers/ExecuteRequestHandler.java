/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.github.kszatan.gocd.b2.publish.executor.DefaultDirectoryScanner;
import io.github.kszatan.gocd.b2.publish.executor.PublishTaskExecutor;
import io.github.kszatan.gocd.b2.publish.executor.TaskExecutor;
import io.github.kszatan.gocd.b2.publish.handlers.bodies.ExecuteRequest;
import io.github.kszatan.gocd.b2.publish.handlers.bodies.ExecuteResponse;
import io.github.kszatan.gocd.b2.publish.handlers.bodies.TaskConfiguration;
import io.github.kszatan.gocd.b2.publish.handlers.bodies.TaskContext;
import io.github.kszatan.gocd.b2.publish.json.IncompleteJson;
import io.github.kszatan.gocd.b2.publish.json.InvalidJson;
import io.github.kszatan.gocd.b2.publish.storage.BackblazeStorage;
import io.github.kszatan.gocd.b2.publish.storage.StorageException;

import java.util.Optional;

import static io.github.kszatan.gocd.b2.publish.Constants.GO_ARTIFACTS_B2_BUCKET;

public class ExecuteRequestHandler implements RequestHandler {
    private TaskExecutor executor;
    
    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) {
        GoPluginApiResponse response;
        try {
            ExecuteRequest executeRequest = ExecuteRequest.create(request.requestBody());
            TaskConfiguration configuration = executeRequest.getTaskConfiguration();
            TaskContext context = executeRequest.getTaskContext();
            if (executor == null) { // for tests
                String bucketName = getBucketName(configuration, context).orElseThrow(
                        () -> new StorageException("Bucket name not specified"));
                setExecutor(new PublishTaskExecutor(new BackblazeStorage(bucketName), new DefaultDirectoryScanner()));
            }
            ExecuteResponse result = executor.execute(configuration, context);
            response = DefaultGoPluginApiResponse.success(result.toJson());
        } catch (StorageException e) {
            response = DefaultGoPluginApiResponse.error(e.getMessage());
        } catch (InvalidJson e) {
            response = DefaultGoPluginApiResponse.error("InvalidJSON: " + e.getMessage());
        } catch (IncompleteJson e) {
            response = DefaultGoPluginApiResponse.incompleteRequest(e.getMessage());
        }
        return response;
    }

    public void setExecutor(TaskExecutor executor) {
        this.executor = executor;
    }

    private Optional<String> getBucketName(TaskConfiguration configuration, TaskContext context) {
        String bucketName = configuration.getBucketName();
        if (bucketName == null || bucketName.isEmpty()) {
            bucketName = context.environmentVariables.get(GO_ARTIFACTS_B2_BUCKET);
        }
        return Optional.ofNullable(bucketName);
    }

}
