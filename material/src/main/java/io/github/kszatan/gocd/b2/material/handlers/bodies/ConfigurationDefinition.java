/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers.bodies;

import com.google.gson.annotations.SerializedName;

public class ConfigurationDefinition {
    public class Field {
        @SerializedName("default-value")
        public String defaultValue;
        public Boolean required;
        public Boolean secure;
    }

    public Field bucketName;
    public Field accountId;
    public Field applicationKey;

    public ConfigurationDefinition() {
        accountId = new Field();
        accountId.defaultValue = "";
        accountId.required = true;
        accountId.secure = true;

        applicationKey = new Field();
        applicationKey.defaultValue = "";
        applicationKey.required = true;
        applicationKey.secure = true;

        bucketName = new Field();
        bucketName.defaultValue = "";
        bucketName.required = true;
        bucketName.secure = false;
    }
}
