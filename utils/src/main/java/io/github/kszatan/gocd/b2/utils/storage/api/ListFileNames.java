/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage.api;

import io.github.kszatan.gocd.b2.utils.storage.*;

import java.io.IOException;
import java.util.Optional;

public class ListFileNames extends B2ApiCall {
    private final AuthorizeResponse authorizeResponse;
    private ListFileNamesResponse response;
    private ListFileNamesParams params;

    public ListFileNames(BackblazeApiWrapper backblazeApiWrapper, AuthorizeResponse authorizeResponse, String bucketId) {
        this(backblazeApiWrapper, authorizeResponse, bucketId, "", "", "");
    }

    public ListFileNames(BackblazeApiWrapper backblazeApiWrapper, AuthorizeResponse authorizeResponse, String bucketId,
                         String startFileName, String prefix, String delimiter) {
        super("list file names", backblazeApiWrapper);
        this.authorizeResponse = authorizeResponse;
        this.params = new ListFileNamesParams();
        this.params.bucketId = bucketId;
        this.params.startFileName = startFileName;
        this.params.prefix = prefix;
        this.params.delimiter = delimiter;
        this.params.maxFileCount = 1000;
    }

    public Optional<ListFileNamesResponse> getResponse() {
        return Optional.of(response);
    }

    @Override
    public Boolean call() throws StorageException {
        try {
            response = backblazeApiWrapper.listFileNames(authorizeResponse, params).orElse(null);
        } catch (IOException e) {
            throw new StorageException(e);
        }
        return response != null;
    }

    @Override
    public void handleErrors(ErrorResponse error) throws StorageException {
        super.handleErrors(error);
    }

}
