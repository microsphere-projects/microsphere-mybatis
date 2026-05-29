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

import io.microsphere.annotation.Nonnull;
import io.microsphere.mybatis.test.entity.Child;
import io.microsphere.mybatis.test.entity.Father;
import io.microsphere.mybatis.test.entity.User;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import static io.microsphere.mybatis.util.MyBatisUtils.buildSqlSessionFactory;
import static io.microsphere.mybatis.util.MyBatisUtils.getConfiguration;
import static io.microsphere.mybatis.util.MyBatisUtils.getDataSource;
import static io.microsphere.mybatis.util.MyBatisUtils.getEnvironment;
import static io.microsphere.mybatis.util.MyBatisUtils.loadProperties;
import static io.microsphere.mybatis.util.MyBatisUtils.runScript;

/**
 * The constants interface of MyBatis Test.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class MyBatisTestUtils {

    /**
     * Default MyBatis Configuration Resource Name
     */
    public static final String DEFAULT_CONFIG_RESOURCE_NAME = "META-INF/mybatis/config.xml";

    /**
     * Empty MyBatis Configuration Resource Name
     */
    public static final String EMPTY_CONFIG_RESOURCE_NAME = "META-INF/mybatis/empty-config.xml";

    /**
     * Default MyBatis Environment ID
     */
    public static final String DEFAULT_ENVIRONMENT_ID = "testing";

    /**
     * Default MyBatis Properties Resource Name
     */
    public static final String DEFAULT_PROPERTIES_RESOURCE_NAME = "META-INF/mybatis/mybatis.properties";

    /**
     * Initialization DB Script Resource Name
     */
    public static final String INIT_DB_SCRIPT_RESOURCE_NAME = "META-INF/sql/init-db.sql";

    /**
     * Destroy DB Script Resource Name
     */
    public static final String DESTROY_DB_SCRIPT_RESOURCE_NAME = "META-INF/sql/destroy-db.sql";

    /**
     * The type alias of {@link User}
     */
    public static final String USER_TYPE_ALIAS = "user";

    /**
     * The type alias of {@link Child}
     */
    public static final String CHILD_TYPE_ALIAS = "child";

    /**
     * The type alias of {@link Father}
     */
    public static final String FATHER_TYPE_ALIAS = "father";

    /**
     * Load Default MyBatis Properties
     *
     * @return non-null
     * @throws IOException
     */
    @Nonnull
    public static Properties loadDefaultProperties() throws IOException {
        return loadProperties(DEFAULT_PROPERTIES_RESOURCE_NAME);
    }

    /**
     * Get Default MyBatis Configuration
     *
     * @return non-null
     * @throws IOException
     */
    @Nonnull
    public static Configuration getDefaultConfiguration() throws IOException {
        return getConfiguration(DEFAULT_CONFIG_RESOURCE_NAME, DEFAULT_ENVIRONMENT_ID, loadDefaultProperties());
    }

    /**
     * Get Default MyBatis DataSource
     *
     * @return non-null
     * @throws IOException
     */
    @Nonnull
    public static DataSource getDefaultDataSource() throws IOException {
        return getDataSource(getDefaultConfiguration());
    }

    /**
     * Get Default MyBatis Environment
     *
     * @return non-null
     * @throws IOException
     */
    @Nonnull
    public static Environment getDefaultEnvironment() throws IOException {
        return getEnvironment(getDefaultConfiguration());
    }

    /**
     * Build Default MyBatis SqlSessionFactory
     *
     * @return non-null
     * @throws IOException
     */
    @Nonnull
    public static SqlSessionFactory buildDefaultSqlSessionFactory() throws IOException {
        return buildSqlSessionFactory(getDefaultConfiguration());
    }

    /**
     * Run Create Database Script
     *
     * @param ds {@link DataSource}
     * @throws SQLException
     * @throws IOException
     */
    public static void runCreateDatabaseScript(DataSource ds) throws SQLException, IOException {
        runScript(ds, INIT_DB_SCRIPT_RESOURCE_NAME);
    }

    /**
     * Run Destroy Database Script
     *
     * @param ds {@link DataSource}
     * @throws SQLException
     * @throws IOException
     */
    public static void runDestroyDatabaseScript(DataSource ds) throws SQLException, IOException {
        runScript(ds, DESTROY_DB_SCRIPT_RESOURCE_NAME);
    }

    private MyBatisTestUtils() {
    }
}
