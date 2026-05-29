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

package io.microsphere.mybatis.test;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;

import static io.microsphere.mybatis.test.AbstractMyBatisTest.assertConfiguration;
import static io.microsphere.mybatis.test.MyBatisTestUtils.CHILD_TYPE_ALIAS;
import static io.microsphere.mybatis.test.MyBatisTestUtils.DEFAULT_CONFIG_RESOURCE_NAME;
import static io.microsphere.mybatis.test.MyBatisTestUtils.DEFAULT_ENVIRONMENT_ID;
import static io.microsphere.mybatis.test.MyBatisTestUtils.DEFAULT_PROPERTIES_RESOURCE_NAME;
import static io.microsphere.mybatis.test.MyBatisTestUtils.DESTROY_DB_SCRIPT_RESOURCE_NAME;
import static io.microsphere.mybatis.test.MyBatisTestUtils.EMPTY_CONFIG_RESOURCE_NAME;
import static io.microsphere.mybatis.test.MyBatisTestUtils.FATHER_TYPE_ALIAS;
import static io.microsphere.mybatis.test.MyBatisTestUtils.INIT_DB_SCRIPT_RESOURCE_NAME;
import static io.microsphere.mybatis.test.MyBatisTestUtils.USER_TYPE_ALIAS;
import static io.microsphere.mybatis.test.MyBatisTestUtils.buildDefaultSqlSessionFactory;
import static io.microsphere.mybatis.test.MyBatisTestUtils.getDefaultConfiguration;
import static io.microsphere.mybatis.test.MyBatisTestUtils.getDefaultDataSource;
import static io.microsphere.mybatis.test.MyBatisTestUtils.getDefaultEnvironment;
import static io.microsphere.mybatis.test.MyBatisTestUtils.runCreateDatabaseScript;
import static io.microsphere.mybatis.test.MyBatisTestUtils.runDestroyDatabaseScript;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link MyBatisTestUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MyBatisTestUtils
 * @since 1.0.0
 */
class MyBatisTestUtilsTest {

    @Test
    void testContaints() {
        assertSame("META-INF/mybatis/config.xml", DEFAULT_CONFIG_RESOURCE_NAME);
        assertSame("META-INF/mybatis/empty-config.xml", EMPTY_CONFIG_RESOURCE_NAME);
        assertSame("testing", DEFAULT_ENVIRONMENT_ID);
        assertSame("META-INF/mybatis/mybatis.properties", DEFAULT_PROPERTIES_RESOURCE_NAME);
        assertSame("META-INF/sql/init-db.sql", INIT_DB_SCRIPT_RESOURCE_NAME);
        assertSame("META-INF/sql/destroy-db.sql", DESTROY_DB_SCRIPT_RESOURCE_NAME);
        assertSame("user", USER_TYPE_ALIAS);
        assertSame("child", CHILD_TYPE_ALIAS);
        assertSame("father", FATHER_TYPE_ALIAS);
    }

    @Test
    void testGetDefaultConfiguration() throws IOException {
        Configuration configuration = getDefaultConfiguration();
        assertConfiguration(configuration);
    }

    @Test
    void testGetDefaultEnvironment() throws IOException {
        Environment defaultEnvironment = getDefaultEnvironment();
        assertNotNull(defaultEnvironment);
    }

    @Test
    void testGetDefaultDataSource() throws Exception {
        DataSource dataSource = getDefaultDataSource();
        assertNotNull(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection);
        }
    }

    @Test
    void testBuildDefaultSqlSessionFactory() throws IOException {
        SqlSessionFactory sqlSessionFactory = buildDefaultSqlSessionFactory();
        Configuration configuration = sqlSessionFactory.getConfiguration();
        assertConfiguration(configuration);
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            assertNotNull(sqlSession);
        }
    }

    @Test
    void testRunScript() {
        assertDoesNotThrow(() -> {
            DataSource dataSource = getDefaultDataSource();
            runCreateDatabaseScript(dataSource);
            runDestroyDatabaseScript(dataSource);
        });
    }
}
