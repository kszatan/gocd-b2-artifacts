/*
 * Copyright (c) 2018 Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.material.handlers.bodies;

import io.github.kszatan.gocd.b2.utils.json.GsonService;
import io.github.kszatan.gocd.b2.utils.json.IncompleteJson;
import io.github.kszatan.gocd.b2.utils.json.InvalidJson;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Collection;

public class LatestRevisionSinceRequest {
    private final PackageConfiguration packageConfiguration;
    private final RepositoryConfiguration repositoryConfiguration;
    @SerializedName("previous-revision")
    private final Revision previousRevision;

    public LatestRevisionSinceRequest(final String json) throws InvalidJson, IncompleteJson {
        ValidatePackageConfigurationRequest validator = new ValidatePackageConfigurationRequest(json);
        repositoryConfiguration = validator.getRepositoryConfiguration();
        packageConfiguration = validator.getPackageConfiguration();
        Collection<String> missing = GsonService.validate(json,
                Arrays.asList("previous-revision"));
        if (!missing.isEmpty()) {
            throw new IncompleteJson("Missing fields: " + missing.toString());
        }
        String configurationJson = GsonService.getField(json, "previous-revision");
        missing.addAll(GsonService.validate(configurationJson,
                Arrays.asList("revision", "timestamp")));
        if (!missing.isEmpty()) {
            throw new IncompleteJson("Missing fields: " + missing.toString());
        }
        previousRevision = GsonService.fromJson(configurationJson, Revision.class);
    }

    public RepositoryConfiguration getRepositoryConfiguration() {
        return repositoryConfiguration;
    }

    public PackageConfiguration getPackageConfiguration() {
        return packageConfiguration;
    }

    public Revision getPreviousRevision() {
        return previousRevision;
    }
}
