/*
 * Copyright (c) 2018. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.storage;

public class UploadFileResponse {
    public String fileId;
    public String fileName;
    public String accountId;
    public String bucketId;
    public Integer contentLength;
    public String contentSha1;
    public String contentType;
}
