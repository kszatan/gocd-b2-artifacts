/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.github.kszatan.gocd.b2.publish.handlers.bodies.TaskConfigurationView;
import io.github.kszatan.gocd.b2.utils.json.GsonService;
import org.apache.commons.io.IOUtils;

public class ViewRequestHandler implements RequestHandler {
    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) {
        GoPluginApiResponse response;
        try {
            String template = IOUtils.toString(getClass().getResourceAsStream("/views/task-config.template.html"), "UTF-8");
            TaskConfigurationView view = new TaskConfigurationView("Publish to B2", template);
            response = DefaultGoPluginApiResponse.success(GsonService.toJson(view));
        } catch (java.io.IOException e) {
            response = DefaultGoPluginApiResponse.error("Failed to find template: " + e.getMessage());
        }
        return response;
    }
}
