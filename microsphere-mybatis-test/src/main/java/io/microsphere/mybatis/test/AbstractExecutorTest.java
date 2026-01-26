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
package io.microsphere.mybatis.test;

import io.microsphere.mybatis.test.entity.User;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Abstract Test for MyBatis {@link Executor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see #testExecutor()
 * @since 1.0.0
 */
public abstract class AbstractExecutorTest extends AbstractSqlSessionTest {

    public static final String MS_ID_SAVE_USER = "io.microsphere.mybatis.test.mapper.UserMapper.saveUser";

    public static final String MS_ID_USER_BY_ID = "io.microsphere.mybatis.test.mapper.UserMapper.getUserById";

    public static final String MS_ID_USER_BY_NAME = "io.microsphere.mybatis.test.mapper.UserMapper.getUserByName";

    @Test
    public void testExecutor() throws Throwable {
        doInExecutor(executor -> assertEexecutor(getConfiguration(), executor));
    }

    public static void assertEexecutor(Configuration configuration, Executor executor) throws SQLException {
        MappedStatement ms = configuration.getMappedStatement(MS_ID_SAVE_USER);
        User user = createUser();

        // Test update
        assertEquals(1, executor.update(ms, user));

        // Test query
        ms = configuration.getMappedStatement(MS_ID_USER_BY_ID);
        List<User> users = executor.query(ms, user.getId(), new RowBounds(), Executor.NO_RESULT_HANDLER);
        assertEquals(1, users.size());
        assertEquals(users.get(0), user);
    }
}