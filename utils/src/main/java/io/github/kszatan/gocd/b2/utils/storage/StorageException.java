/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage;

public class StorageException extends Exception {
    public StorageException(String s) {
        super(s);
    }
    public StorageException(Throwable cause) {
        super (cause);
    }

    public StorageException(String message, Throwable cause) {
        super (message, cause);
    }
}
