/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers.bodies;

import java.util.Date;

public class LatestRevisionSinceResponse {
    public String revision;
    public Date timestamp;
    public String user;
    public String revisionComment;
    public String trackbackUrl;
    public RevisionData data;
}
