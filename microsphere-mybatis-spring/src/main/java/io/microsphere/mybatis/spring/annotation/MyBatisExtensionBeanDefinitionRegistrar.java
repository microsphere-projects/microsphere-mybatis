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

import io.microsphere.logging.Logger;
import io.microsphere.mybatis.executor.ExecutorFilter;
import io.microsphere.mybatis.executor.ExecutorInterceptor;
import io.microsphere.mybatis.executor.InterceptingExecutor;
import io.microsphere.mybatis.plugin.InterceptingExecutorInterceptor;
import io.microsphere.spring.beans.BeanSource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.BeanSource.registerBeans;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.util.ArrayUtils.forEach;
import static io.microsphere.util.ArrayUtils.length;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * {@link ImportBeanDefinitionRegistrar} for {@link EnableMyBatisExtension @EnableMyBatisExtension}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableMyBatisExtension
 * @see ExecutorFilter
 * @see ExecutorInterceptor
 * @see InterceptingExecutor
 * @see InterceptingExecutorInterceptor
 * @since 1.0.0
 */
public class MyBatisExtensionBeanDefinitionRegistrar extends MyBatisImportBeanDefinitionRegistrar<EnableMyBatisExtension> {

    private static final Logger logger = getLogger(MyBatisExtensionBeanDefinitionRegistrar.class);

    /**
     * The Spring Bean name of {@link InterceptingExecutorInterceptor}
     */
    public static final String INTERCEPTING_EXECUTOR_INTERCEPTOR_BEAN_NAME = "interceptingExecutorInterceptor";

    /**
     * Registers the relevant {@link BeanDefinition}s for the intercepting executor components
     * if the {@link EnableMyBatisExtension#interceptExecutor()} attribute is enabled.
     * <p>
     * This includes registering {@link ExecutorFilter}s and {@link ExecutorInterceptor}s
     * from the specified sources, and conditionally registering the
     * {@link InterceptingExecutorInterceptor} if any filters or interceptors are present.
     *
     * @param attributes the resolved {@link AnnotationAttributes} from {@link EnableMyBatisExtension}
     * @param metadata   the {@link AnnotationMetadata} of the importing class
     * @param registry   the {@link BeanDefinitionRegistry} to register bean definitions with
     */
    @Override
    protected void registerBeanDefinitions(AnnotationAttributes attributes, AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        if (attributes.getBoolean("interceptExecutor")) {
            BeanSource[] sources = (BeanSource[]) attributes.get("sources");
            registerExecutorFilters(sources);
            registerExecutorInterceptors(sources);
            registerInterceptingExecutorInterceptorIfRequired(registry);
        }
    }

    /**
     * Registers {@link ExecutorFilter} beans from the specified {@link BeanSource}s.
     *
     * @param sources the array of {@link BeanSource}s to scan for {@link ExecutorFilter} implementations
     */
    private void registerExecutorFilters(BeanSource[] sources) {
        registerBeansFromSources(ExecutorFilter.class, sources);
    }

    /**
     * Registers {@link ExecutorInterceptor} beans from the specified {@link BeanSource}s.
     *
     * @param sources the array of {@link BeanSource}s to scan for {@link ExecutorInterceptor} implementations
     */
    private void registerExecutorInterceptors(BeanSource[] sources) {
        registerBeansFromSources(ExecutorInterceptor.class, sources);
    }

    /**
     * Registers beans of the specified type from the given {@link BeanSource}s.
     * <p>
     * Depending on the source type, this method will register beans via Spring Factories,
     * Java Service Provider Interface (SPI), or expect them to be manually registered
     * in the Bean Factory.
     *
     * @param beanType the type of beans to register
     * @param sources  the array of {@link BeanSource}s indicating where to look for bean implementations
     */
    private void registerBeansFromSources(Class<?> beanType, BeanSource[] sources) {
        Map<Class<?>, String> beanTypesAndNames = registerBeans(this.beanFactory, sources, beanType);
        logger.trace("Registered {} implementation(s) from the sources : {} : {}", beanType, sources, beanTypesAndNames);
    }

    /**
     * Register the {@link BeanDefinition} of {@link InterceptingExecutorInterceptor} if Any {@link ExecutorFilter}  or
     * {@link ExecutorInterceptor} bean is present.
     *
     * @param registry {@link BeanDefinitionRegistry}
     * @see ExecutorFilter
     * @see ExecutorInterceptor
     * @see InterceptingExecutorInterceptor
     */
    private void registerInterceptingExecutorInterceptorIfRequired(BeanDefinitionRegistry registry) {
        String[] executorFilterBeanNames = getBeanNamesByType(ExecutorFilter.class);
        String[] executorInterceptorBeanNames = getBeanNamesByType(ExecutorInterceptor.class);
        int executorFilterBeanCount = length(executorFilterBeanNames);
        int executorInterceptorBeanCount = length(executorInterceptorBeanNames);
        logger.trace("Found {} ExecutorFilter and {} ExecutorInterceptor BeanDefinition(s)",
                executorFilterBeanCount, executorInterceptorBeanCount);
        if (executorFilterBeanCount == 0 && executorInterceptorBeanCount == 0) {
            logger.trace("No bean of ExecutorFilter or ExecutorInterceptor was found.");
            return;
        }
        BeanDefinitionBuilder builder = genericBeanDefinition(InterceptingExecutorInterceptor.class);
        forEach(executorFilterBeanNames, builder::addDependsOn);
        forEach(executorInterceptorBeanNames, builder::addDependsOn);

        BeanDefinition beanDefinition = builder.getBeanDefinition();
        registerBeanDefinition(registry, INTERCEPTING_EXECUTOR_INTERCEPTOR_BEAN_NAME, beanDefinition);
        registerBeanDefinition(registry, SqlSessionFactoryBeanPostProcessor.class);
    }
}
