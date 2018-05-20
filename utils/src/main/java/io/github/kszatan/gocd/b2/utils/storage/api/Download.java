/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage.api;

import io.github.kszatan.gocd.b2.utils.storage.AuthorizeResponse;
import io.github.kszatan.gocd.b2.utils.storage.DownloadFileResponse;
import io.github.kszatan.gocd.b2.utils.storage.ErrorResponse;
import io.github.kszatan.gocd.b2.utils.storage.StorageException;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class Download extends B2ApiCall {
    private final String bucketName;
    private final String backblazeFileName;
    private final Path destination;
    private final AuthorizeResponse authorizeResponse;
    private DownloadFileResponse downloadFileResponse;
    private MkdirsProvider mkdirsProvider;

    // for test
    @FunctionalInterface
    public interface MkdirsProvider {
        void mkdirs(Path path) throws IOException;
    }

    public Download(BackblazeApiWrapper backblazeApiWrapper, String bucketName, String backblazeFileName,
                    Path destination, AuthorizeResponse authorizeResponse) {
        super("download " + backblazeFileName, backblazeApiWrapper);
        this.bucketName = bucketName;
        this.backblazeFileName = backblazeFileName;
        this.destination = destination;
        this.authorizeResponse = authorizeResponse;
        this.mkdirsProvider = path -> {
            Files.createDirectories(path);
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
            final Path absoluteDestFilePath = destination.resolve(backblazeFileName).getParent();
            mkdirsProvider.mkdirs(absoluteDestFilePath);
            downloadFileResponse = backblazeApiWrapper.downloadFileByName(
                    bucketName, backblazeFileName, destination, authorizeResponse).orElse(null);
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
