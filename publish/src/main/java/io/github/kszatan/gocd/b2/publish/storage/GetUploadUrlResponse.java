/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * op-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.storage;

public class GetUploadUrlResponse {
    public String bucketId;
    public String uploadUrl;
    public String authorizationToken;
}
