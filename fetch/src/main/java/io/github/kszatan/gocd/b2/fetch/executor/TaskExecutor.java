/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.fetch.executor;

import io.github.kszatan.gocd.b2.fetch.handlers.bodies.ExecuteResponse;
import io.github.kszatan.gocd.b2.fetch.handlers.bodies.TaskConfiguration;
import io.github.kszatan.gocd.b2.fetch.handlers.bodies.TaskContext;

public interface TaskExecutor {
    ExecuteResponse execute(TaskConfiguration configuration, TaskContext context);
}
