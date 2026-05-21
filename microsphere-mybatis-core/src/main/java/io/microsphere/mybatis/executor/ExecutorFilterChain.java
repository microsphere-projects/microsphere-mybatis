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
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Build a chain manually (normally done by InterceptingExecutor)
 *   Executor delegate = configuration.newExecutor(transaction);
 *   Properties properties = new Properties();
 *   ExecutorFilter filter1 = new LoggingExecutorFilter();
 *   ExecutorFilterChain chain = new ExecutorFilterChain(delegate, properties, filter1);
 *
 *   // Execute an update through the filter chain
 *   int rows = chain.update(mappedStatement, parameter);
 * }</pre>
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

    /**
     * Construct a new {@link ExecutorFilterChain}.
     *
     * @param executor        the target {@link Executor} invoked after all filters; must not be {@code null}
     * @param properties      optional {@link Properties}; may be {@code null}
     * @param executorFilters one or more {@link ExecutorFilter} instances; must not be empty
     */
    public ExecutorFilterChain(Executor executor, @Nullable Properties properties, ExecutorFilter... executorFilters) {
        assertNotNull(executor, () -> "The 'executor' must not be null!");
        assertNotEmpty(executorFilters, () -> "The 'executorFilters' must not be empty!");
        this.executor = executor;
        this.properties = properties;
        this.filters = executorFilters;
        this.size = length(executorFilters);
        this.position = 0;
    }

    /**
     * Execute {@link Executor#update(MappedStatement, Object)} through the filter chain.
     *
     * @param ms        {@link MappedStatement}
     * @param parameter the parameter object
     * @return the number of rows affected
     * @throws SQLException if the update fails
     */
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        return applySQL(f -> f.update(ms, parameter, this), e -> e.update(ms, parameter));
    }

    /**
     * Execute {@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler, CacheKey, BoundSql)}
     * through the filter chain.
     *
     * @param ms            {@link MappedStatement}
     * @param parameter     the parameter object
     * @param rowBounds     {@link RowBounds}
     * @param resultHandler {@link ResultHandler}
     * @param cacheKey      {@link CacheKey}
     * @param boundSql      {@link BoundSql}
     * @param <E>           the element type of the result list
     * @return the result list
     * @throws SQLException if the query fails
     */
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException {
        return applySQL(f -> f.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql, this),
                e -> e.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql));
    }

    /**
     * Execute {@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler)}
     * through the filter chain.
     *
     * @param ms            {@link MappedStatement}
     * @param parameter     the parameter object
     * @param rowBounds     {@link RowBounds}
     * @param resultHandler {@link ResultHandler}
     * @param <E>           the element type of the result list
     * @return the result list
     * @throws SQLException if the query fails
     */
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        return applySQL(f -> f.query(ms, parameter, rowBounds, resultHandler, this),
                e -> e.query(ms, parameter, rowBounds, resultHandler));
    }

    /**
     * Execute {@link Executor#queryCursor(MappedStatement, Object, RowBounds)}
     * through the filter chain.
     *
     * @param ms        {@link MappedStatement}
     * @param parameter the parameter object
     * @param rowBounds {@link RowBounds}
     * @param <E>       the element type of the cursor
     * @return the {@link Cursor}
     * @throws SQLException if the query fails
     */
    public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
        return applySQL(f -> f.queryCursor(ms, parameter, rowBounds, this),
                e -> e.queryCursor(ms, parameter, rowBounds));
    }

    /**
     * Execute {@link Executor#commit(boolean)} through the filter chain.
     *
     * @param required {@code true} if the commit must be performed
     * @throws SQLException if the commit fails
     */
    public void commit(boolean required) throws SQLException {
        consumeSQL(f -> f.commit(required, this),
                e -> e.commit(required));
    }

    /**
     * Execute {@link Executor#rollback(boolean)} through the filter chain.
     *
     * @param required {@code true} if the rollback must be performed
     * @throws SQLException if the rollback fails
     */
    public void rollback(boolean required) throws SQLException {
        consumeSQL(f -> f.rollback(required, this),
                e -> e.rollback(required));
    }

    /**
     * Execute {@link Executor#createCacheKey(MappedStatement, Object, RowBounds, BoundSql)}
     * through the filter chain.
     *
     * @param ms        {@link MappedStatement}
     * @param parameter the parameter object
     * @param rowBounds {@link RowBounds}
     * @param boundSql  {@link BoundSql}
     * @return the {@link CacheKey}
     */
    public CacheKey createCacheKey(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) {
        return apply(f -> f.createCacheKey(ms, parameter, rowBounds, boundSql, this),
                e -> e.createCacheKey(ms, parameter, rowBounds, boundSql));
    }

    /**
     * Execute {@link Executor#deferLoad(MappedStatement, MetaObject, String, CacheKey, Class)}
     * through the filter chain.
     *
     * @param ms           {@link MappedStatement}
     * @param resultObject {@link MetaObject}
     * @param property     the property name
     * @param key          {@link CacheKey}
     * @param targetType   the target type
     */
    public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
        consume(f -> f.deferLoad(ms, resultObject, property, key, targetType, this),
                e -> e.deferLoad(ms, resultObject, property, key, targetType));
    }

    /**
     * Execute {@link Executor#getTransaction()} through the filter chain.
     *
     * @return the current {@link Transaction}
     */
    public Transaction getTransaction() {
        return apply(f -> f.getTransaction(this), Executor::getTransaction);
    }


    /**
     * Execute {@link Executor#close(boolean)} through the filter chain.
     *
     * @param forceRollback {@code true} to force a rollback before closing
     */
    public void close(boolean forceRollback) {
        consume(f -> f.close(forceRollback, this), e -> e.close(forceRollback));
    }

    /**
     * Consume (void) an {@link ExecutorFilter} operation, falling back to the {@link Executor} when
     * no more filters remain.  Any checked exception thrown by the consumers is wrapped into a
     * {@link RuntimeException}.
     *
     * @param filterConsumer   consumer applied to the next {@link ExecutorFilter}
     * @param executorConsumer consumer applied to the delegate {@link Executor} at the end of the chain
     */
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

    /**
     * Consume (void) an {@link ExecutorFilter} operation that may throw {@link SQLException}, falling
     * back to the {@link Executor} when no more filters remain.
     *
     * @param filterConsumer   consumer applied to the next {@link ExecutorFilter}
     * @param executorConsumer consumer applied to the delegate {@link Executor} at the end of the chain
     * @throws SQLException if either consumer throws a {@link SQLException}
     */
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

    /**
     * Apply (return-value) an {@link ExecutorFilter} function, falling back to the {@link Executor}
     * when no more filters remain.  Any checked exception is wrapped into a {@link RuntimeException}.
     *
     * @param filterFunction   function applied to the next {@link ExecutorFilter}
     * @param executorFunction function applied to the delegate {@link Executor} at the end of the chain
     * @param <R>              the return type
     * @return the result
     */
    protected <R> R apply(ThrowableFunction<ExecutorFilter, R> filterFunction,
                          ThrowableFunction<Executor, R> executorFunction) {
        return process(filterFunction, executorFunction, e -> wrap(e, RuntimeException.class));
    }

    /**
     * Apply (return-value) an {@link ExecutorFilter} function that may throw {@link SQLException},
     * falling back to the {@link Executor} when no more filters remain.
     *
     * @param filterFunction   function applied to the next {@link ExecutorFilter}
     * @param executorFunction function applied to the delegate {@link Executor} at the end of the chain
     * @param <R>              the return type
     * @return the result
     * @throws SQLException if either function throws a {@link SQLException}
     */
    protected <R> R applySQL(ThrowableFunction<ExecutorFilter, R> filterFunction,
                             ThrowableFunction<Executor, R> executorFunction) throws SQLException {
        return process(filterFunction, executorFunction, e -> wrap(e, SQLException.class));
    }

    /**
     * Core routing method: if there is a next filter it is invoked, otherwise the delegate
     * {@link Executor} is called.  Exceptions are converted using the supplied {@code failureHandler}.
     *
     * @param filterFunction   function applied to the next {@link ExecutorFilter}
     * @param executorFunction function applied to the delegate {@link Executor} at the end of the chain
     * @param failureHandler   converts any thrown {@link Throwable} into the declared exception type
     * @param <E>              the declared exception type
     * @param <R>              the return type
     * @return the result
     * @throws E if either function or the failure handler throws
     */
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
