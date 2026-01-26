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

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * {@link ExecutorFilter} class throws the errors.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ExecutorFilter
 * @since 1.0.0
 */
public class ThrowingErrorExecutorFilter implements ExecutorFilter {

    @Override
    public int update(MappedStatement ms, Object parameter, ExecutorFilterChain chain) throws SQLException {
        throwsError();
        return ExecutorFilter.super.update(ms, parameter, chain);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
                             CacheKey cacheKey, BoundSql boundSql, ExecutorFilterChain chain) throws SQLException {
        throwsError();
        return ExecutorFilter.super.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql, chain);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
                             ExecutorFilterChain chain) throws SQLException {
        throwsError();
        return ExecutorFilter.super.query(ms, parameter, rowBounds, resultHandler, chain);
    }

    @Override
    public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds, ExecutorFilterChain chain) throws SQLException {
        throwsError();
        return ExecutorFilter.super.queryCursor(ms, parameter, rowBounds, chain);
    }

    @Override
    public void commit(boolean required, ExecutorFilterChain chain) throws SQLException {
        throwsError();
        ExecutorFilter.super.commit(required, chain);
    }

    @Override
    public void rollback(boolean required, ExecutorFilterChain chain) throws SQLException {
        throwsError();
        ExecutorFilter.super.rollback(required, chain);
    }

    @Override
    public CacheKey createCacheKey(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql, ExecutorFilterChain chain) {
        throwsError();
        return ExecutorFilter.super.createCacheKey(ms, parameter, rowBounds, boundSql, chain);
    }

    @Override
    public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType, ExecutorFilterChain chain) {
        throwsError();
        ExecutorFilter.super.deferLoad(ms, resultObject, property, key, targetType, chain);
    }

    @Override
    public Transaction getTransaction(ExecutorFilterChain chain) {
        throwsError();
        return ExecutorFilter.super.getTransaction(chain);
    }

    @Override
    public void close(boolean forceRollback, ExecutorFilterChain chain) {
        throwsError();
        ExecutorFilter.super.close(forceRollback, chain);
    }

    public static void throwsError() {
        throw new RuntimeException("For testing");
    }
}
