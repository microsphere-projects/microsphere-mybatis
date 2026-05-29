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

import io.microsphere.mybatis.test.MyBatisTestUtils;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static io.microsphere.mybatis.test.MyBatisTestUtils.DEFAULT_CONFIG_RESOURCE_NAME;
import static io.microsphere.mybatis.test.MyBatisTestUtils.DEFAULT_ENVIRONMENT_ID;
import static io.microsphere.mybatis.test.MyBatisTestUtils.DEFAULT_PROPERTIES_RESOURCE_NAME;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@link MyBatisTest} is a JUnit Jupiter extension to enable {@link MyBatisRuntime MyBatis runtime} used in a test case.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MyBatisRuntime
 * @see MyBatisTestExtension
 * @since 1.0.0
 */
@Target(TYPE)
@Retention(RUNTIME)
@ExtendWith(MyBatisTestExtension.class)
@Inherited
public @interface MyBatisTest {

    /**
     * Resource name of MyBatis XML config file, for example,
     * {@code "META-INF/mybatis/config.xml"}
     *
     * @return {@link MyBatisTestUtils#DEFAULT_CONFIG_RESOURCE_NAME} as default.
     * @see MyBatisTestUtils#DEFAULT_CONFIG_RESOURCE_NAME
     *
     */
    String configResource() default DEFAULT_CONFIG_RESOURCE_NAME;

    /**
     * MyBatis Environment ID
     *
     * @return {@link MyBatisTestUtils#DEFAULT_ENVIRONMENT_ID} as default.
     * @see MyBatisTestUtils#DEFAULT_ENVIRONMENT_ID
     */
    String environment() default DEFAULT_ENVIRONMENT_ID;

    /**
     * Resource name of MyBatis Properties config file, for example,
     * {@code "META-INF/mybatis/mybatis.properties"}
     *
     * @return {@link MyBatisTestUtils#DEFAULT_PROPERTIES_RESOURCE_NAME} as default.
     * @see MyBatisTestUtils#DEFAULT_PROPERTIES_RESOURCE_NAME
     */
    String propertiesResource() default DEFAULT_PROPERTIES_RESOURCE_NAME;
}
