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

package io.microsphere.mybatis.test.junit.jupiter.resolver;

import org.junit.jupiter.api.extension.ExtensionContext;

import static io.microsphere.mybatis.test.junit.jupiter.resolver.ComponentResolver.get;
import static io.microsphere.mybatis.test.junit.jupiter.resolver.ComponentResolver.store;
import static io.microsphere.reflect.JavaType.from;

/**
 * Abstract class of {@link ComponentResolver}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ComponentResolver
 * @since 1.0.0
 */
public abstract class AbstractComponentResolver<T> implements ComponentResolver<T> {

    private final Class<T> componentType;

    public AbstractComponentResolver() {
        this.componentType = from(getClass())
                .as(ComponentResolver.class)
                .getGenericType(0)
                .toClass();
    }

    @Override
    public final T resolve(ExtensionContext extensionContext, Class<?> requestedComponentType) throws Exception {
        T component = (T) get(extensionContext, requestedComponentType);
        if (component == null) {
            component = doResolve(extensionContext, requestedComponentType);
            if (supportsStaticField()) {
                store(extensionContext, component, true);
            }
            store(extensionContext, component, false);
        }
        return component;
    }

    @Override
    public Class<T> getComponentType() {
        return this.componentType;
    }

    protected abstract T doResolve(ExtensionContext extensionContext, Class<?> componentType) throws Exception;

}
