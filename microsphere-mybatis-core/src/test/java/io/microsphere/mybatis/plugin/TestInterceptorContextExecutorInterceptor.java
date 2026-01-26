/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.mybatis.plugin;

import io.microsphere.annotation.Nullable;
import io.microsphere.mybatis.executor.ExecutorInterceptor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;

import java.sql.SQLException;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ExecutorInterceptor} class to test {@link InterceptorContext}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ExecutorInterceptor
 * @since 1.0.0
 */
public class TestInterceptorContextExecutorInterceptor implements ExecutorInterceptor {

    private static final String ATTRIBUTE_NAME = "test.name";

    @Override
    public void beforeUpdate(InterceptorContext<Executor> context, MappedStatement ms, Object parameter) {
        assertSame(emptyMap(), context.getAttributes());
        assertNotNull(context.getProperties());
        context.setAttribute(ATTRIBUTE_NAME, context.getTarget());
        context.setStartTime(System.currentTimeMillis());
    }

    @Override
    public void afterUpdate(InterceptorContext<Executor> context, MappedStatement ms, Object parameter, @Nullable Integer result, @Nullable SQLException failure) {
        if (context.hasAttribute(ATTRIBUTE_NAME)) {
            assertNotSame(context.getAttributes(), context.getOrCreateAttributes());
            assertEquals(context.getAttributes(), context.getOrCreateAttributes());
            Object value = context.getAttribute(ATTRIBUTE_NAME);
            Object defaultValue = context.getAttribute("ABSENT_NAME", value);
            assertEquals(value, defaultValue);
            assertEquals(value, context.getTarget());
            assertEquals(value, context.removeAttribute(ATTRIBUTE_NAME));
            assertSame(context, context.removeAttributes());
        }
        assertTrue(System.currentTimeMillis() >= context.getStartTime());
    }
}
