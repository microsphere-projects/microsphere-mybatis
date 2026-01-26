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

import io.microsphere.logging.Logger;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Arrays;

import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * Test Annotated {@link Interceptor} for {@link Executor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Interceptor
 * @since 1.0.0
 */
@Intercepts(
        value = {@Signature(
                type = Executor.class,
                method = "update",
                args = {MappedStatement.class, Object.class}
        ), @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}
        ), @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
        }
)
public class TestAnnotatedExecutorInterceptor implements Interceptor {

    private static final Logger logger = getLogger(TestAnnotatedExecutorInterceptor.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (logger.isTraceEnabled()) {
            logger.trace("intercept - target : {} , method : {} , args : {}",
                    invocation.getTarget(), invocation.getMethod(), Arrays.toString(invocation.getArgs()));
        }
        return invocation.proceed();
    }
}
