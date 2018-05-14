/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.github.kszatan.gocd.b2.material.handlers.bodies.PackageConfiguration;
import io.github.kszatan.gocd.b2.material.handlers.bodies.StatusMessagesResponse;
import io.github.kszatan.gocd.b2.material.handlers.bodies.CheckPackageConnectionRequest;
import io.github.kszatan.gocd.b2.material.handlers.bodies.RepositoryConfiguration;
import io.github.kszatan.gocd.b2.utils.json.IncompleteJson;
import io.github.kszatan.gocd.b2.utils.json.InvalidJson;
import io.github.kszatan.gocd.b2.utils.storage.BackblazeStorage;
import io.github.kszatan.gocd.b2.utils.storage.ListFileNamesResponse;
import io.github.kszatan.gocd.b2.utils.storage.Storage;
import io.github.kszatan.gocd.b2.utils.storage.StorageException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Optional;

public class CheckPackageConnectionRequestHandler implements RequestHandler {
    private Storage storage;

    public CheckPackageConnectionRequestHandler() throws IOException {
        this.storage = new BackblazeStorage();
    }

    public CheckPackageConnectionRequestHandler(Storage storage) {
        this.storage = storage;
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) {
        GoPluginApiResponse response;
        try {
            CheckPackageConnectionRequest checkRepositoryConnectionRequest =
                    new CheckPackageConnectionRequest(request.requestBody());
            RepositoryConfiguration repositoryConfiguration = checkRepositoryConnectionRequest.getRepositoryConfiguration();
            storage.setCredentials(repositoryConfiguration.getAccountId(), repositoryConfiguration.getApplicationKey());
            storage.setBucketName(repositoryConfiguration.getBucketName());
            StatusMessagesResponse statusMessagesResponse;
            if (!storage.authorize()) {
                statusMessagesResponse =
                        StatusMessagesResponse.failure(Arrays.asList(storage.getLastErrorMessage()));
            } else {
                PackageConfiguration packageConfiguration = checkRepositoryConnectionRequest.getPackageConfiguration();
                if (packageExistsInRepository(packageConfiguration)) {
                    statusMessagesResponse = StatusMessagesResponse.success(Arrays.asList("Package found."));
                } else {
                    statusMessagesResponse = StatusMessagesResponse.failure(Arrays.asList("Package not found."));
                }
            }
            response = DefaultGoPluginApiResponse.success(statusMessagesResponse.toJson());
        } catch (StorageException | InvalidJson e) {
            StatusMessagesResponse statusMessagesResponse =
                    StatusMessagesResponse.failure(Arrays.asList(e.getMessage()));
            response = DefaultGoPluginApiResponse.success(statusMessagesResponse.toJson());
        } catch (IncompleteJson e) {
            response = DefaultGoPluginApiResponse.incompleteRequest(e.getMessage());
        }
        return response;
    }

    private Boolean packageExistsInRepository(PackageConfiguration packageConfiguration) throws StorageException {
        final String prefix = packageConfiguration.getPipelineName() + "/" +
                packageConfiguration.getStageName() + "/" +
                packageConfiguration.getJobName() + "/";
        Optional<ListFileNamesResponse> response = storage.listFiles(null, prefix, "/");
        return response.orElse(new ListFileNamesResponse()).fileNames.size() > 0;
    }
}
