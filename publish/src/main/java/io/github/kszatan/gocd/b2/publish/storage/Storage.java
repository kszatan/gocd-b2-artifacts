/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.storage;

import java.nio.file.Path;
import java.security.GeneralSecurityException;

public interface Storage {
    String getLastErrorMessage();
    
    void addProgressObserver(ProgressObserver observer);

    Boolean authorize(String accountId, String applicationKey) throws StorageException;

    Boolean upload(Path workDir, String relativeFilePath, String destination) throws StorageException, GeneralSecurityException;

    void download(String filename);
}
