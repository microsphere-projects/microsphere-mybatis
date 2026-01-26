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
package io.microsphere.mybatis.executor;

import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.mybatis.plugin.InterceptorContext;
import io.microsphere.util.PriorityComparator;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.Assert.assertNoNullElements;
import static io.microsphere.util.Assert.assertNotEmpty;
import static io.microsphere.util.ClassUtils.getTypeName;
import static java.util.Arrays.sort;

/**
 * {@link ExecutorFilter} Adapter based on the one or more {@link ExecutorInterceptor interceptors}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ExecutorInterceptor
 * @see ExecutorFilter
 * @since 1.0.0
 */
public class InterceptorsExecutorFilterAdapter implements ExecutorFilter {

    private static final Logger logger = getLogger(InterceptorsExecutorFilterAdapter.class);

    private final ExecutorInterceptor[] executorInterceptors;

    private final int executorInterceptorsCount;

    public InterceptorsExecutorFilterAdapter(ExecutorInterceptor[] executorInterceptors) {
        assertNotEmpty(executorInterceptors, () -> "The ExecutorInterceptor array must not be empty");
        assertNoNullElements(executorInterceptors, () -> "Any element of interceptors must not be null!");
        this.executorInterceptors = executorInterceptors;
        this.executorInterceptorsCount = length(executorInterceptors);

        // sort by its priority
        sort(this.executorInterceptors, PriorityComparator.INSTANCE);
    }

    @Override
    public int update(MappedStatement ms, Object parameter, ExecutorFilterChain chain) throws SQLException {
        InterceptorContext<Executor> context = buildContext(chain);
        beforeUpdate(context, ms, parameter);
        Integer result = null;
        SQLException failure = null;
        try {
            result = chain.update(ms, parameter);
        } catch (SQLException e) {
            failure = e;
            throw e;
        } finally {
            afterUpdate(context, ms, parameter, result, failure);
        }
        return result;
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
                             CacheKey cacheKey, BoundSql boundSql, ExecutorFilterChain chain) throws SQLException {
        InterceptorContext<Executor> context = buildContext(chain);
        beforeQuery(context, ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
        List<E> result = null;
        SQLException failure = null;
        try {
            result = chain.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
        } catch (SQLException e) {
            failure = e;
            throw e;
        } finally {
            afterQuery(context, ms, parameter, rowBounds, resultHandler, cacheKey, boundSql, result, failure);
        }
        return result;
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
                             ExecutorFilterChain chain) throws SQLException {
        InterceptorContext<Executor> context = buildContext(chain);
        beforeQuery(context, ms, parameter, rowBounds, resultHandler, null, null);
        List<E> result = null;
        SQLException failure = null;
        try {
            result = chain.query(ms, parameter, rowBounds, resultHandler);
        } catch (SQLException e) {
            failure = e;
            throw e;
        } finally {
            afterQuery(context, ms, parameter, rowBounds, resultHandler, null, null, result, failure);
        }
        return result;
    }

    @Override
    public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds, ExecutorFilterChain chain) throws SQLException {
        InterceptorContext<Executor> context = buildContext(chain);

        beforeQueryCursor(context, ms, parameter, rowBounds);
        Cursor<E> result = null;
        SQLException failure = null;
        try {
            result = chain.queryCursor(ms, parameter, rowBounds);
        } catch (SQLException e) {
            failure = e;
            throw e;
        } finally {
            afterQueryCursor(context, ms, parameter, rowBounds, result, failure);
        }
        return result;
    }

    @Override
    public void commit(boolean required, ExecutorFilterChain chain) throws SQLException {
        InterceptorContext<Executor> context = buildContext(chain);
        beforeCommit(context, required);
        SQLException failure = null;
        try {
            chain.commit(required);
        } catch (SQLException e) {
            failure = e;
            throw e;
        } finally {
            afterCommit(context, required, failure);
        }
    }

    @Override
    public void rollback(boolean required, ExecutorFilterChain chain) throws SQLException {
        InterceptorContext<Executor> context = buildContext(chain);
        beforeRollback(context, required);
        SQLException failure = null;
        try {
            chain.rollback(required);
        } catch (SQLException e) {
            failure = e;
            throw e;
        } finally {
            afterRollback(context, required, failure);
        }
    }

    @Override
    public CacheKey createCacheKey(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql, ExecutorFilterChain chain) {
        InterceptorContext<Executor> context = buildContext(chain);
        beforeCreateCacheKey(context, ms, parameter, rowBounds, boundSql);
        CacheKey result = null;
        Throwable failure = null;
        try {
            result = chain.createCacheKey(ms, parameter, rowBounds, boundSql);
        } catch (Throwable e) {
            failure = e;
        } finally {
            afterCreateCacheKey(context, ms, parameter, rowBounds, boundSql, result, failure);
        }
        return result;
    }

    @Override
    public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType, ExecutorFilterChain chain) {
        InterceptorContext<Executor> context = buildContext(chain);
        beforeDeferLoad(context, ms, resultObject, property, key, targetType);
        Throwable failure = null;
        try {
            chain.deferLoad(ms, resultObject, property, key, targetType);
        } catch (Throwable e) {
            failure = e;
        } finally {
            afterDeferLoad(context, ms, resultObject, property, key, targetType, failure);
        }
    }

    @Override
    public Transaction getTransaction(ExecutorFilterChain chain) {
        InterceptorContext<Executor> context = buildContext(chain);
        beforeGetTransaction(context);
        Transaction transaction = null;
        Throwable failure = null;
        try {
            transaction = chain.getTransaction();
        } catch (Throwable e) {
            failure = e;
        } finally {
            afterGetTransaction(context, failure);
        }
        return transaction;
    }

    @Override
    public void close(boolean forceRollback, ExecutorFilterChain chain) {
        InterceptorContext<Executor> context = buildContext(chain);
        beforeClose(context, forceRollback);
        try {
            chain.close(forceRollback);
        } finally {
            afterClose(context, forceRollback);
        }
    }

    void beforeUpdate(InterceptorContext<Executor> context, MappedStatement ms, Object parameter) {
        iterate(executorInterceptor -> executorInterceptor.beforeUpdate(context, ms, parameter));
    }

    void afterUpdate(InterceptorContext<Executor> context, MappedStatement ms, Object parameter,
                     @Nullable Integer result, @Nullable SQLException failure) {
        iterate(executorInterceptor -> executorInterceptor.afterUpdate(context, ms, parameter, result, failure));
    }

    void beforeQuery(InterceptorContext<Executor> context, MappedStatement ms, Object parameter,
                     RowBounds rowBounds, ResultHandler resultHandler, @Nullable CacheKey cacheKey, @Nullable BoundSql boundSql) {
        iterate(executorInterceptor ->
                executorInterceptor.beforeQuery(context, ms, parameter, rowBounds, resultHandler, cacheKey, boundSql));
    }

    <E> void afterQuery(InterceptorContext<Executor> context, MappedStatement ms, Object parameter,
                        RowBounds rowBounds, ResultHandler resultHandler, @Nullable CacheKey cacheKey, @Nullable BoundSql boundSql,
                        @Nullable List<E> result, @Nullable SQLException failure) {
        iterate(executorInterceptor ->
                executorInterceptor.afterQuery(context, ms, parameter, rowBounds, resultHandler, cacheKey, boundSql, result, failure));
    }

    void beforeQueryCursor(InterceptorContext<Executor> context, MappedStatement ms, Object parameter, RowBounds rowBounds) {
        iterate(executorInterceptor ->
                executorInterceptor.beforeQueryCursor(context, ms, parameter, rowBounds));
    }

    <E> void afterQueryCursor(InterceptorContext<Executor> context, MappedStatement ms, Object parameter,
                              RowBounds rowBounds, @Nullable Cursor<E> result, @Nullable SQLException failure) {
        iterate(executorInterceptor ->
                executorInterceptor.afterQueryCursor(context, ms, parameter, rowBounds, result, failure));
    }

    void beforeCommit(InterceptorContext<Executor> context, boolean required) {
        iterate(executorInterceptor -> executorInterceptor.beforeCommit(context, required));
    }

    void afterCommit(InterceptorContext<Executor> context, boolean required, @Nullable SQLException failure) {
        iterate(executorInterceptor -> executorInterceptor.afterCommit(context, required, failure));
    }

    void beforeRollback(InterceptorContext<Executor> context, boolean required) {
        iterate(executorInterceptor -> executorInterceptor.beforeRollback(context, required));
    }

    void afterRollback(InterceptorContext<Executor> context, boolean required, @Nullable SQLException failure) {
        iterate(executorInterceptor -> executorInterceptor.afterRollback(context, required, failure));
    }

    void beforeGetTransaction(InterceptorContext<Executor> context) {
        iterate(executorInterceptor -> executorInterceptor.beforeGetTransaction(context));
    }

    void afterGetTransaction(InterceptorContext<Executor> context, @Nullable Throwable failure) {
        iterate(executorInterceptor -> executorInterceptor.afterGetTransaction(context, failure));
    }

    void beforeCreateCacheKey(InterceptorContext<Executor> context, MappedStatement ms, Object parameter,
                              RowBounds rowBounds, BoundSql boundSql) {
        iterate(executorInterceptor ->
                executorInterceptor.beforeCreateCacheKey(context, ms, parameter, rowBounds, boundSql));
    }

    void afterCreateCacheKey(InterceptorContext<Executor> context, MappedStatement ms, Object parameter,
                             RowBounds rowBounds, BoundSql boundSql, @Nullable CacheKey result, @Nullable Throwable failure) {
        iterate(executorInterceptor ->
                executorInterceptor.afterCreateCacheKey(context, ms, parameter, rowBounds, boundSql, result, failure));
    }

    void beforeDeferLoad(InterceptorContext<Executor> context, MappedStatement ms, MetaObject resultObject, String property,
                         CacheKey key, Class<?> targetType) {
        iterate(executorInterceptor ->
                executorInterceptor.beforeDeferLoad(context, ms, resultObject, property, key, targetType));
    }

    void afterDeferLoad(InterceptorContext<Executor> context, MappedStatement ms, MetaObject resultObject,
                        String property, CacheKey key, Class<?> targetType, @Nullable Throwable failure) {
        iterate(executorInterceptor ->
                executorInterceptor.afterDeferLoad(context, ms, resultObject, property, key, targetType, failure));
    }

    void beforeClose(InterceptorContext<Executor> context, boolean forceRollback) {
        iterate(executorInterceptor -> executorInterceptor.beforeClose(context, forceRollback));
    }

    void afterClose(InterceptorContext<Executor> context, boolean forceRollback) {
        iterate(executorInterceptor -> executorInterceptor.afterClose(context, forceRollback));
    }

    void iterate(Consumer<ExecutorInterceptor> executorInterceptorConsumer) {
        for (int i = 0; i < executorInterceptorsCount; i++) {
            ExecutorInterceptor executorInterceptor = executorInterceptors[i];
            try {
                executorInterceptorConsumer.accept(executorInterceptor);
            } catch (Throwable e) {
                logger.warn("Failed to execute ExecutorInterceptor[index : {} , class : {}]", i,
                        getTypeName(executorInterceptor), e);
            }
        }
    }

    private InterceptorContext<Executor> buildContext(ExecutorFilterChain chain) {
        return new InterceptorContext<>(chain.getExecutor(), chain.getProperties());
    }
}
