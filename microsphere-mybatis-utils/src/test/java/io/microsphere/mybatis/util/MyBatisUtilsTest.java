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

package io.microsphere.mybatis.util;

import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import static io.microsphere.mybatis.util.MyBatisUtils.buildSqlSessionFactory;
import static io.microsphere.mybatis.util.MyBatisUtils.doInExecutor;
import static io.microsphere.mybatis.util.MyBatisUtils.getConfiguration;
import static io.microsphere.mybatis.util.MyBatisUtils.getDataSource;
import static io.microsphere.mybatis.util.MyBatisUtils.getEnvironment;
import static io.microsphere.mybatis.util.MyBatisUtils.loadProperties;
import static io.microsphere.mybatis.util.MyBatisUtils.runScript;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link MyBatisUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MyBatisUtils
 * @since 1.0.0
 */
class MyBatisUtilsTest {

    @Test
    void testLoadProperties() throws IOException {
        Properties properties = loadProperties("META-INF/mybatis/test-mybatis.properties");
        assertEquals("org.h2.Driver", properties.get("jdbc.driver"));
        assertEquals("jdbc:h2:mem:test_mem", properties.get("jdbc.url"));
        assertEquals("sa", properties.get("jdbc.username"));
        assertEquals("", properties.get("jdbc.password"));
    }

    @Test
    void testGetConfiguration() throws IOException {
        Configuration configuration = getConfiguration("META-INF/mybatis/test-config.xml");
        assertNotNull(configuration);
        assertNotNull(getEnvironment(configuration));
        assertNotNull(getDataSource(configuration));
        assertNotNull(buildSqlSessionFactory(configuration));
    }

    @Test
    void testRunScript() throws IOException, SQLException {
        Configuration configuration = configuration();
        runScript(getDataSource(configuration), "META-INF/mybatis/test-scripts.sql");
    }

    @Test
    void testDoInExecutor() throws Throwable {
        Configuration configuration = configuration();
        doInExecutor(configuration, executor -> {
            assertNotNull(executor.getTransaction());
        });
    }

    Configuration configuration() throws IOException {
        Properties properties = loadProperties("META-INF/mybatis/test-mybatis.properties");
        return getConfiguration("META-INF/mybatis/test-config.xml", "default", properties);
    }
}
