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
import io.microsphere.mybatis.test.entity.Father;
import io.microsphere.mybatis.test.entity.User;
import io.microsphere.mybatis.test.mapper.ChildMapper;
import io.microsphere.mybatis.test.mapper.FatherMapper;
import io.microsphere.mybatis.test.mapper.UserMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Abstract Test for Mybatis Mapper
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see #testMapper()
 * @since 1.0.0
 */
public abstract class AbstractMapperTest extends AbstractExecutorTest {

    @Test
    public void testMapper() throws Throwable {
        getConfiguration().setCacheEnabled(false);
        doInMapper(UserMapper.class, AbstractMapperTest::assertUserMapper);
        doInMapper(ChildMapper.class, AbstractMapperTest::assertChildMapper);
        doInMapper(FatherMapper.class, AbstractMapperTest::assertFatherMapper);
    }

    public static void assertUserMapper(UserMapper userMapper) {
        User user = createUser();
        // Test saveUser
        userMapper.saveUser(user);

        // Test getUserById
        User foundUser = userMapper.getUserById(user.getId());
        assertEquals(foundUser, user);

        // Test getUserByName
        foundUser = userMapper.getUserByName(user.getName());
        assertEquals(foundUser, user);

        assertNotEquals(foundUser, createUser());
        assertNotEquals(foundUser, userMapper);

        assertThrows(Throwable.class, () -> userMapper.getErrorUserByName(user.getName()));
    }

    public static void assertChildMapper(ChildMapper childMapper) {
        List<Child> children = childMapper.selectAll();
        assertEquals(2, children.size());

        Child firstChild = children.get(0);
        Father father = firstChild.getFather();
        assertEquals(1, firstChild.getId());
        assertEquals("John Smith jr", firstChild.getName());
        assertEquals(1, father.getId());

        Child secondChild = children.get(1);
        assertEquals(2, secondChild.getId());
        assertEquals("John Smith jr 2", secondChild.getName());
        assertEquals(1, secondChild.getFather().getId());
    }

    public static void assertFatherMapper(FatherMapper fatherMapper) {
        Father father = fatherMapper.selectById(1);
        assertEquals("John Smith", father.getName());
    }

}