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
import io.microsphere.mybatis.plugin.InterceptorContext;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.List;

import static io.microsphere.mybatis.executor.ThrowingErrorExecutorFilter.throwsError;

/**
 * {@link ExecutorInterceptor} class throws the errors.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ExecutorInterceptor
 * @since 1.0.0
 */
public class ThrowingErrorExecutorInterceptor implements ExecutorInterceptor {

    @Override
    public void beforeUpdate(InterceptorContext<Executor> context, MappedStatement ms, Object parameter) {
        throwsError();
        ExecutorInterceptor.super.beforeUpdate(context, ms, parameter);
    }

    @Override
    public void afterUpdate(InterceptorContext<Executor> context, MappedStatement ms, Object parameter, @Nullable Integer result, @Nullable SQLException failure) {
        throwsError();
        ExecutorInterceptor.super.afterUpdate(context, ms, parameter, result, failure);
    }

    @Override
    public void beforeQuery(InterceptorContext<Executor> context, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, @Nullable CacheKey cacheKey, @Nullable BoundSql boundSql) {
        throwsError();
        ExecutorInterceptor.super.beforeQuery(context, ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
    }

    @Override
    public <E> void afterQuery(InterceptorContext<Executor> context, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, @Nullable CacheKey cacheKey, @Nullable BoundSql boundSql, @Nullable List<E> result, @Nullable SQLException failure) {
        throwsError();
        ExecutorInterceptor.super.afterQuery(context, ms, parameter, rowBounds, resultHandler, cacheKey, boundSql, result, failure);
    }

    @Override
    public void beforeQueryCursor(InterceptorContext<Executor> context, MappedStatement ms, Object parameter, RowBounds rowBounds) {
        throwsError();
        ExecutorInterceptor.super.beforeQueryCursor(context, ms, parameter, rowBounds);
    }

    @Override
    public <E> void afterQueryCursor(InterceptorContext<Executor> context, MappedStatement ms, Object parameter, RowBounds rowBounds, @Nullable Cursor<E> result, @Nullable SQLException failure) {
        throwsError();
        ExecutorInterceptor.super.afterQueryCursor(context, ms, parameter, rowBounds, result, failure);
    }

    @Override
    public void beforeCommit(InterceptorContext<Executor> context, boolean required) {
        throwsError();
        ExecutorInterceptor.super.beforeCommit(context, required);
    }

    @Override
    public void afterCommit(InterceptorContext<Executor> context, boolean required, @Nullable SQLException failure) {
        throwsError();
        ExecutorInterceptor.super.afterCommit(context, required, failure);
    }

    @Override
    public void beforeRollback(InterceptorContext<Executor> context, boolean required) {
        throwsError();
        ExecutorInterceptor.super.beforeRollback(context, required);
    }

    @Override
    public void afterRollback(InterceptorContext<Executor> context, boolean required, @Nullable SQLException failure) {
        throwsError();
        ExecutorInterceptor.super.afterRollback(context, required, failure);
    }

    @Override
    public void beforeGetTransaction(InterceptorContext<Executor> context) {
        throwsError();
        ExecutorInterceptor.super.beforeGetTransaction(context);
    }

    @Override
    public void afterGetTransaction(InterceptorContext<Executor> context, @Nullable Throwable failure) {
        throwsError();
        ExecutorInterceptor.super.afterGetTransaction(context, failure);
    }

    @Override
    public void beforeCreateCacheKey(InterceptorContext<Executor> context, MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) {
        throwsError();
        ExecutorInterceptor.super.beforeCreateCacheKey(context, ms, parameter, rowBounds, boundSql);
    }

    @Override
    public void afterCreateCacheKey(InterceptorContext<Executor> context, MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql, @Nullable CacheKey key, @Nullable Throwable failure) {
        throwsError();
        ExecutorInterceptor.super.afterCreateCacheKey(context, ms, parameter, rowBounds, boundSql, key, failure);
    }

    @Override
    public void beforeDeferLoad(InterceptorContext<Executor> context, MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
        throwsError();
        ExecutorInterceptor.super.beforeDeferLoad(context, ms, resultObject, property, key, targetType);
    }

    @Override
    public void afterDeferLoad(InterceptorContext<Executor> context, MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType, @Nullable Throwable failure) {
        throwsError();
        ExecutorInterceptor.super.afterDeferLoad(context, ms, resultObject, property, key, targetType, failure);
    }

    @Override
    public void beforeClose(InterceptorContext<Executor> context, boolean forceRollback) {
        throwsError();
        ExecutorInterceptor.super.beforeClose(context, forceRollback);
    }

    @Override
    public void afterClose(InterceptorContext<Executor> context, boolean forceRollback) {
        throwsError();
        ExecutorInterceptor.super.afterClose(context, forceRollback);
    }
}
