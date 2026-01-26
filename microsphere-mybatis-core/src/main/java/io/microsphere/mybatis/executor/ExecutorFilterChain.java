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

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.lang.function.ThrowableConsumer;
import io.microsphere.lang.function.ThrowableFunction;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.Assert.assertNotEmpty;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.ExceptionUtils.wrap;

/**
 * The chain of {@link ExecutorFilter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Executor
 * @since 1.0.0
 */
public class ExecutorFilterChain {

    private final Executor executor;

    @Nullable
    private final Properties properties;

    private final ExecutorFilter[] filters;

    private final int size;

    private int position;

    public ExecutorFilterChain(Executor executor, @Nullable Properties properties, ExecutorFilter... executorFilters) {
        assertNotNull(executor, () -> "The 'executor' must not be null!");
        assertNotEmpty(executorFilters, () -> "The 'executorFilters' must not be empty!");
        this.executor = executor;
        this.properties = properties;
        this.filters = executorFilters;
        this.size = length(executorFilters);
        this.position = 0;
    }

    public int update(MappedStatement ms, Object parameter) throws SQLException {
        return applySQL(f -> f.update(ms, parameter, this), e -> e.update(ms, parameter));
    }

    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException {
        return applySQL(f -> f.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql, this),
                e -> e.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql));
    }

    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        return applySQL(f -> f.query(ms, parameter, rowBounds, resultHandler, this),
                e -> e.query(ms, parameter, rowBounds, resultHandler));
    }

    public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
        return applySQL(f -> f.queryCursor(ms, parameter, rowBounds, this),
                e -> e.queryCursor(ms, parameter, rowBounds));
    }

    public void commit(boolean required) throws SQLException {
        consumeSQL(f -> f.commit(required, this),
                e -> e.commit(required));
    }

    public void rollback(boolean required) throws SQLException {
        consumeSQL(f -> f.rollback(required, this),
                e -> e.rollback(required));
    }

    public CacheKey createCacheKey(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) {
        return apply(f -> f.createCacheKey(ms, parameter, rowBounds, boundSql, this),
                e -> e.createCacheKey(ms, parameter, rowBounds, boundSql));
    }

    public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
        consume(f -> f.deferLoad(ms, resultObject, property, key, targetType, this),
                e -> e.deferLoad(ms, resultObject, property, key, targetType));
    }

    public Transaction getTransaction() {
        return apply(f -> f.getTransaction(this), Executor::getTransaction);
    }


    public void close(boolean forceRollback) {
        consume(f -> f.close(forceRollback, this), e -> e.close(forceRollback));
    }

    protected void consume(ThrowableConsumer<ExecutorFilter> filterConsumer,
                           ThrowableConsumer<Executor> executorConsumer) {
        apply(f -> {
            filterConsumer.accept(f);
            return null;
        }, e -> {
            executorConsumer.accept(e);
            return null;
        });
    }

    protected void consumeSQL(ThrowableConsumer<ExecutorFilter> filterConsumer,
                              ThrowableConsumer<Executor> executorConsumer) throws SQLException {
        applySQL(f -> {
            filterConsumer.accept(f);
            return null;
        }, e -> {
            executorConsumer.accept(e);
            return null;
        });
    }

    protected <R> R apply(ThrowableFunction<ExecutorFilter, R> filterFunction,
                          ThrowableFunction<Executor, R> executorFunction) {
        return process(filterFunction, executorFunction, e -> wrap(e, RuntimeException.class));
    }

    protected <R> R applySQL(ThrowableFunction<ExecutorFilter, R> filterFunction,
                             ThrowableFunction<Executor, R> executorFunction) throws SQLException {
        return process(filterFunction, executorFunction, e -> wrap(e, SQLException.class));
    }

    protected <E extends Throwable, R> R process(ThrowableFunction<ExecutorFilter, R> filterFunction,
                                                 ThrowableFunction<Executor, R> executorFunction,
                                                 Function<Throwable, E> failureHandler) throws E {
        final R result;
        try {
            if (position < size) {
                result = filterFunction.apply(filters[position++]);
            } else {
                result = executorFunction.apply(this.executor);
            }
        } catch (Throwable failure) {
            throw failureHandler.apply(failure);
        }
        return result;
    }

    /**
     * Get the {@link Executor}
     *
     * @return non-null
     */
    @Nonnull
    public Executor getExecutor() {
        return this.executor;
    }

    /**
     * Get the reference of {@link Interceptor#setProperties(Properties)}
     *
     * @return <code>null</code> if {@link Interceptor#setProperties(Properties)} was not set
     */
    @Nullable
    public Properties getProperties() {
        return this.properties;
    }

    /**
     * Get the copy of {@link ExecutorFilter filters}
     *
     * @return non-null
     */
    @Nonnull
    public ExecutorFilter[] getFilters() {
        return this.filters.clone();
    }

    /**
     * Get the size of {@link ExecutorFilter filters}
     *
     * @return positive integer(exclude 0)
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Get the current position of {@link ExecutorFilter filters}
     *
     * @return positive integer(include 0)
     */
    public int getPosition() {
        return this.position;
    }

    @Override
    public String toString() {
        return "ExecutorFilterChain{" +
                "executor=" + this.executor +
                ", properties=" + this.properties +
                ", filters=" + Arrays.toString(this.filters) +
                ", size=" + this.size +
                ", position=" + this.position +
                '}';
    }
}
