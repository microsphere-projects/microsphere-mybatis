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

import io.microsphere.mybatis.test.entity.Child;
import io.microsphere.mybatis.test.entity.User;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.microsphere.mybatis.test.AbstractExecutorTest.MS_ID_SAVE_USER;
import static io.microsphere.mybatis.test.AbstractExecutorTest.MS_ID_USER_BY_ID;
import static io.microsphere.mybatis.test.AbstractExecutorTest.MS_ID_USER_BY_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Abstract Test for Mybatis {@link SqlSession}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see #testSqlSession()
 * @since 1.0.0
 */
public abstract class AbstractSqlSessionTest extends AbstractMyBatisTest {

    @Test
    public void testSqlSession() throws Throwable {
        doInSqlSession(sqlSession -> {

            User user = createUser();

            // Test insert
            assertEquals(1, sqlSession.insert(MS_ID_SAVE_USER, user));

            // Test selectCursor
            Cursor<User> cursor = sqlSession.selectCursor(MS_ID_USER_BY_ID, user.getId());
            assertNotNull(cursor);
            assertFalse(cursor.isOpen());
            assertFalse(cursor.isConsumed());
            assertEquals(-1, cursor.getCurrentIndex());
            cursor.forEach(foundUser -> assertEquals(foundUser, user));

            // Test selectOne
            User foundUser = sqlSession.selectOne(MS_ID_USER_BY_NAME, user.getName());
            assertEquals(foundUser, user);

            // Test selectList
            List<User> users = sqlSession.selectList(MS_ID_USER_BY_NAME, user.getName());
            assertEquals(1, users.size());
            assertEquals(users.get(0), user);

            // Test deferLoad
            deferLoadAfterResultHandler(sqlSession);

            // Test flushStatements
            assertNotNull(sqlSession.flushStatements());


            // Test commit
            sqlSession.commit();
            sqlSession.commit(true);

            // Test rollback
            sqlSession.rollback();
            sqlSession.rollback(true);

            // Test clearCache
            sqlSession.clearCache();
        });

    }

    protected void deferLoadAfterResultHandler(SqlSession sqlSession) {
        MyResultHandler myResultHandler = new MyResultHandler();
        sqlSession.select("io.microsphere.mybatis.test.mapper.ChildMapper.selectAll", myResultHandler);
        for (Child child : myResultHandler.getChildren()) {
            assertNotNull(child.getFather());
        }
    }

    public static class MyResultHandler implements ResultHandler {
        private final List<Child> children = new ArrayList<>();

        @Override
        public void handleResult(ResultContext context) {
            Child child = (Child) context.getResultObject();
            children.add(child);
        }

        public List<Child> getChildren() {
            return children;
        }
    }

}
