/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.github.kszatan.gocd.b2.material.handlers.bodies.*;
import io.github.kszatan.gocd.b2.utils.json.GsonService;
import io.github.kszatan.gocd.b2.utils.json.IncompleteJson;
import io.github.kszatan.gocd.b2.utils.json.InvalidJson;
import io.github.kszatan.gocd.b2.utils.storage.*;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class LatestRevisionRequestHandler implements RequestHandler {
    private Storage storage;

    public LatestRevisionRequestHandler() throws IOException {
        storage = new BackblazeStorage();
    }

    public LatestRevisionRequestHandler(Storage storage) {
        this.storage = storage;
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) {
        GoPluginApiResponse response;
        try {
            LatestRevisionRequest latestRevisionRequest =
                    new LatestRevisionRequest(request.requestBody());
            RepositoryConfiguration repositoryConfiguration = latestRevisionRequest.getRepositoryConfiguration();
            storage.setBucketName(repositoryConfiguration.getBucketName());
            Optional<LatestRevisionResponse> maybeLatestRevision;
            if (!storage.authorize(repositoryConfiguration.getAccountId(), repositoryConfiguration.getApplicationKey())) {
                throw new StorageException(storage.getLastErrorMessage());
            } else {
                PackageConfiguration packageConfiguration = latestRevisionRequest.getPackageConfiguration();
                maybeLatestRevision = fetchLatestPackage(packageConfiguration);
            }
            if (maybeLatestRevision.isPresent()) {
                response = DefaultGoPluginApiResponse.success(GsonService.toJson(maybeLatestRevision.get()));
            } else {
                response = DefaultGoPluginApiResponse.success("{}");
            }
        } catch (StorageException | InvalidJson e) {
            response = DefaultGoPluginApiResponse.error(e.getMessage());
        } catch (IncompleteJson e) {
            response = DefaultGoPluginApiResponse.incompleteRequest(e.getMessage());
        }
        return response;
    }

    private Optional<LatestRevisionResponse> fetchLatestPackage(PackageConfiguration packageConfiguration) throws StorageException {
        final String prefix = packageConfiguration.getPipelineName() + "/" +
                packageConfiguration.getStageName() + "/" +
                packageConfiguration.getJobName() + "/";
        Optional<ListFileNamesResponse> maybeResponse = storage.listFiles(null, prefix, "/");
        Optional<LatestRevisionResponse> maybeLatestRevision = Optional.empty();
        if (maybeResponse.isPresent()) {
            ListFileNamesResponse response = maybeResponse.get();
            Optional<FileName> maybeFirstPackage = response.fileNames.stream()
                    .sorted((o1, o2) -> o2.fileName.compareTo(o1.fileName))
                    .findFirst();
            if (maybeFirstPackage.isPresent()) {
                FileName firstPackage = maybeFirstPackage.get();
                LatestRevisionResponse latestRevision = new LatestRevisionResponse();
                Path lastRevisionPath = Paths.get(firstPackage.fileName);
                latestRevision.revision = lastRevisionPath.getName(lastRevisionPath.getNameCount() - 1).toString();
                maybeLatestRevision = Optional.of(latestRevision);
            }
        }
        return maybeLatestRevision;
    }
}
