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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;

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
            storage.setCredentials(repositoryConfiguration.getAccountId(), repositoryConfiguration.getApplicationKey());
            storage.setBucketName(repositoryConfiguration.getBucketName());
            Optional<LatestRevisionResponse> maybeLatestRevision;
            if (!storage.authorize()) {
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
                    .sorted((f1, f2) -> fileNameRevisionComparator(f2, f1))
                    .findFirst();
            if (maybeFirstPackage.isPresent()) {
                FileName firstPackage = maybeFirstPackage.get();
                LatestRevisionResponse latestRevision = new LatestRevisionResponse();
                Path lastRevisionPath = Paths.get(firstPackage.fileName);
                latestRevision.revision = lastRevisionPath.getName(lastRevisionPath.getNameCount() - 1).toString();
                latestRevision.timestamp = getUploadedTime(prefix + latestRevision.revision);
                latestRevision.data = new RevisionData(packageConfiguration.getPipelineName(),
                        packageConfiguration.getStageName(),
                        packageConfiguration.getJobName(),
                        latestRevision.revision);
                maybeLatestRevision = Optional.of(latestRevision);
            }
        }
        return maybeLatestRevision;
    }

    private Date getUploadedTime(String path) throws StorageException {
        Date uploadedTime = new Date();
        Optional<ListFileNamesResponse> maybeResponse = storage.listFiles(null, path, "*");
        if (maybeResponse.isPresent()) {
            ListFileNamesResponse response = maybeResponse.get();
            Optional<FileName> maybeFileName = response.fileNames.stream()
                    .sorted((f1, f2) -> f2.uploadTimestamp.compareTo(f1.uploadTimestamp))
                    .findFirst();
            if (maybeFileName.isPresent()) {
                uploadedTime.setTime(maybeFileName.get().uploadTimestamp);
            }
        } else {
            uploadedTime.setTime(0);
        }
        return uploadedTime;
    }

    static private int fileNameRevisionComparator(FileName f1, FileName f2) {
        Path path = Paths.get(f1.fileName);
        String f1Revision = path.getName(path.getNameCount() - 1).toString();
        path = Paths.get(f2.fileName);
        String f2Revision = path.getName(path.getNameCount() - 1).toString();
        float result = Float.parseFloat(f1Revision) - Float.parseFloat(f2Revision);
        if (result > 0)
            return 1;
        else if (result < 0)
            return -1;
        return 0;
    }
}
