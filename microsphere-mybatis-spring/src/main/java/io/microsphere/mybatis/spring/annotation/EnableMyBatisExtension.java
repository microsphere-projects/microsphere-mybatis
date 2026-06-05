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

package io.microsphere.mybatis.spring.annotation;

import io.microsphere.mybatis.executor.ExecutorFilter;
import io.microsphere.mybatis.executor.ExecutorInterceptor;
import io.microsphere.mybatis.executor.InterceptingExecutor;
import io.microsphere.mybatis.plugin.InterceptingExecutorInterceptor;
import io.microsphere.spring.beans.BeanSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.plugin.Plugin;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static io.microsphere.spring.beans.BeanSource.BEAN_FACTORY;
import static io.microsphere.spring.beans.BeanSource.JAVA_SERVICE_PROVIDER;
import static io.microsphere.spring.beans.BeanSource.SPRING_FACTORIES;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Enables Spring's annotation-driven MyBatis extension, allowing for the customization of MyBatis {@link Executor}
 * behavior through {@link ExecutorFilter} and {@link ExecutorInterceptor} beans.
 * <p>
 * This annotation imports {@link MyBatisExtensionBeanDefinitionRegistrar} to register necessary beans.
 *
 * <h3>Example Usage</h3>
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableMyBatisExtension(interceptExecutor = true)
 * public class MyBatisConfig {
 *
 *     &#064;Bean
 *     public ExecutorInterceptor myCustomInterceptor() {
 *         return new MyCustomExecutorInterceptor();
 *     }
 * }
 * </pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Plugin
 * @see Executor
 * @see ExecutorFilter
 * @see ExecutorInterceptor
 * @see InterceptingExecutor
 * @see InterceptingExecutorInterceptor
 * @since 1.0.0
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
@Inherited
@Import(MyBatisExtensionBeanDefinitionRegistrar.class)
public @interface EnableMyBatisExtension {

    /**
     * Indicate whether the methods of MyBatis {@link Executor} should be intercepted.
     * If <code>true</code>, {@link ExecutorFilter} and {@link ExecutorInterceptor} beans will be searched in the
     * specified {@link #sources() scopes}, and then be applied to the MyBatis {@link InterceptingExecutor}.
     *
     * @see Plugin
     * @see Executor
     * @see ExecutorFilter
     * @see ExecutorInterceptor
     * @see InterceptingExecutor
     * @see InterceptingExecutorInterceptor
     */
    boolean interceptExecutor() default true;

    /**
     * The sources to search the {@link ExecutorFilter} and {@link ExecutorInterceptor} beans.
     *
     * @return The default value is {@code {BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER}},
     * it indicates to search in Spring Bean Factory,  "META-INF/spring.factories" files and "META-INF/services" files.
     */
    BeanSource[] sources() default {BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER};
}