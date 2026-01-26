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

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.PluginException;
import org.apache.ibatis.plugin.Signature;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.microsphere.collection.ListUtils.ofList;
import static io.microsphere.collection.MapUtils.newFixedHashMap;
import static io.microsphere.text.FormatUtils.format;
import static java.lang.reflect.Proxy.getInvocationHandler;
import static org.apache.ibatis.util.MapUtil.computeIfAbsent;

/**
 * The utilities class of {@link Plugin}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Plugin
 * @see Intercepts
 * @see Signature
 * @since 1.0.0
 */
public abstract class Plugins {

    /**
     * The valid target classes for {@link Interceptor}
     */
    public static final List<Class<?>> TARGET_CLASSES = ofList(Executor.class, ParameterHandler.class,
            ResultSetHandler.class, StatementHandler.class);

    /**
     * The size of {@link #TARGET_CLASSES}
     */
    public static final int TARGET_CLASSES_SIZE = TARGET_CLASSES.size();

    /**
     * Get the {@link Signature} map from the specified {@link Interceptor} that was annotated {@link Intercepts}
     *
     * @param interceptor the {@link Interceptor} that was annotated {@link Intercepts}
     * @return
     * @throws PluginException if No {@link Intercepts @Intercepts} annotation was found in interceptor or
     *                         no {@link Signature @Signature} was found in the {@link Intercepts @Intercepts}
     * @see Plugin#getSignatureMap(Interceptor)
     */
    public static Map<Class<?>, Set<Method>> getSignatureMap(Interceptor interceptor) throws PluginException {
        return getSignatureMap(interceptor.getClass());
    }

    /**
     * Get the {@link Signature} map from the specified {@link Interceptor} that was annotated {@link Intercepts}
     *
     * @param interceptorClass the {@link Interceptor} that was annotated {@link Intercepts}
     * @return
     * @throws PluginException if No {@link Intercepts @Intercepts} annotation was found in interceptorClass or
     *                         no {@link Signature @Signature} was found in the {@link Intercepts @Intercepts}
     * @see Plugin#getSignatureMap(Interceptor)
     */
    public static Map<Class<?>, Set<Method>> getSignatureMap(Class<? extends Interceptor> interceptorClass) throws PluginException {
        Intercepts interceptsAnnotation = interceptorClass.getAnnotation(Intercepts.class);
        if (interceptsAnnotation == null) {
            throw newPluginException("No @Intercepts annotation was found in interceptorClass : {}", interceptorClass.getName());
        }
        Signature[] sigs = interceptsAnnotation.value();
        // @Intercepts usually specifies one or two targets, so HashMap can be optimized
        Map<Class<?>, Set<Method>> signatureMap = newFixedHashMap(TARGET_CLASSES_SIZE / 2);
        for (Signature sig : sigs) {
            Class<?> targetType = sig.type();
            if (!TARGET_CLASSES.contains(targetType)) {
                throw newPluginException("The @Intercepts#type() =  {} must be one of the target classes : {}", targetType.getName(), TARGET_CLASSES);
            }
            Set<Method> methods = computeIfAbsent(signatureMap, targetType, k -> new HashSet<>());
            String methodName = sig.method();
            try {
                Method method = targetType.getMethod(methodName, sig.args());
                methods.add(method);
            } catch (NoSuchMethodException e) {
                throw new PluginException(format("Could not find method on {} named {}", targetType, methodName), e);
            }
        }
        return signatureMap;
    }

    /**
     * Get the {@link Plugin} from the specified proxy
     *
     * @param proxy the specified proxy
     * @return <code>null</code> if the proxy is not a {@link Plugin}
     */
    public static Plugin getPlugin(Object proxy) {
        if (proxy instanceof Proxy) {
            InvocationHandler invocationHandler = getInvocationHandler(proxy);
            if (invocationHandler instanceof Plugin) {
                return (Plugin) invocationHandler;
            }
        }
        return null;
    }

    protected static PluginException newPluginException(String messagePattern, Object... args) {
        return new PluginException(format(messagePattern, args));
    }

    private Plugins() {
    }
}
