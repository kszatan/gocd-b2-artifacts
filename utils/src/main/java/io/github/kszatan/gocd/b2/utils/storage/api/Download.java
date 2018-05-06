/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage.api;

import io.github.kszatan.gocd.b2.utils.storage.*;
import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class Download extends B2ApiCall {
    private final String bucketName;
    private final String fileName;
    private final Path destination;
    private final AuthorizeResponse authorizeResponse;
    private DownloadFileResponse downloadFileResponse;
    private MkdirsProvider mkdirsProvider;

    // for test
    @FunctionalInterface
    public interface MkdirsProvider {
        Boolean mkdirs(String path);
    }

    public Download(BackblazeApiWrapper backblazeApiWrapper, String bucketName, String fileName,
                    Path destination, AuthorizeResponse authorizeResponse) {
        super("download " + fileName, backblazeApiWrapper);
        this.bucketName = bucketName;
        this.fileName = fileName;
        this.destination = destination;
        this.authorizeResponse = authorizeResponse;
        this.mkdirsProvider = path -> {
            File file = new File(path);
            return file.mkdirs();
        };
    }

    public void setMkdirsProvider(MkdirsProvider mkdirsProvider) {
        this.mkdirsProvider = mkdirsProvider;
    }

    Optional<DownloadFileResponse> getResponse() {
        return Optional.ofNullable(downloadFileResponse);
    }

    @Override
    public Boolean call() throws StorageException {
        try {
            final String absoluteDestFilePath = destination.resolve(fileName).getParent().toString();
            if (!mkdirsProvider.mkdirs(absoluteDestFilePath)) {
                throw new IOException("Unable to create directories on path " + absoluteDestFilePath);
            }
            downloadFileResponse = backblazeApiWrapper.downloadFileByName(
                    bucketName, fileName, destination, authorizeResponse).orElse(null);
        } catch(IOException e) {
            throw new StorageException("Exception while downloading file: " + e.getMessage(), e);
        }
        return downloadFileResponse != null;
    }

    @Override
    public void handleErrors(ErrorResponse error) throws StorageException {
        if (error.status == HttpStatus.SC_NOT_FOUND) {
            throw new StorageException("Not found: " + error.message);
        }
        super.handleErrors(error);
    }
}
