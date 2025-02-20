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
package io.microsphere.mybatis.plugin;

import io.microsphere.lang.function.ThrowableConsumer;
import io.microsphere.mybatis.executor.LogggingExecutorInterceptor;
import io.microsphere.mybatis.test.entity.User;
import io.microsphere.mybatis.test.mapper.UserMapper;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link InterceptingExecutorInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see InterceptingExecutorInterceptor
 * @since 1.0.0
 */
public class InterceptingExecutorInterceptorTest {

    public static final String MS_ID_SAVE_USER = "io.microsphere.mybatis.test.mapper.UserMapper.saveUser";

    public static final String MS_ID_USER_BY_ID = "io.microsphere.mybatis.test.mapper.UserMapper.getUserById";

    public static final String MS_ID_USER_BY_NAME = "io.microsphere.mybatis.test.mapper.UserMapper.getUserByName";

    private InterceptingExecutorInterceptor interceptor;

    private SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    public void init() throws Throwable {
        this.interceptor = createInterceptingExecutorInterceptor();
        this.sqlSessionFactory = buildSqlSessionFactory();
        initData();
    }

    private InterceptingExecutorInterceptor createInterceptingExecutorInterceptor() {
        LogggingExecutorInterceptor loggingExecutorInterceptor = new LogggingExecutorInterceptor();
        InterceptingExecutorInterceptor interceptingExecutorInterceptor = new InterceptingExecutorInterceptor(asList(loggingExecutorInterceptor));
        Properties properties = new Properties();
        properties.setProperty("test.class", this.getClass().getName());
        interceptingExecutorInterceptor.setProperties(properties);
        return interceptingExecutorInterceptor;
    }

    private SqlSessionFactory buildSqlSessionFactory() throws IOException {
        String resource = "META-INF/mybatis/config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        SqlSessionFactory factory = builder.build(inputStream);
        factory.getConfiguration().addInterceptor(this.interceptor);
        return factory;
    }

    private SqlSession openSqlSession() {
        return this.sqlSessionFactory.openSession();
    }

    private UserMapper getUserMapper(SqlSession sqlSession) throws Throwable {
        return sqlSession.getMapper(UserMapper.class);
    }

    private void initData() throws Throwable {
        doInStatement(statement -> {
            statement.execute("CREATE TABLE users (id INT, name VARCHAR(50))");
        });
    }

    private void doInStatement(ThrowableConsumer<Statement> consumer) throws Throwable {
        doInConnection(connection -> {
            Statement statement = connection.createStatement();
            try {
                consumer.accept(statement);
            } finally {
                statement.close();
            }
        });
    }

    private void doInConnection(ThrowableConsumer<Connection> consumer) throws Throwable {
        doInSqlSession(sqlSession -> consumer.accept(sqlSession.getConnection()));
    }

    private void doInSqlSession(ThrowableConsumer<SqlSession> consumer) throws Throwable {
        SqlSession sqlSession = openSqlSession();
        try {
            consumer.accept(sqlSession);
        } finally {
            sqlSession.close();
        }
    }

    private void doInExecutor(ThrowableConsumer<Executor> consumer) throws Throwable {
        doInConnection(connection -> {
            Configuration configuration = this.sqlSessionFactory.getConfiguration();
            Environment environment = configuration.getEnvironment();
            TransactionFactory transactionFactory = environment.getTransactionFactory();
            Transaction transaction = transactionFactory.newTransaction(connection);
            Executor executor = configuration.newExecutor(transaction);
            try {
                consumer.accept(executor);
            } finally {
                executor.close(false);
            }
        });
    }


    @AfterEach
    public void destroy() throws Throwable {
        destroyData();
    }

    private void destroyData() throws Throwable {
        doInStatement(statement -> {
            statement.execute("DROP TABLE users");
        });
    }

    private User createUser() {
        int id = 1;
        String name = "Mercy";
        return new User(id, name);
    }

    @Test
    public void testMapper() throws Throwable {
        doInSqlSession(sqlSession -> {
            UserMapper userMapper = getUserMapper(sqlSession);
            User user = createUser();
            // Test saveUser
            userMapper.saveUser(user);

            // Test getUserById
            User foundUser = userMapper.getUserById(user.getId());
            assertEquals(foundUser, user);

            // Test getUserByName
            foundUser = userMapper.getUserByName(user.getName());
            assertEquals(foundUser, user);
        });
    }


    @Test
    public void testSqlSession() throws Throwable {
        doInSqlSession(sqlSession -> {

            User user = createUser();

            // Test insert
            assertEquals(1, sqlSession.insert(MS_ID_SAVE_USER, user));

            // Test selectCursor
            Cursor<User> cursor = sqlSession.selectCursor(MS_ID_USER_BY_ID, user.getId());
            assertNotNull(cursor);
            assertTrue(cursor.isOpen());
            assertFalse(cursor.isConsumed());
            assertEquals(0, cursor.getCurrentIndex());
            cursor.forEach(foundUser -> assertEquals(foundUser, user));

            // Test selectOne
            User foundUser = sqlSession.selectOne(MS_ID_USER_BY_NAME, user.getName());
            assertEquals(foundUser, user);

            // Test selectList
            List<User> users = sqlSession.selectList(MS_ID_USER_BY_NAME, user.getName());
            assertEquals(1, users.size());
            assertEquals(users.get(0), user);

            // Test flushStatements
            assertNotNull(sqlSession.flushStatements());

            // Test commit
            sqlSession.commit();
            sqlSession.commit(true);

            // Test rollback
            sqlSession.rollback();
            sqlSession.rollback(true);

            // Test clearCache
            sqlSession.clearCache();
        });

    }

    @Test
    public void testExecutor() throws Throwable {
        doInExecutor(executor -> {
            Configuration configuration = this.sqlSessionFactory.getConfiguration();
            MappedStatement ms = configuration.getMappedStatement(MS_ID_SAVE_USER);
            User user = createUser();

            // Test update
            assertEquals(1, executor.update(ms, user));

            // Test query
            ms = configuration.getMappedStatement(MS_ID_USER_BY_ID);
            List<User> users = executor.query(ms, user.getId(), new RowBounds(), Executor.NO_RESULT_HANDLER);
            assertEquals(1, users.size());
            assertEquals(users.get(0), user);
        });
    }

}
