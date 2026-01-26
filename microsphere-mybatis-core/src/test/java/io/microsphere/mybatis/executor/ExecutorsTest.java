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

package io.microsphere.mybatis.executor;


import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.Executor;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;

import static io.microsphere.mybatis.executor.Executors.getDelegate;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static java.lang.reflect.Proxy.newProxyInstance;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link Executors} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Executors
 * @since 1.0.0
 */
class ExecutorsTest {

    @Test
    void testGetDelegate() {
        Executor delegate = mockExecutor();
        CachingExecutor cachingExecutor = new CachingExecutor(delegate);
        assertSame(delegate, getDelegate(cachingExecutor));
    }

    public static Executor mockExecutor() {
        return mockExecutor((proxy, method, args) -> null);
    }

    public static Executor mockExecutor(InvocationHandler handler) {
        return (Executor) newProxyInstance(getDefaultClassLoader(), ofArray(Executor.class), handler);
    }
}