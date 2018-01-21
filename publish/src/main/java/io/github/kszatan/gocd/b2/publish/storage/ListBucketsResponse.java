/*
 * Copyright (c) 2018. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.storage;

import java.util.List;

public class ListBucketsResponse {
    public class Bucket {
        public String accountId;
        public String bucketId;
        public Object bucketInfo;
        public String bucketName;
        public String bucketType;
        public List<String> lifecycleRules;
    }

    public List<Bucket> buckets;
}
