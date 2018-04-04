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
import io.github.kszatan.gocd.b2.utils.json.IncompleteJson;
import io.github.kszatan.gocd.b2.utils.json.InvalidJson;
import io.github.kszatan.gocd.b2.utils.storage.BackblazeStorage;
import io.github.kszatan.gocd.b2.utils.storage.Storage;
import io.github.kszatan.gocd.b2.utils.storage.StorageException;

import java.io.IOException;
import java.util.Arrays;

public class CheckRepositoryConnectionRequestHandler implements RequestHandler {
    private Storage storage;

    public CheckRepositoryConnectionRequestHandler() throws IOException {
        this.storage = new BackblazeStorage();
    }

    public CheckRepositoryConnectionRequestHandler(Storage storage) {
        this.storage = storage;
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) {
        GoPluginApiResponse response;
        try {
            CheckRepositoryConnectionRequest checkRepositoryConnectionRequest =
                    new CheckRepositoryConnectionRequest(request.requestBody());
            RepositoryConfiguration configuration = checkRepositoryConnectionRequest.getConfiguration();
            storage.setBucketName(configuration.getBucketName());
            StatusMessagesResponse statusMessagesResponse;
            if (storage.checkConnection(configuration.getAccountId(), configuration.getApplicationKey())) {
                statusMessagesResponse =
                        StatusMessagesResponse.success(Arrays.asList("Successfully connected to B2."));
            } else {
                statusMessagesResponse =
                        StatusMessagesResponse.failure(Arrays.asList(storage.getLastErrorMessage()));
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
}
