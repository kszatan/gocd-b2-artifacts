/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage.api;

import io.github.kszatan.gocd.b2.utils.storage.AuthorizeResponse;
import io.github.kszatan.gocd.b2.utils.storage.ErrorResponse;
import io.github.kszatan.gocd.b2.utils.storage.ListFileNamesResponse;
import io.github.kszatan.gocd.b2.utils.storage.StorageException;

import java.io.IOException;
import java.util.Optional;

public class ListFileNames extends B2ApiCall {
    private final AuthorizeResponse authorizeResponse;
    private ListFileNamesResponse listFileNamesResponse;
    private String bucketId;
    private String startFileName;
    private String prefix;
    private String delimiter;

    public ListFileNames(BackblazeApiWrapper backblazeApiWrapper, AuthorizeResponse authorizeResponse, String bucketId) {
        this(backblazeApiWrapper, authorizeResponse, bucketId, "", "", "");
    }

    public ListFileNames(BackblazeApiWrapper backblazeApiWrapper, AuthorizeResponse authorizeResponse, String bucketId,
                         String startFileName, String prefix, String delimiter) {
        super("list file names", backblazeApiWrapper);
        this.authorizeResponse = authorizeResponse;
        this.bucketId = bucketId;
        this.startFileName = startFileName;
        this.prefix = prefix;
        this.delimiter = delimiter;
    }

    public Optional<ListFileNamesResponse> getResponse() {
        return Optional.of(listFileNamesResponse);
    }

    @Override
    public Boolean call() throws StorageException {
        try {
            listFileNamesResponse = backblazeApiWrapper.listFileNames(
                    authorizeResponse, bucketId, startFileName, prefix, delimiter).orElse(null);
        } catch (IOException e) {
            throw new StorageException(e);
        }
        return listFileNamesResponse != null;
    }

    @Override
    public void handleErrors(ErrorResponse error) throws StorageException {
        super.handleErrors(error);
    }

}
