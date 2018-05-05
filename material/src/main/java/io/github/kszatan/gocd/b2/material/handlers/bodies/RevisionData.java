/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers.bodies;

public class RevisionData {
    public RevisionData() {}

    public RevisionData(String pipelineName, String stageName, String jobName, String label) {
        this.pipelineName = pipelineName;
        this.stageName = stageName;
        this.jobName = jobName;
        this.label = label;
    }

    public String pipelineName;
    public String stageName;
    public String jobName;
    public String label;
}
