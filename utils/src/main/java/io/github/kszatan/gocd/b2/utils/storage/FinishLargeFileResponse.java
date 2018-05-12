/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage;

public class FinishLargeFileResponse {
    public String accountId;
    public String action;
    public String bucketId;                                                                         
    public Integer contentLength;
    public String contentSha1;
    public String contentType;
    public String fileId;
    public FileInfo fileInfo;
    public String fileName;
    public Long uploadTimestamp;
}
