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

import io.microsphere.mybatis.executor.LoggingExecutorFilter;
import io.microsphere.mybatis.executor.LoggingExecutorInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static io.microsphere.spring.beans.BeanSource.BEAN_FACTORY;
import static io.microsphere.spring.beans.BeanSource.JAVA_SERVICE_PROVIDER;
import static io.microsphere.spring.beans.BeanSource.SPRING_FACTORIES;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;

/**
 * {@link EnableMyBatisExtension} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableMyBatisExtension
 * @since 1.0.0
 */
class EnableMyBatisExtensionTest {

    @Test
    void testDefaultConfig() {
        testInSpringContainer(EnableMyBatisTest::assertTest, EnableMyBatisTest.DefaultConfig.class, DefaultConfig.class);
    }

    @Test
    void testBeanFactorySourceConfig() {
        testInSpringContainer(EnableMyBatisTest::assertTest, BeanFactorySourceConfig.class, EnableMyBatisTest.DefaultConfig.class);
    }

    @Test
    void testSpringFactoriesSourceConfig() {
        testInSpringContainer(EnableMyBatisTest::assertTest, EnableMyBatisTest.DefaultConfig.class, SpringFactoriesSourceConfig.class);
    }

    @Test
    void testJavaServiceProviderSourceConfig() {
        testInSpringContainer(EnableMyBatisTest::assertTest, EnableMyBatisTest.DefaultConfig.class, JavaServiceProviderSourceConfig.class);
    }

    @Test
    void testNoConfig() {
        testInSpringContainer(EnableMyBatisTest::assertTest, EnableMyBatisTest.DefaultConfig.class, NoConfig.class);
    }

    @Test
    void testOnlyFilterConfig() {
        testInSpringContainer(EnableMyBatisTest::assertTest, EnableMyBatisTest.DefaultConfig.class, OnlyFilterConfig.class);
    }

    @Test
    void testOnlyInterceptorConfig() {
        testInSpringContainer(EnableMyBatisTest::assertTest, EnableMyBatisTest.DefaultConfig.class, OnlyInterceptorConfig.class);
    }

    @Test
    void testDisabledConfig() {
        testInSpringContainer(EnableMyBatisTest::assertTest, EnableMyBatisTest.DefaultConfig.class, DisabledConfig.class);
    }

    @EnableMyBatisExtension
    static class DefaultConfig {
    }

    @EnableMyBatisExtension(sources = BEAN_FACTORY)
    @Import(value = {
            LoggingExecutorFilter.class,
            LoggingExecutorInterceptor.class
    })
    static class BeanFactorySourceConfig {
    }

    @EnableMyBatisExtension(sources = SPRING_FACTORIES)
    static class SpringFactoriesSourceConfig {
    }

    @EnableMyBatisExtension(sources = JAVA_SERVICE_PROVIDER)
    static class JavaServiceProviderSourceConfig {
    }

    @EnableMyBatisExtension(sources = BEAN_FACTORY)
    static class NoConfig {
    }

    @EnableMyBatisExtension(sources = BEAN_FACTORY)
    @Import(LoggingExecutorFilter.class)
    static class OnlyFilterConfig {
    }

    @EnableMyBatisExtension(sources = BEAN_FACTORY)
    @Import(LoggingExecutorInterceptor.class)
    static class OnlyInterceptorConfig {
    }

    @EnableMyBatisExtension(interceptExecutor = false)
    static class DisabledConfig {
    }
}