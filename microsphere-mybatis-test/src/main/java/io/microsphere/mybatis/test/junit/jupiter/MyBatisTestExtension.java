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

package io.microsphere.mybatis.test.junit.jupiter;

import io.microsphere.annotation.Nullable;
import io.microsphere.mybatis.test.junit.jupiter.resolver.ComponentResolver;
import io.microsphere.mybatis.test.junit.jupiter.resolver.ConfigurationResolver;
import io.microsphere.reflect.MemberUtils;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.mybatis.test.junit.jupiter.resolver.ComponentResolver.get;
import static io.microsphere.reflect.FieldUtils.findAllDeclaredFields;
import static io.microsphere.reflect.FieldUtils.setFieldValue;
import static io.microsphere.util.ServiceLoaderUtils.loadServicesList;
import static java.util.Objects.nonNull;

/**
 * The JUnit Jupiter Test {@link Extension} of MyBatis
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MyBatisTest
 * @see MyBatisRuntime
 * @since 1.0.0
 */
public class MyBatisTestExtension implements BeforeAllCallback, AfterAllCallback, AfterEachCallback,
        TestInstancePostProcessor, ParameterResolver {

    private static final List<ComponentResolver> componentResolvers = loadServicesList(ComponentResolver.class);

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        ConfigurationResolver.INSTANCE.resolve(context);
        injectFields(context, null);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        close(context, true);
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        injectFields(context, testInstance);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        close(context, false);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return nonNull(getComponentResolver(parameterContext, extensionContext));
    }

    @Override
    public @Nullable Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        ComponentResolver componentResolver = getComponentResolver(parameterContext, extensionContext);
        return resolveComponent(componentResolver, extensionContext, parameterContext.getParameter().getType());
    }

    private void injectFields(ExtensionContext extensionContext, @Nullable Object testInstance) {
        Class<?> testClass = extensionContext.getRequiredTestClass();

        boolean isStatic = testInstance == null;

        Predicate<Field> predicate = isStatic ? MemberUtils::isStatic : MemberUtils::isNonStatic;

        Set<Field> allFields = findAllDeclaredFields(testClass, predicate);
        for (Field field : allFields) {
            ComponentResolver resolver = getComponentResolver(extensionContext, field);
            Object component = resolveComponent(resolver, extensionContext, field.getType());
            if (component != null) {
                setFieldValue(testInstance, field, component);
            }
        }
    }

    static ComponentResolver getComponentResolver(ParameterContext parameterContext, ExtensionContext extensionContext) {
        for (ComponentResolver componentResolver : componentResolvers) {
            if (componentResolver.supportsParameter(parameterContext, extensionContext)) {
                return componentResolver;
            }
        }
        return null;
    }

    static ComponentResolver getComponentResolver(ExtensionContext extensionContext, Field field) {
        for (ComponentResolver componentResolver : componentResolvers) {
            if (componentResolver.supportsField(extensionContext, field)) {
                return componentResolver;
            }
        }
        return null;
    }

    static <T> T resolveComponent(ComponentResolver resolver, ExtensionContext context, Class<?> requestedComponentType) {
        return resolver == null ? null : execute(() -> (T) resolver.resolve(context, requestedComponentType));
    }

    static void close(ExtensionContext context, boolean forAll) throws Exception {
        for (ComponentResolver componentResolver : componentResolvers) {
            Class componentType = componentResolver.getComponentType();
            Object component = null;
            if (forAll == componentResolver.supportsStaticField()) {
                component = get(context, componentType, forAll, true);
            }
            if (component instanceof AutoCloseable closeable) {
                closeable.close();
            }
        }
    }
}
