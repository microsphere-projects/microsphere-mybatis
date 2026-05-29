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

import org.apache.ibatis.mapping.MappedStatement;
import org.junit.jupiter.api.Test;

import static io.microsphere.mybatis.test.AbstractExecutorTest.MS_ID_USER_BY_ID;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The test class for {@link AbstractMyBatisTest}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractMyBatisTest
 * @since 1.0.0
 */
class AbstractMyBatisTestTest extends AbstractMyBatisTest {

    @Test
    void testRunSafely() {
        assertDoesNotThrow(() -> {
            runSafely(() -> {
            });
        });

        assertDoesNotThrow(() -> {
            runSafely(() -> {
                throw new RuntimeException("Test Exception");
            });
        });
    }

    @Test
    void testGetConnection() throws Throwable {
        doInExecutor(executor -> {
            assertNotNull(getConnection(executor));
        });
    }

    @Test
    void testGetMappedStatement() {
        MappedStatement mappedStatement = getMappedStatement(MS_ID_USER_BY_ID);
        assertNotNull(mappedStatement);
    }
}
