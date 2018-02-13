/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.storage;

import java.nio.file.Path;

public interface Storage {
    String getLastErrorMessage();
    
    public void addProgressObserver(ProgressObserver observer);

    Boolean authorize(String accountId, String applicationKey) throws StorageException;

    void upload(Path workDir, String relativeFilePath, String destination) throws StorageException;

    void download(String filename);
}
