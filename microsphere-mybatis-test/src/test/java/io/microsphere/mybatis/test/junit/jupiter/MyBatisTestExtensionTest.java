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

import io.microsphere.mybatis.test.mapper.ChildMapper;
import io.microsphere.mybatis.test.mapper.FatherMapper;
import io.microsphere.mybatis.test.mapper.UserMapper;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import static io.microsphere.mybatis.test.AbstractExecutorTest.assertExecutor;
import static io.microsphere.mybatis.test.AbstractMapperTest.assertChildMapper;
import static io.microsphere.mybatis.test.AbstractMapperTest.assertFatherMapper;
import static io.microsphere.mybatis.test.AbstractMapperTest.assertUserMapper;
import static io.microsphere.mybatis.test.AbstractMyBatisTest.assertConfiguration;
import static io.microsphere.mybatis.test.MyBatisTestUtils.DESTROY_DB_SCRIPT_RESOURCE_NAME;
import static io.microsphere.mybatis.test.MyBatisTestUtils.INIT_DB_SCRIPT_RESOURCE_NAME;
import static io.microsphere.mybatis.util.MyBatisUtils.runScript;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link MyBatisTestExtension} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MyBatisTestExtension
 * @since 1.0.0
 */
@MyBatisTest
class MyBatisTestExtensionTest {

    @Nested
    @DisplayName("Test : Injection on instance fields")
    class FieldTests {

        @MyBatisRuntime
        private Configuration configuration;

        @MyBatisRuntime
        private Environment environment;

        @MyBatisRuntime
        private SqlSessionFactory sqlSessionFactory;

        @MyBatisRuntime
        private DataSource dataSource;

        @MyBatisRuntime
        private Properties properties;

        @MyBatisRuntime
        private SqlSession sqlSession;

        @MyBatisRuntime
        private Transaction transaction;

        @MyBatisRuntime
        private Executor executor;

        @MyBatisRuntime
        private UserMapper userMapper;

        @MyBatisRuntime
        private ChildMapper childMapper;

        @MyBatisRuntime
        FatherMapper fatherMapper;

        @Test
        void test() throws SQLException, IOException {
            assertAll(configuration, environment, sqlSessionFactory, dataSource, properties, sqlSession, transaction,
                    executor, userMapper);

            runScript(dataSource, INIT_DB_SCRIPT_RESOURCE_NAME);
            assertConfiguration(configuration);
            assertExecutor(configuration, executor);
            assertUserMapper(userMapper);
            assertChildMapper(childMapper);
            assertFatherMapper(fatherMapper);
            runScript(dataSource, DESTROY_DB_SCRIPT_RESOURCE_NAME);
        }
    }

    @Nested
    @DisplayName("Test : Injection on static fields")
    class StaticFieldTests {

        @MyBatisRuntime
        private static Configuration configuration;

        @MyBatisRuntime
        private static Environment environment;

        @MyBatisRuntime
        private static SqlSessionFactory sqlSessionFactory;

        @MyBatisRuntime
        private static DataSource dataSource;

        @MyBatisRuntime
        private static Properties properties;

        @MyBatisRuntime
        private static SqlSession sqlSession;

        @MyBatisRuntime
        private static Transaction transaction;

        @MyBatisRuntime
        private static Executor executor;

        @MyBatisRuntime
        private static UserMapper userMapper;

        @Test
        void test() {
            assertNotNull(configuration);
            assertSame(environment, configuration.getEnvironment());
            assertSame(configuration, sqlSessionFactory.getConfiguration());
            assertSame(dataSource, environment.getDataSource());
            assertNotNull(properties);

            assertNull(sqlSession);
            assertNull(transaction);
            assertNull(executor);
            assertNull(userMapper);
        }
    }

    @Nested
    @DisplayName("Test : Injection on parameters of methods")
    class MethodTests {

        @Test
        void test(@MyBatisRuntime Configuration configuration,
                  @MyBatisRuntime Environment environment,
                  @MyBatisRuntime SqlSessionFactory sqlSessionFactory,
                  @MyBatisRuntime DataSource dataSource,
                  @MyBatisRuntime Properties properties,
                  @MyBatisRuntime SqlSession sqlSession,
                  @MyBatisRuntime Transaction transaction,
                  @MyBatisRuntime Executor executor,
                  @MyBatisRuntime UserMapper userMapper) {
            assertAll(configuration, environment, sqlSessionFactory, dataSource, properties, sqlSession, transaction,
                    executor, userMapper);
        }
    }

    void assertAll(Configuration configuration,
                   Environment environment,
                   SqlSessionFactory sqlSessionFactory,
                   DataSource dataSource,
                   Properties properties,
                   SqlSession sqlSession,
                   Transaction transaction,
                   Executor executor,
                   UserMapper userMapper) {
        assertNotNull(configuration);
        assertSame(environment, configuration.getEnvironment());
        assertSame(configuration, sqlSessionFactory.getConfiguration());
        assertSame(dataSource, environment.getDataSource());

        assertNotNull(properties);
        assertNotNull(sqlSession);
        assertNotNull(transaction);
        assertNotNull(executor);
        assertNotNull(userMapper);
    }
}
