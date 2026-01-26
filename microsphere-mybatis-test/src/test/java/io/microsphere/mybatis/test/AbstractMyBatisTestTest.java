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

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;

import static io.microsphere.mybatis.test.AbstractMyBatisTest.CHILD_TYPE_ALIAS;
import static io.microsphere.mybatis.test.AbstractMyBatisTest.CONFIG_RESOURCE_NAME;
import static io.microsphere.mybatis.test.AbstractMyBatisTest.CREATE_DB_SCRIPT_RESOURCE_NAME;
import static io.microsphere.mybatis.test.AbstractMyBatisTest.DESTROY_DB_SCRIPT_RESOURCE_NAME;
import static io.microsphere.mybatis.test.AbstractMyBatisTest.DEVELOPMENT_ID;
import static io.microsphere.mybatis.test.AbstractMyBatisTest.EMPTY_CONFIG_RESOURCE_NAME;
import static io.microsphere.mybatis.test.AbstractMyBatisTest.FATHER_TYPE_ALIAS;
import static io.microsphere.mybatis.test.AbstractMyBatisTest.PROPERTIES_RESOURCE_NAME;
import static io.microsphere.mybatis.test.AbstractMyBatisTest.USER_TYPE_ALIAS;
import static io.microsphere.mybatis.test.AbstractMyBatisTest.assertConfiguration;
import static io.microsphere.mybatis.test.AbstractMyBatisTest.buildSqlSessionFactory;
import static io.microsphere.mybatis.test.AbstractMyBatisTest.configuration;
import static io.microsphere.mybatis.test.AbstractMyBatisTest.dataSource;
import static io.microsphere.mybatis.test.AbstractMyBatisTest.runCreateDatabaseScript;
import static io.microsphere.mybatis.test.AbstractMyBatisTest.runDestroyDatabaseScript;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * The test class for {@link AbstractMyBatisTest}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractMyBatisTest
 * @since 1.0.0
 */
class AbstractMyBatisTestTest {

    @Test
    void testContaints() {
        assertSame("META-INF/mybatis/config.xml", CONFIG_RESOURCE_NAME);
        assertSame("META-INF/mybatis/empty-config.xml", EMPTY_CONFIG_RESOURCE_NAME);
        assertSame("development", DEVELOPMENT_ID);
        assertSame("META-INF/mybatis/mybatis.properties", PROPERTIES_RESOURCE_NAME);
        assertSame("META-INF/sql/create-db.sql", CREATE_DB_SCRIPT_RESOURCE_NAME);
        assertSame("META-INF/sql/destroy-db.sql", DESTROY_DB_SCRIPT_RESOURCE_NAME);
        assertSame("user", USER_TYPE_ALIAS);
        assertSame("child", CHILD_TYPE_ALIAS);
        assertSame("father", FATHER_TYPE_ALIAS);
    }

    @Test
    void testConfiguration() throws IOException {
        Configuration configuration = configuration();
        assertConfiguration(configuration);
    }

    @Test
    void testDataSource() throws Exception {
        DataSource dataSource = dataSource();
        assertNotNull(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection);
        }
    }

    @Test
    void testBuildSqlSessionFactory() throws IOException {
        SqlSessionFactory sqlSessionFactory = buildSqlSessionFactory();
        Configuration configuration = sqlSessionFactory.getConfiguration();
        assertConfiguration(configuration);

        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            assertNotNull(sqlSession);
        }
    }

    @Test
    void testRunScript() throws Exception {
        DataSource dataSource = dataSource();
        runCreateDatabaseScript(dataSource);
        runDestroyDatabaseScript(dataSource);
    }
}