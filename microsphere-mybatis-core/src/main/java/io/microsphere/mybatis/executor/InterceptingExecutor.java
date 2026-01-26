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

import io.microsphere.logging.Logger;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static io.microsphere.util.Assert.assertNotNull;

/**
 * Delegating {@link Executor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Executor
 * @see ExecutorInterceptor
 * @since 1.0.0
 */
public class InterceptingExecutor implements Executor {

    private static final Logger logger = getLogger(InterceptingExecutor.class);

    private final Executor delegate;

    private final Properties properties;

    private final ExecutorFilter[] executorFilters;

    public InterceptingExecutor(Executor delegate, Properties properties, ExecutorFilter... executorFilters) {
        assertNotNull(delegate, () -> "The 'delegate' argument must not be null");
        assertNotNull(executorFilters, () -> "The 'executorFilters' argument must not be null");
        this.delegate = delegate;
        this.properties = properties;
        this.executorFilters = executorFilters;
        logger.trace(this.toString());
    }

    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        ExecutorFilterChain chain = buildChain();
        return chain.update(ms, parameter);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
                             CacheKey cacheKey, BoundSql boundSql) throws SQLException {
        ExecutorFilterChain chain = buildChain();
        return chain.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        ExecutorFilterChain chain = buildChain();
        return chain.query(ms, parameter, rowBounds, resultHandler);
    }

    @Override
    public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
        ExecutorFilterChain chain = buildChain();
        return chain.queryCursor(ms, parameter, rowBounds);
    }

    @Override
    public List<BatchResult> flushStatements() throws SQLException {
        return delegate.flushStatements();
    }

    @Override
    public void commit(boolean required) throws SQLException {
        ExecutorFilterChain chain = buildChain();
        chain.commit(required);
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        ExecutorFilterChain chain = buildChain();
        chain.rollback(required);
    }

    @Override
    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
        ExecutorFilterChain chain = buildChain();
        return chain.createCacheKey(ms, parameterObject, rowBounds, boundSql);
    }

    @Override
    public boolean isCached(MappedStatement ms, CacheKey key) {
        return delegate.isCached(ms, key);
    }

    @Override
    public void clearLocalCache() {
        delegate.clearLocalCache();
    }

    @Override
    public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
        ExecutorFilterChain chain = buildChain();
        chain.deferLoad(ms, resultObject, property, key, targetType);
    }

    @Override
    public Transaction getTransaction() {
        ExecutorFilterChain chain = buildChain();
        return chain.getTransaction();
    }

    @Override
    public void close(boolean forceRollback) {
        ExecutorFilterChain chain = buildChain();
        chain.close(forceRollback);
    }

    @Override
    public boolean isClosed() {
        return delegate.isClosed();
    }

    @Override
    public void setExecutorWrapper(Executor executor) {
        if (executor instanceof InterceptingExecutor) {
            return;
        }
        InterceptingExecutor interceptingExecutor = new InterceptingExecutor(executor, this.properties, this.executorFilters);
        delegate.setExecutorWrapper(interceptingExecutor);
    }

    ExecutorFilterChain buildChain() {
        return new ExecutorFilterChain(this.delegate, this.properties, this.executorFilters);
    }

    public Executor getDelegate() {
        return delegate;
    }

    public Properties getProperties() {
        return properties;
    }

    public ExecutorFilter[] getExecutorFilters() {
        return executorFilters;
    }

    @Override
    public String toString() {
        return "InterceptingExecutor{" +
                "delegate=" + delegate +
                ", properties=" + properties +
                ", executorFilters=" + arrayToString(executorFilters) +
                '}';
    }
}
