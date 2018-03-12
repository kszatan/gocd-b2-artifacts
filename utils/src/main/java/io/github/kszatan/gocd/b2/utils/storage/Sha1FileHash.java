/*
 * Copyright (c) 2018. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class Sha1FileHash implements FileHash {
    @Override
    public String getHashValue(Path filePath) throws NoSuchAlgorithmException, IOException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-1");
        InputStream fis = Files.newInputStream(filePath);
        byte[] buffer = new byte[8192];
        for (int read = 0; (read = fis.read(buffer)) != -1; ) {
            digest.update(buffer, 0, read);
        }

        try (Formatter formatter = new Formatter()) {
            for (final byte b : digest.digest()) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        }
    }
}
