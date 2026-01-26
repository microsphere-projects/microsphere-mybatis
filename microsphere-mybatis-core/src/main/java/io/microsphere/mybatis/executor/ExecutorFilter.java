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

import io.microsphere.lang.Prioritized;
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

/**
 * The invocation handler of {@link Executor}, these methods will be filtered:
 * <ul>
 *     <li>{@link Executor#update(MappedStatement, Object)}</li>
 *     <li>{@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler, CacheKey, BoundSql)}</li>
 *     <li>{@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler)}</li>
 *     <li>{@link Executor#queryCursor(MappedStatement, Object, RowBounds)}</li>
 *     <li>{@link Executor#commit(boolean)}</li>
 *     <li>{@link Executor#rollback(boolean)}</li>
 *     <li>{@link Executor#createCacheKey(MappedStatement, Object, RowBounds, BoundSql)}</li>
 *     <li>{@link Executor#deferLoad(MappedStatement, MetaObject, String, CacheKey, Class)}</li>
 *     <li>{@link Executor#getTransaction()}</li>
 *     <li>{@link Executor#close(boolean)}</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Executor
 * @since 1.0.0
 */
public interface ExecutorFilter extends Prioritized {

    /**
     * Filter {@link Executor#update(MappedStatement, Object)}
     *
     * @param ms        {@link MappedStatement}
     * @param parameter the parameter of {@link MappedStatement}
     * @param chain     {@link ExecutorFilterChain}
     * @return
     * @throws SQLException
     */
    default int update(MappedStatement ms, Object parameter, ExecutorFilterChain chain) throws SQLException {
        return chain.update(ms, parameter);
    }

    /**
     * Filer {@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler, CacheKey, BoundSql)}
     *
     * @param ms            {@link MappedStatement}
     * @param parameter     the parameter of {@link MappedStatement}
     * @param rowBounds     {@link RowBounds}
     * @param resultHandler {@link ResultHandler}
     * @param cacheKey      {@link CacheKey}
     * @param boundSql      {@link BoundSql}
     * @param chain         {@link ExecutorFilterChain}
     * @param <E>           the type of multiple elements
     * @return
     * @throws SQLException
     */
    default <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
                              CacheKey cacheKey, BoundSql boundSql, ExecutorFilterChain chain) throws SQLException {
        return chain.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
    }

    /**
     * Filter {@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler)}
     *
     * @param ms            {@link MappedStatement}
     * @param parameter     the parameter of {@link MappedStatement}
     * @param rowBounds     {@link RowBounds}
     * @param resultHandler {@link ResultHandler}
     * @param chain         {@link ExecutorFilterChain}
     * @param <E>           the type of multiple elements
     * @return
     * @throws SQLException
     */
    default <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
                              ExecutorFilterChain chain) throws SQLException {
        return chain.query(ms, parameter, rowBounds, resultHandler);
    }

    /**
     * Filter {@link Executor#queryCursor(MappedStatement, Object, RowBounds)}
     *
     * @param ms        {@link MappedStatement}
     * @param parameter the parameter of {@link MappedStatement}
     * @param rowBounds {@link RowBounds}
     * @param chain     {@link ExecutorFilterChain}
     * @param <E>       the type of multiple elements
     * @return
     * @throws SQLException
     */
    default <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds,
                                      ExecutorFilterChain chain) throws SQLException {
        return chain.queryCursor(ms, parameter, rowBounds);
    }

    /**
     * Filter {@link Executor#commit(boolean)}
     *
     * @param required
     * @param chain    {@link ExecutorFilterChain}
     * @throws SQLException
     */
    default void commit(boolean required, ExecutorFilterChain chain) throws SQLException {
        chain.commit(required);
    }

    /**
     * Filter {@link Executor#rollback(boolean)}
     *
     * @param required
     * @param chain    {@link ExecutorFilterChain}
     * @throws SQLException
     */
    default void rollback(boolean required, ExecutorFilterChain chain) throws SQLException {
        chain.rollback(required);
    }

    /**
     * Filter {@link Executor#createCacheKey(MappedStatement, Object, RowBounds, BoundSql)}
     *
     * @param ms        {@link MappedStatement}
     * @param parameter the parameter of {@link MappedStatement}
     * @param rowBounds {@link RowBounds}
     * @param boundSql  {@link BoundSql}
     * @param chain     {@link ExecutorFilterChain}
     * @return
     */
    default CacheKey createCacheKey(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql,
                                    ExecutorFilterChain chain) {
        return chain.createCacheKey(ms, parameter, rowBounds, boundSql);
    }

    /**
     * Filter {@link Executor#deferLoad(MappedStatement, MetaObject, String, CacheKey, Class)}
     *
     * @param ms           {@link MappedStatement}
     * @param resultObject
     * @param property
     * @param key
     * @param targetType
     * @param chain        {@link ExecutorFilterChain}
     */
    default void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType,
                           ExecutorFilterChain chain) {
        chain.deferLoad(ms, resultObject, property, key, targetType);
    }

    /**
     * Filter {@link Executor#getTransaction()}
     *
     * @param chain {@link ExecutorFilterChain}
     * @return
     */
    default Transaction getTransaction(ExecutorFilterChain chain) {
        return chain.getTransaction();
    }

    /**
     * Filter {@link Executor#close(boolean)}
     *
     * @param forceRollback force to rollback the transaction or not
     * @param chain         {@link ExecutorFilterChain}
     */
    default void close(boolean forceRollback, ExecutorFilterChain chain) {
        chain.close(forceRollback);
    }
}
