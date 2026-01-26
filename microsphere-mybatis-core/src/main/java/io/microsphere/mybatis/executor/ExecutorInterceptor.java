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
import io.microsphere.lang.Prioritized;
import io.microsphere.mybatis.plugin.InterceptorContext;
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
 * The Interceptor of {@link Executor}, these methods will be intercepted:
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
public interface ExecutorInterceptor extends Prioritized {

    /**
     * Callback before execute {@link Executor#update(MappedStatement, Object)}
     *
     * @param context   {@link InterceptorContext}
     * @param ms        {@link MappedStatement}
     * @param parameter the parameter object
     */
    default void beforeUpdate(InterceptorContext<Executor> context, MappedStatement ms, Object parameter) {
    }

    /**
     * Callback after execute {@link Executor#update(MappedStatement, Object)}
     *
     * @param context   {@link InterceptorContext}
     * @param ms        {@link MappedStatement}
     * @param parameter the parameter object
     * @param result    (optional) the result of {@link Executor#update(MappedStatement, Object)}
     * @param failure   (optional) the {@link SQLException} if occurred
     */
    default void afterUpdate(InterceptorContext<Executor> context, MappedStatement ms, Object parameter,
                             @Nullable Integer result, @Nullable SQLException failure) {
    }

    /**
     * Callback before execute {@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler)} or
     * {@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler, CacheKey, BoundSql)}
     *
     * @param context       {@link InterceptorContext}
     * @param ms            {@link MappedStatement}
     * @param parameter     the parameter object
     * @param rowBounds     {@link RowBounds}
     * @param resultHandler {@link ResultHandler}
     * @param cacheKey      (optional) {@link CacheKey}
     * @param boundSql      (optional) {@link BoundSql}
     */
    default void beforeQuery(InterceptorContext<Executor> context, MappedStatement ms, Object parameter,
                             RowBounds rowBounds, ResultHandler resultHandler, @Nullable CacheKey cacheKey, @Nullable BoundSql boundSql) {
    }

    /**
     * Callback after execute {@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler)} or
     * {@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler, CacheKey, BoundSql)}
     *
     * @param context       {@link InterceptorContext}
     * @param ms            {@link MappedStatement}
     * @param parameter     the parameter object
     * @param rowBounds     {@link RowBounds}
     * @param resultHandler {@link ResultHandler}
     * @param cacheKey      (optional) {@link CacheKey}
     * @param boundSql      (optional) {@link BoundSql}
     * @param result        (optional) the result of {@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler)} or
     *                      {@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler, CacheKey, BoundSql)}
     * @param failure       (optional) the {@link SQLException} if occurred
     * @param <E>           the type of result
     */
    default <E> void afterQuery(InterceptorContext<Executor> context, MappedStatement ms, Object parameter,
                                RowBounds rowBounds, ResultHandler resultHandler, @Nullable CacheKey cacheKey,
                                @Nullable BoundSql boundSql, @Nullable List<E> result, @Nullable SQLException failure) {
    }

    /**
     * Callback before execute {@link Executor#queryCursor(MappedStatement, Object, RowBounds)}
     *
     * @param context   {@link InterceptorContext}
     * @param ms        {@link MappedStatement}
     * @param parameter the parameter object
     * @param rowBounds {@link RowBounds}
     */
    default void beforeQueryCursor(InterceptorContext<Executor> context, MappedStatement ms, Object parameter, RowBounds rowBounds) {
    }

    /**
     * Callback after execute {@link Executor#queryCursor(MappedStatement, Object, RowBounds)}
     *
     * @param context   {@link InterceptorContext}
     * @param ms        {@link MappedStatement}
     * @param parameter the parameter object
     * @param rowBounds {@link RowBounds}
     * @param result    (optional) the result of {@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler)} or
     *                  {@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler, CacheKey, BoundSql)}
     * @param failure   (optional) the {@link SQLException} if occurred
     * @param <E>       the type of result
     */
    default <E> void afterQueryCursor(InterceptorContext<Executor> context, MappedStatement ms, Object parameter,
                                      RowBounds rowBounds, @Nullable Cursor<E> result, @Nullable SQLException failure) {
    }

    /**
     * Callback before execute {@link Executor#commit(boolean)}
     *
     * @param context  {@link InterceptorContext}
     * @param required <code>true</code> means the transaction will be {@link Transaction#commit() committed} really,
     *                 otherwise ignored
     */
    default void beforeCommit(InterceptorContext<Executor> context, boolean required) {
    }

    /**
     * Callback after execute {@link Executor#commit(boolean)}
     *
     * @param context  {@link InterceptorContext}
     * @param required <code>true</code> means the transaction will be {@link Transaction#commit() committed} really,
     *                 otherwise ignored
     * @param failure  (optional) the {@link SQLException} if occurred
     */
    default void afterCommit(InterceptorContext<Executor> context, boolean required, @Nullable SQLException failure) {
    }

    /**
     * Callback before execute {@link Executor#rollback(boolean)}
     *
     * @param context  {@link InterceptorContext}
     * @param required <code>true</code> means the transaction will be {@link Transaction#rollback() rollback}, otherwise ignored
     */
    default void beforeRollback(InterceptorContext<Executor> context, boolean required) {
    }

    /**
     * Callback after execute {@link Executor#rollback(boolean)}
     *
     * @param context  {@link InterceptorContext}
     * @param required <code>true</code> means the transaction will be {@link Transaction#rollback() rollback}, otherwise ignored
     * @param failure  (optional) the {@link SQLException} if occurred
     */
    default void afterRollback(InterceptorContext<Executor> context, boolean required, @Nullable SQLException failure) {
    }

    /**
     * Callback before execute {@link Executor#getTransaction()}
     *
     * @param context {@link InterceptorContext}
     */
    default void beforeGetTransaction(InterceptorContext<Executor> context) {
    }

    /**
     * Callback after execute {@link Executor#getTransaction()}
     *
     * @param context {@link InterceptorContext}
     * @param failure (optional) the {@link Throwable} occurred when the transaction was gotten
     */
    default void afterGetTransaction(InterceptorContext<Executor> context, @Nullable Throwable failure) {
    }

    /**
     * Callback before execute {@link Executor#createCacheKey(MappedStatement, Object, RowBounds, BoundSql)}
     *
     * @param context   {@link InterceptorContext}
     * @param ms        {@link MappedStatement}
     * @param parameter the parameter object
     * @param rowBounds {@link RowBounds}
     * @param boundSql  {@link BoundSql}
     */
    default void beforeCreateCacheKey(InterceptorContext<Executor> context, MappedStatement ms, Object parameter,
                                      RowBounds rowBounds, BoundSql boundSql) {
    }

    /**
     * Callback after execute {@link Executor#createCacheKey(MappedStatement, Object, RowBounds, BoundSql)}
     *
     * @param context   {@link InterceptorContext}
     * @param ms        {@link MappedStatement}
     * @param parameter the parameter object
     * @param rowBounds {@link RowBounds}
     * @param boundSql  {@link BoundSql}
     * @param key       {@link CacheKey}
     * @param failure   (optional) the {@link Throwable} occurred when the cache key was created
     */
    default void afterCreateCacheKey(InterceptorContext<Executor> context, MappedStatement ms, Object parameter,
                                     RowBounds rowBounds, BoundSql boundSql, @Nullable CacheKey key, @Nullable Throwable failure) {
    }

    /**
     * Callback before execute {@link Executor#deferLoad(MappedStatement, MetaObject, String, CacheKey, Class)}
     *
     * @param context      {@link InterceptorContext}
     * @param ms           {@link MappedStatement}
     * @param resultObject {@link MetaObject}
     * @param property     {@link RowBounds}
     * @param key          {@link CacheKey}
     * @param targetType   the target type
     */
    default void beforeDeferLoad(InterceptorContext<Executor> context, MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
    }

    /**
     * Callback after execute {@link Executor#deferLoad(MappedStatement, MetaObject, String, CacheKey, Class)}
     *
     * @param context      {@link InterceptorContext}
     * @param ms           {@link MappedStatement}
     * @param resultObject {@link MetaObject}
     * @param property     {@link RowBounds}
     * @param key          {@link CacheKey}
     * @param targetType   the target type
     * @param failure      (optional) the {@link Throwable} occurred if the cache key was created
     */
    default void afterDeferLoad(InterceptorContext<Executor> context, MappedStatement ms, MetaObject resultObject,
                                String property, CacheKey key, Class<?> targetType, @Nullable Throwable failure) {
    }

    /**
     * Callback before execute {@link Executor#close(boolean)}
     *
     * @param context       {@link InterceptorContext}
     * @param forceRollback <code>true</code> means the transaction will be {@link Transaction#rollback() rollback}
     */
    default void beforeClose(InterceptorContext<Executor> context, boolean forceRollback) {
    }

    /**
     * Callback after execute {@link Executor#close(boolean)}
     *
     * @param context       {@link InterceptorContext}
     * @param forceRollback <code>true</code> means the transaction will be {@link Transaction#rollback() rollback}
     */
    default void afterClose(InterceptorContext<Executor> context, boolean forceRollback) {
    }
}
