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

import io.microsphere.mybatis.executor.LoggingExecutorInterceptor;
import io.microsphere.mybatis.executor.LoggingExecutorFilter;
import io.microsphere.mybatis.executor.NoOpExecutorInterceptor;
import io.microsphere.mybatis.executor.TestExecutorFilter;
import io.microsphere.mybatis.executor.ThrowingErrorExecutorInterceptor;
import io.microsphere.mybatis.test.AbstractMapperTest;
import io.microsphere.mybatis.test.mapper.UserMapper;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.Properties;

import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.util.ArrayUtils.of;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Collections.emptyList;
import static org.apache.ibatis.session.RowBounds.DEFAULT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link InterceptingExecutorInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see InterceptingExecutorInterceptor
 * @since 1.0.0
 */
public class InterceptingExecutorInterceptorTest extends AbstractMapperTest {

    public static final String TEST_PROPERTY_KEY = "test.class";

    @Test
    void testInValidConstructorArgs() {
        assertThrows(IllegalArgumentException.class, () -> new InterceptingExecutorInterceptor(ofArray()));
    }

    @Test
    void testOnFailed() throws Throwable {
        // test Executor#update
        doInExecutor(executor -> {

            // test Executor#update
            runSafely(() -> {
                MappedStatement ms = getMappedStatement(MS_ID_SAVE_USER);
                executor.update(ms, null);
            });

            // test Executor#query
            runSafely(() -> {
                MappedStatement ms = getMappedStatement(MS_ID_USER_BY_ID);
                executor.query(ms, null, DEFAULT, Executor.NO_RESULT_HANDLER);
            });

            runSafely(() -> {
                MappedStatement ms = getMappedStatement(MS_ID_USER_BY_ID);
                BoundSql boundSql = new BoundSql(getConfiguration(), MS_ID_USER_BY_ID, emptyList(), null);

                CacheKey cacheKey = executor.createCacheKey(ms, null, new RowBounds(), boundSql);
                executor.query(ms, null, DEFAULT, Executor.NO_RESULT_HANDLER, cacheKey, boundSql);
            });

            // test Executor#queryCursor
            runSafely(() -> {
                MappedStatement ms = getMappedStatement(MS_ID_USER_BY_ID);
                Connection connection = getConnection(executor);
                connection.close();
                executor.queryCursor(ms, null, DEFAULT);
            });

            // test Executor#createCacheKey
            runSafely(() -> {
                executor.createCacheKey(null, null, DEFAULT, null);
            });

            runSafely(() -> {
                executor.close(false);
                executor.createCacheKey(null, null, DEFAULT, null);
            });

        });

        doInExecutor(executor -> {
            // test Executor#getTransaction
            runSafely(() -> {
                executor.close(false);
                getConnection(executor);
            });
        });

        doInExecutor(executor -> {
            // test Executor#commit
            runSafely(() -> {
                executor.close(false);
                executor.commit(true);
            });
        });

        doInExecutor(executor -> {
            // test Executor#rollback
            runSafely(() -> {
                Connection connection = getConnection(executor);
                connection.close();
                executor.rollback(true);
            });
        });

        doInSqlSession(sqlSession -> {
            runSafely(() -> {
                sqlSession.close();
                deferLoadAfterResultHandler(sqlSession);
            });
        });

        doInMapper(UserMapper.class, userMapper -> {
            runSafely(() -> userMapper.getErrorUserByName("testing"));
        });

    }

    @Test
    void testIntercept() throws Throwable {
        doInExecutor(executor -> {
            Invocation invocation = new Invocation(executor, findMethod(Executor.class, "isClosed"), ofArray());
            InterceptingExecutorInterceptor interceptor = createInterceptingExecutorInterceptor();
            assertEquals(executor.isClosed(), interceptor.intercept(invocation));
        });
    }

    @Override
    protected void customize(Configuration configuration) {
        configuration.addInterceptor(createInterceptingExecutorInterceptor());
        configuration.addInterceptor(new InterceptingExecutorInterceptor(of(new LoggingExecutorFilter())));

        InterceptingExecutorInterceptor interceptor = new InterceptingExecutorInterceptor(of(), new ThrowingErrorExecutorInterceptor());
        Properties properties = new Properties();
        properties.setProperty(TEST_PROPERTY_KEY, this.getClass().getName());
        interceptor.setProperties(properties);

        configuration.addInterceptor(interceptor);
        configuration.addInterceptor(new InterceptingExecutorInterceptor(of(), new NoOpExecutorInterceptor()));
    }

    private InterceptingExecutorInterceptor createInterceptingExecutorInterceptor() {
        InterceptingExecutorInterceptor interceptor = new InterceptingExecutorInterceptor(of(new TestExecutorFilter()),
                new LoggingExecutorInterceptor(), new TestInterceptorContextExecutorInterceptor());
        return interceptor;
    }
}
