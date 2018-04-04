/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.utils.storage;

import com.google.gson.annotations.SerializedName;

public class FileInfo {
    @SerializedName("src_last_modified_millis")
    public String srcLastModifiedMillis;
}
