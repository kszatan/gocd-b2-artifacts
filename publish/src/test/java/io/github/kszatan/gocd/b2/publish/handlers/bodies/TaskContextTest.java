/*
 * Copyright (c) 2017. Krzysztof Szatan <kszatan@gmail.com>
 * This file is subject to the license terms in the LICENSE file found in the
 * top-level directory of this distribution.
 */

package io.github.kszatan.gocd.b2.publish.handlers.bodies;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class TaskContextTest {
    @Test
    public void getAccountId() throws Exception {
        TaskContext context = new TaskContext();
        String accountId = "my_account_id";
        context.environmentVariables.put("B2_ACCOUNT_ID", accountId);
        assertThat(context.getAccountId(), equalTo(accountId));
    }

    @Test
    public void getApplicationId() throws Exception {
        TaskContext context = new TaskContext();
        String applicationKey = "my_application_key";
        context.environmentVariables.put("B2_APPLICATION_KEY", applicationKey);
        assertThat(context.getApplicationKey(), equalTo(applicationKey));
    }

}