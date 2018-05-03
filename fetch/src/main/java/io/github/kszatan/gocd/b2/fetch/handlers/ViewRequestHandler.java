/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.fetch.handlers;

import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import io.github.kszatan.gocd.b2.fetch.handlers.bodies.TaskConfigurationView;
import io.github.kszatan.gocd.b2.utils.json.GsonService;
import org.apache.commons.io.IOUtils;

public class ViewRequestHandler implements RequestHandler {
    private static final String TASK_NAME = "Fetch from B2";
    private static final String TEMPLATE_PATH = "/views/task-config.template.html";

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) {
        GoPluginApiResponse response;
        try {
            String template = IOUtils.toString(getClass().getResourceAsStream(TEMPLATE_PATH), "UTF-8");
            TaskConfigurationView view = new TaskConfigurationView(TASK_NAME, template);
            response = DefaultGoPluginApiResponse.success(GsonService.toJson(view));
        } catch (java.io.IOException e) {
            response = DefaultGoPluginApiResponse.error("Failed to find template: " + e.getMessage());
        }
        return response;
    }
}
