/*
 * Copyright (c) 2018. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Bucket {
    public String accountId;
    @SerializedName("bucketId")
    public String id;
    @SerializedName("bucketInfo")
    public Object info;
    @SerializedName("bucketName")
    public String name;
    @SerializedName("bucketType")
    public String type;
    public List<String> corsRules;
    public List<String> lifecycleRules;
    public Integer revision;
}
