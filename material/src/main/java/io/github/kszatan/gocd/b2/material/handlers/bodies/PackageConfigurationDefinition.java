/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers.bodies;

import com.google.gson.annotations.SerializedName;

public class PackageConfigurationDefinition {
    public class Field {
        @SerializedName("display-name")
        public String displayName;
        @SerializedName("display-order")
        public String displayOrder;
        @SerializedName("default-value")
        public String defaultValue;
        public Boolean required;
        public Boolean secure;
        public Boolean partOfIdentity;
    }

    public Field pipelineName;
    public Field stageName;
    public Field jobName;

    public PackageConfigurationDefinition() {
        pipelineName = new Field();
        pipelineName.displayName = "Pipeline Name";
        pipelineName.displayOrder = "0";
        pipelineName.defaultValue = "";
        pipelineName.required = true;
        pipelineName.secure = false;
        pipelineName.partOfIdentity = true;

        stageName = new Field();
        stageName.displayName = "Stage Name";
        stageName.displayOrder = "1";
        stageName.defaultValue = "";
        stageName.required = true;
        stageName.secure = false;
        stageName.partOfIdentity = true;

        jobName = new Field();
        jobName.displayName = "Job Name";
        jobName.displayOrder = "2";
        jobName.defaultValue = "";
        jobName.required = true;
        jobName.secure = false;
        jobName.partOfIdentity = true;
    }
}
