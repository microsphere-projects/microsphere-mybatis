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
package io.microsphere.mybatis.test.executor;

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

import static io.microsphere.logging.LoggerFactory.getLogger;
import static java.util.Collections.emptyList;

/**
 * Logging {@link Executor} with no-operation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Executor
 * @since 1.0.0
 */
public class LoggingExecutor implements Executor {

    private static final Logger logger = getLogger(LoggingExecutor.class);

    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        logger.debug("update() : {} , {} ", ms, parameter);
        return 0;
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) throws SQLException {
        logger.debug("query() : {} , {} , {} , {} , {} , {} ", ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
        return emptyList();
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        logger.debug("query() : {} , {} , {} , {} ", ms, parameter, rowBounds, resultHandler);
        return emptyList();
    }

    @Override
    public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
        logger.debug("queryCursor() : {} , {} , {} ", ms, parameter, rowBounds);
        return null;
    }

    @Override
    public List<BatchResult> flushStatements() throws SQLException {
        logger.debug("flushStatements()");
        return emptyList();
    }

    @Override
    public void commit(boolean required) throws SQLException {
        logger.debug("commit() : {} ", required);
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        logger.debug("rollback() : {} ", required);
    }

    @Override
    public CacheKey createCacheKey(MappedStatement ms, Object parameter, RowBounds rowBounds, BoundSql boundSql) {
        logger.debug("createCacheKey() : {} , {} , {} ", ms, parameter, rowBounds);
        return new CacheKey();
    }

    @Override
    public boolean isCached(MappedStatement ms, CacheKey key) {
        logger.debug("isCached() : {} , {}", ms, key);
        return false;
    }

    @Override
    public void clearLocalCache() {
        logger.debug("clearLocalCache()");
    }

    @Override
    public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
        logger.debug("deferLoad() : {} , {} , {} , {} , {} ", ms, resultObject, property, key, targetType);
    }

    @Override
    public Transaction getTransaction() {
        logger.debug("getTransaction() : null");
        return null;
    }

    @Override
    public void close(boolean forceRollback) {
        logger.debug("close() : {} ", forceRollback);
    }

    @Override
    public boolean isClosed() {
        logger.debug("isClosed() : false");
        return false;
    }

    @Override
    public void setExecutorWrapper(Executor executor) {
        logger.debug("setExecutorWrapper() : {}", executor);
    }
}
