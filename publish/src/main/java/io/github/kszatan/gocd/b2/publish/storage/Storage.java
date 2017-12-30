/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.storage;

public interface Storage {
    String getLastErrorMessage();

    Boolean authorize(String accountId, String applicationKey);

    void upload(String filePath, String destination) throws StorageException;

    void download(String filename);
}
