/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers.bodies;

public class PackageConfiguration {
    private Entry pipelineName;
    private Entry stageName;
    private Entry jobName;

    public PackageConfiguration() {
        pipelineName = new Entry();
        stageName = new Entry();
        jobName = new Entry();
    }

    public String getPipelineName() {
        return pipelineName.value;
    }

    public String getStageName() {
        return stageName.value;
    }

    public String getJobName() {
        return jobName.value;
    }
}
