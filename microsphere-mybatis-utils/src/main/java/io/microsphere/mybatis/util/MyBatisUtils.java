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

import io.microsphere.annotation.Nonnull;
import io.microsphere.lang.function.ThrowableConsumer;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.apache.ibatis.io.Resources.getResourceAsReader;

/**
 * The utility class of MyBatis.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Load properties
 *   Properties props = MyBatisUtils.loadProperties("META-INF/mybatis/mybatis.properties");
 *
 *   // Build a Configuration
 *   Configuration configuration = MyBatisUtils.getConfiguration(
 *       "META-INF/mybatis/config.xml", "default", props);
 *
 *   // Run a DDL/DML script
 *   MyBatisUtils.runScript(MyBatisUtils.getDataSource(configuration),
 *       "META-INF/mybatis/schema.sql");
 *
 *   // Execute logic inside an Executor
 *   MyBatisUtils.doInExecutor(configuration, executor -> {
 *       Transaction tx = executor.getTransaction();
 *       // ... use executor
 *   });
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class MyBatisUtils {

    /**
     * Load Default MyBatis Properties
     *
     * @param propertiesResource the resource of MyBatis Properties
     * @return non-null
     * @throws IOException
     */
    @Nonnull
    public static Properties loadProperties(String propertiesResource) throws IOException {
        try (Reader reader = getResourceAsReader(propertiesResource)) {
            Properties properties = new Properties();
            properties.load(reader);
            return properties;
        }
    }

    /**
     * Get MyBatis Configuration
     *
     * @param configResource the resource of MyBatis Configuration
     * @return non-null
     * @throws IOException
     */
    @Nonnull
    public static Configuration getConfiguration(String configResource) throws IOException {
        return getConfiguration(configResource, null);
    }

    /**
     * Get MyBatis Configuration
     *
     * @param configResource the resource of MyBatis Configuration
     * @param environment    the environment
     * @return non-null
     * @throws IOException
     */
    @Nonnull
    public static Configuration getConfiguration(String configResource, String environment) throws IOException {
        return getConfiguration(configResource, environment, null);
    }

    /**
     * Get MyBatis Configuration
     *
     * @param configResource the resource of MyBatis Configuration
     * @param environment    the environment
     * @param properties     the properties
     * @return non-null
     * @throws IOException
     */
    @Nonnull
    public static Configuration getConfiguration(String configResource, String environment, Properties properties) throws IOException {
        try (Reader reader = getResourceAsReader(configResource)) {
            XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder(reader, environment, properties);
            return xmlConfigBuilder.parse();
        }
    }

    /**
     * Get MyBatis Environment from MyBatis Configuration
     *
     * @param configuration {@link Configuration}
     * @return non-null
     */
    @Nonnull
    public static Environment getEnvironment(@Nonnull Configuration configuration) {
        return configuration.getEnvironment();
    }

    /**
     * Get MyBatis DataSource from MyBatis Configuration
     *
     * @param configuration {@link Configuration}
     * @return non-null
     */
    @Nonnull
    public static DataSource getDataSource(@Nonnull Configuration configuration) {
        return getDataSource(getEnvironment(configuration));
    }

    /**
     * Get MyBatis DataSource from MyBatis Environment
     *
     * @param environment {@link Environment}
     * @return non-null
     */
    @Nonnull
    public static DataSource getDataSource(@Nonnull Environment environment) {
        return environment.getDataSource();
    }

    /**
     * Build MyBatis SqlSessionFactory
     *
     * @param configuration {@link Configuration}
     * @return non-null
     * @throws IOException
     */
    @Nonnull
    public static SqlSessionFactory buildSqlSessionFactory(Configuration configuration) {
        return new DefaultSqlSessionFactory(configuration);
    }

    /**
     * Open a new {@link SqlSession} from the given {@link Configuration}.
     *
     * @param configuration {@link Configuration}
     * @return a new {@link SqlSession}; the caller is responsible for closing it
     */
    public static SqlSession openSession(Configuration configuration) {
        return buildSqlSessionFactory(configuration).openSession();
    }

    /**
     * Get a {@link Connection} from the {@link SqlSession} opened for the given {@link Configuration}.
     * <p><strong>Note:</strong> The returned connection is not automatically closed.
     *
     * @param configuration {@link Configuration}
     * @return a {@link Connection} from the underlying data source
     */
    public static Connection getConnection(Configuration configuration) {
        return openSession(configuration).getConnection();
    }

    /**
     * Create a new {@link Transaction} for the given {@link Configuration} and {@link Connection}.
     *
     * @param configuration {@link Configuration}
     * @param connection    an open {@link Connection}
     * @return a new {@link Transaction}
     */
    public static Transaction newTransaction(Configuration configuration, Connection connection) {
        Environment environment = getEnvironment(configuration);
        TransactionFactory transactionFactory = environment.getTransactionFactory();
        return transactionFactory.newTransaction(connection);
    }

    /**
     * Create a new {@link Executor} from the given {@link Configuration} and {@link Connection}.
     *
     * @param configuration {@link Configuration}
     * @param connection    an open {@link Connection}
     * @return a new {@link Executor}
     */
    public static Executor newExecutor(Configuration configuration, Connection connection) {
        Transaction transaction = newTransaction(configuration, connection);
        return configuration.newExecutor(transaction);
    }

    /**
     * Run Script
     *
     * @param dataSource     {@link DataSource}
     * @param scriptResource the resource of Script
     * @throws SQLException
     * @throws IOException
     */
    public static void runScript(DataSource dataSource, String scriptResource) throws IOException, SQLException {
        try (Connection connection = dataSource.getConnection()) {
            ScriptRunner runner = new ScriptRunner(connection);
            runner.setAutoCommit(true);
            runner.setStopOnError(false);
            runner.setLogWriter(null);
            runner.setErrorLogWriter(null);
            runScript(runner, scriptResource);
        }
    }

    /**
     * Run Script
     *
     * @param runner   {@link ScriptRunner}
     * @param resource the resource of Script
     * @throws IOException
     */
    public static void runScript(ScriptRunner runner, String resource) throws IOException {
        try (Reader reader = getResourceAsReader(resource)) {
            runner.runScript(reader);
        }
    }

    /**
     * Do In Executor
     *
     * @param configuration {@link Configuration}
     * @param consumer      {@link ThrowableConsumer} of {@link Executor}
     * @throws Throwable
     */
    public static void doInExecutor(Configuration configuration, ThrowableConsumer<Executor> consumer) throws Throwable {
        doInConnection(configuration, connection -> {
            Executor executor = newExecutor(configuration, connection);
            try {
                consumer.accept(executor);
            } finally {
                executor.close(false);
            }
        });
    }

    /**
     * Do In Connection
     *
     * @param configuration {@link Configuration}
     * @param consumer      {@link ThrowableConsumer} of {@link Connection}
     * @throws Throwable
     */
    public static <M> void doInConnection(Configuration configuration, ThrowableConsumer<Connection> consumer) throws Throwable {
        doInSqlSession(configuration, sqlSession -> {
            Connection connection = sqlSession.getConnection();
            try {
                consumer.accept(connection);
            } finally {
                connection.close();
            }
        });
    }

    /**
     * Do In SqlSession
     *
     * @param configuration {@link Configuration}
     * @param consumer      {@link ThrowableConsumer} of {@link SqlSession}
     * @throws Throwable
     */
    public static void doInSqlSession(Configuration configuration, ThrowableConsumer<SqlSession> consumer) throws Throwable {
        try (SqlSession sqlSession = openSession(configuration)) {
            consumer.accept(sqlSession);
        }
    }

    private MyBatisUtils() {
    }
}