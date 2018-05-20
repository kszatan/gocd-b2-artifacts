/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage;

import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;

public interface Storage {
    void setBucketName(String bucketName);
    void setCredentials(String accountId, String applicationKey);
    String getLastErrorMessage();
    void addProgressObserver(ProgressObserver observer);
    Boolean checkConnection(String accountId, String applicationKey) throws StorageException;
    Boolean authorize() throws StorageException;
    Optional<ListFileNamesResponse> listFiles(String startFileName, String prefix, String delimiter) throws StorageException;
    void upload(Path workDir, Path relativeFilePath, String destination) throws StorageException, GeneralSecurityException;
    Boolean download(String backblazeFileName, Path destination, String fileNamePrefix) throws StorageException;
}
