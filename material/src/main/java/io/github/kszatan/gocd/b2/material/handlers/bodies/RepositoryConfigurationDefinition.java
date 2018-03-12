/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers.bodies;

import com.google.gson.annotations.SerializedName;

public class RepositoryConfigurationDefinition {
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

    public Field url;
    public Field bucketName;
    public Field accountId;
    public Field applicationKey;

    public RepositoryConfigurationDefinition() {
        url = new Field();
        url.displayName = "Repository URL";
        url.displayOrder = "0";
        url.defaultValue = "";
        url.required = true;
        url.secure = true;
        url.partOfIdentity = true;

        bucketName = new Field();
        bucketName.displayName = "Bucket Name";
        bucketName.displayOrder = "1";
        bucketName.defaultValue = "";
        bucketName.required = true;
        bucketName.secure = false;
        bucketName.partOfIdentity = true;

        accountId = new Field();
        accountId.displayName = "Account ID";
        accountId.defaultValue = "";
        accountId.displayOrder = "2";
        accountId.required = true;
        accountId.secure = true;
        accountId.partOfIdentity = false;

        applicationKey = new Field();
        applicationKey.displayName = "Application Key";
        applicationKey.defaultValue = "";
        applicationKey.displayOrder = "3";
        applicationKey.required = true;
        applicationKey.secure = true;
        applicationKey.partOfIdentity = false;
    }
}
