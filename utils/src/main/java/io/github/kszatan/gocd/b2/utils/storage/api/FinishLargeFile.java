/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage.api;

import io.github.kszatan.gocd.b2.utils.storage.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class FinishLargeFile extends B2ApiCall {
    private final AuthorizeResponse authorizeResponse;
    private FinishLargeFileResponse response;
    private String fileId;
    private List<String> partSha1Array;

    public FinishLargeFile(BackblazeApiWrapper backblazeApiWrapper, AuthorizeResponse authorizeResponse, String fileId,
                           List<String> partSha1Array) {
        super("finish large file", backblazeApiWrapper);
        this.authorizeResponse = authorizeResponse;
        this.fileId = fileId;
        this.partSha1Array = partSha1Array;
    }

    public Optional<FinishLargeFileResponse> getResponse() {
        return Optional.of(response);
    }

    @Override
    public Boolean call() throws StorageException {
        try {
            response = backblazeApiWrapper.finishLargeFile(authorizeResponse, fileId, partSha1Array).orElse(null);
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
