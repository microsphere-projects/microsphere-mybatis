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

import io.microsphere.annotation.Nonnull;
import io.microsphere.mybatis.test.junit.jupiter.MyBatisRuntime;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Objects;

import static io.microsphere.reflect.MemberUtils.isStatic;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.create;
import static org.junit.jupiter.api.extension.ExtensionContext.StoreScope.EXTENSION_CONTEXT;

/**
 * The interface to resolve the MyBatis component
 *
 * @param <T> the type of MyBatis component
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ParameterContext
 * @since 1.0.0
 */
public interface ComponentResolver<T> {

    /**
     * Determine if this resolver supports resolution of an argument for the
     * {@link Parameter} in the supplied {@link ParameterContext} for the supplied
     * {@link ExtensionContext}.
     *
     * @param parameterContext the context for the parameter for which an argument should
     *                         be resolved; never {@code null}
     * @param extensionContext the extension context for the {@code Executable}
     *                         about to be invoked; never {@code null}
     * @return {@code true} if this resolver can resolve an argument for the parameter
     * @see ParameterContext
     */
    default boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Parameter parameter = parameterContext.getParameter();
        Class<?> parameterType = parameter.getType();
        return isMyBatisRuntime(parameter) && isComponentType(extensionContext, parameterType);
    }

    /**
     * Determine if this resolver supports resolution of the field
     *
     * @param extensionContext {@link ExtensionContext}
     * @param field            {@link Field}
     * @return {@code true} if this resolver supports the field , otherwise {@code false}
     */
    default boolean supportsField(ExtensionContext extensionContext, Field field) {
        if (isMyBatisRuntime(field) && isComponentType(extensionContext, field.getType())) {
            if (isStatic(field)) {
                return supportsStaticField();
            }
            return true;
        }
        return false;
    }

    /**
     * Determine if this resolver supports resolution of the static field
     *
     * @return {@code true} if this resolver supports the static field , otherwise {@code false}
     */
    default boolean supportsStaticField() {
        return true;
    }

    /**
     * Get the MyBatis component type
     *
     * @return non-null
     */
    @Nonnull
    Class<T> getComponentType();

    /**
     * Determine if the requested component type is the same as the MyBatis component type
     *
     * @param extensionContext       {@link ExtensionContext}
     * @param requestedComponentType requested MyBatis component type
     * @return {@code true} if the requested component type is the MyBatis component type , otherwise {@code false}
     */
    default boolean isComponentType(ExtensionContext extensionContext, Class<?> requestedComponentType) {
        return Objects.equals(getComponentType(), requestedComponentType);
    }

    /**
     * Resolve the MyBatis component
     *
     * @param extensionContext {@link ExtensionContext}
     * @return the MyBatis component
     * @throws Exception
     */
    default T resolve(ExtensionContext extensionContext) throws Exception {
        return resolve(extensionContext, getComponentType());
    }

    /**
     * Resolve the MyBatis component by component type
     *
     * @param extensionContext       {@link ExtensionContext}
     * @param requestedComponentType the requested component type
     * @return the MyBatis component
     * @throws Exception
     */
    T resolve(ExtensionContext extensionContext, Class<?> requestedComponentType) throws Exception;

    /**
     * Determine if the {@link AnnotatedElement} is annotated with {@link MyBatisRuntime}
     *
     * @param annotatedElement {@link AnnotatedElement}
     * @return {@code true} if the {@link AnnotatedElement} is annotated with {@link MyBatisRuntime} , otherwise {@code false}
     */
    static boolean isMyBatisRuntime(AnnotatedElement annotatedElement) {
        return annotatedElement.isAnnotationPresent(MyBatisRuntime.class);
    }

    /**
     * Store the MyBatis component
     *
     * @param context   {@link ExtensionContext}
     * @param component the MyBatis component
     * @param forAll    {@code true} if the component is for all test methods , otherwise {@code false}
     */
    static void store(ExtensionContext context, Object component, boolean forAll) {
        Store store = getStore(context, forAll);
        store.put(component.getClass(), component);
    }

    /**
     * Get the MyBatis component by component type
     *
     * @param context       {@link ExtensionContext}
     * @param componentType the MyBatis component type
     * @param <T>           the type of MyBatis component
     * @return the MyBatis component , if not found , return {@code null}
     */
    static <T> T get(ExtensionContext context, Class<T> componentType) {
        T component = get(context, componentType, false, false);
        if (component == null) {
            component = get(context, componentType, true, false);
        }
        return component;
    }

    /**
     * Get the MyBatis component by component type
     *
     * @param context       {@link ExtensionContext}
     * @param componentType the MyBatis component type
     * @param forAll        {@code true} if the component is for all test methods , otherwise {@code false}
     * @param forRemoval    {@code true} if the component is for removal , otherwise {@code false}
     * @param <T>           the type of MyBatis component
     * @return the MyBatis component , if not found , return {@code null}
     */
    static <T> T get(ExtensionContext context, Class<T> componentType, boolean forAll, boolean forRemoval) {
        Store store = getStore(context, forAll);
        return forRemoval ? store.remove(componentType, componentType) : store.get(componentType, componentType);
    }

    /**
     * Get the {@link Store} by {@link ExtensionContext} and forAll flag
     *
     * @param context {@link ExtensionContext}
     * @param forAll  {@code true} if the component is for all test methods , otherwise {@code false}
     * @return non-null
     */
    static Store getStore(ExtensionContext context, boolean forAll) {
        Class<?> testClass = context.getRequiredTestClass();
        Namespace namespace = forAll ? create("FOR_ALL", testClass) : create("FOR_EACH", testClass);
        return context.getStore(EXTENSION_CONTEXT, namespace);
    }
}
