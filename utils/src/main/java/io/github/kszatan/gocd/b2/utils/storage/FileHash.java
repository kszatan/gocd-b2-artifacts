/*
 * Copyright (c) 2018. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

public interface FileHash {
    String getHashValue(Path filePath) throws IOException, NoSuchAlgorithmException;
    String getHashValue(byte[] buffer, int length) throws IOException, NoSuchAlgorithmException;
}
