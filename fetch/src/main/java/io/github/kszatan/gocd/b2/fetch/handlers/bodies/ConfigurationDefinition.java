/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.fetch.handlers.bodies;

import com.google.gson.annotations.SerializedName;

public class ConfigurationDefinition {
    public class Field {
        @SerializedName("default-value")
        public String defaultValue;

        public Boolean required;

        public Boolean secure;
    }

    public Field repositoryName;
    public Field packageName;
    public Field destination;

    public ConfigurationDefinition() {
        repositoryName = new Field();
        repositoryName.defaultValue = "";
        repositoryName.required = true;
        repositoryName.secure = false;

        packageName = new Field();
        packageName.defaultValue = "";
        packageName.required = true;
        packageName.secure = false;

        destination = new Field();
        destination.defaultValue = "";
        destination.required = false;
        destination.secure = false;
    }
}
