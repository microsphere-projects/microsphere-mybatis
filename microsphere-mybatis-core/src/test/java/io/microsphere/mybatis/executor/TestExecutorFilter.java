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

import org.apache.ibatis.mapping.MappedStatement;

import java.sql.SQLException;

import static io.microsphere.mybatis.plugin.InterceptingExecutorInterceptorTest.TEST_PROPERTY_KEY;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ExecutorFilter} class to test {@link ExecutorFilter} and {@link ExecutorFilterChain}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ExecutorFilter
 * @see ExecutorFilterChain
 * @since 1.0.0
 */
public class TestExecutorFilter implements ExecutorFilter {

    @Override
    public int update(MappedStatement ms, Object parameter, ExecutorFilterChain chain) throws SQLException {
        assertNotNull(chain.getProperties().get(TEST_PROPERTY_KEY));
        assertTrue(chain.getPosition() <= chain.getSize());
        assertTrue(asList(chain.getFilters()).contains(this));
        return chain.update(ms, parameter);
    }
}
