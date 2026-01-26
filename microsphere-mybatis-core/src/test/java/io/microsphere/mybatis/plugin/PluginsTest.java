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

import io.microsphere.mybatis.test.executor.LoggingExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.PluginException;
import org.apache.ibatis.plugin.Signature;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import static io.microsphere.mybatis.plugin.Plugins.TARGET_CLASSES;
import static io.microsphere.mybatis.plugin.Plugins.TARGET_CLASSES_SIZE;
import static io.microsphere.mybatis.plugin.Plugins.getPlugin;
import static io.microsphere.mybatis.plugin.Plugins.getSignatureMap;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static java.lang.reflect.Proxy.newProxyInstance;
import static org.apache.ibatis.plugin.Plugin.wrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link Plugins} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Plugins
 * @since 1.0.0
 */
class PluginsTest {

    @Test
    void testTargetClasses() {
        assertEquals(4, TARGET_CLASSES.size());
        assertEquals(TARGET_CLASSES_SIZE, TARGET_CLASSES.size());
        assertTrue(TARGET_CLASSES.contains(Executor.class));
        assertTrue(TARGET_CLASSES.contains(ParameterHandler.class));
        assertTrue(TARGET_CLASSES.contains(ResultSetHandler.class));
        assertTrue(TARGET_CLASSES.contains(StatementHandler.class));
    }

    @Test
    void testGetSignatureMap() {
        Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(new TestAnnotatedExecutorInterceptor());
        assertEquals(1, signatureMap.size());
        assertTrue(signatureMap.containsKey(Executor.class));
        assertFalse(signatureMap.containsKey(ParameterHandler.class));
        assertFalse(signatureMap.containsKey(ResultSetHandler.class));
        assertFalse(signatureMap.containsKey(StatementHandler.class));
        Set<Method> methods = signatureMap.get(Executor.class);
        assertEquals(3, methods.size());
    }

    @Test
    void testGetSignatureMapOnFailed() {
        assertThrows(PluginException.class, () -> getSignatureMap(new NoOpInterceptor()));
        assertThrows(PluginException.class, () -> getSignatureMap(new WrongTargetSignatureInterceptor()));
        assertThrows(PluginException.class, () -> getSignatureMap(new WrongMethodSignatureInterceptor()));
    }

    @Test
    void testGetPlugin() throws SQLException {
        Executor executor = newExecutor();
        Object proxy = wrap(executor, new TestAnnotatedExecutorInterceptor());
        assertTrue(proxy instanceof Executor);

        Plugin plugin = getPlugin(proxy);
        assertNotNull(plugin);

        Executor proxyExecutor = (Executor) proxy;
        assertEquals(0, proxyExecutor.update(null, null));

        // No Proxy
        proxy = wrap(executor, new SignatureInterceptor());
        assertSame(executor, proxy);
        plugin = getPlugin(proxy);
        assertNull(plugin);
        // JDK Proxy
        proxy = newProxyInstance(getDefaultClassLoader(), ofArray(Serializable.class), (proxy1, method, args) -> null);
        plugin = getPlugin(proxy);
        assertNull(plugin);
    }

    private Executor newExecutor() {
        return new LoggingExecutor();
    }

    @Intercepts(
            value = {}
    )
    static class SignatureInterceptor implements Interceptor {
        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            return invocation.proceed();
        }
    }


    @Intercepts(
            @Signature(
                    type = Object.class,
                    method = "equals",
                    args = {Object.class}
            )
    )
    static class WrongTargetSignatureInterceptor implements Interceptor {
        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            return invocation.proceed();
        }
    }

    @Intercepts(
            @Signature(
                    type = Executor.class,
                    method = "methodNotFound",
                    args = {}
            )
    )
    static class WrongMethodSignatureInterceptor implements Interceptor {
        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            return invocation.proceed();
        }
    }
}