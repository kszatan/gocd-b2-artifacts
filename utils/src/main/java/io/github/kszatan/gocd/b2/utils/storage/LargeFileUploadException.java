/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage;

public class LargeFileUploadException extends Exception {
    private String fileId;

    public LargeFileUploadException(String s, String fileId) {
        super(s);
        this.fileId = fileId;
    }
    public LargeFileUploadException(String fileId, Throwable cause) {
        super (cause);
        this.fileId = fileId;
    }

    public LargeFileUploadException(String message, String fileId, Throwable cause) {
        super (message, cause);
        this.fileId = fileId;
    }

    public String getFileId() {
        return fileId;
    }
}
