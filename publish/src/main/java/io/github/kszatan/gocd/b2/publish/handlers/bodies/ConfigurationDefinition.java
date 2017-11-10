/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers.bodies;

import com.google.gson.annotations.SerializedName;

public class ConfigurationDefinition {
    public class Field {
        @SerializedName("default-value")
        public String defaultValue;

        public Boolean required;

        public Boolean secure;
    }

    public Field destinationPrefix;
    public Field bucketId;

    public ConfigurationDefinition() {
        destinationPrefix = new Field();
        destinationPrefix.defaultValue = "";
        destinationPrefix.required = false;
        destinationPrefix.secure = false;

        bucketId = new Field();
        bucketId.defaultValue = "";
        bucketId.required = false;
        bucketId.secure = false;
    }
}
