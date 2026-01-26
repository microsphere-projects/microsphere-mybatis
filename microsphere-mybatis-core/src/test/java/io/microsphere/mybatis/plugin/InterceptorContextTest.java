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


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link InterceptorContext} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see InterceptorContext
 * @since 1.0.0
 */
class InterceptorContextTest {

    private static final String TEST_TARGET = "test";

    private Properties properties;

    private InterceptorContext<String> context;

    @BeforeEach
    void setUp() {
        this.properties = new Properties();
        this.context = new InterceptorContext<>(TEST_TARGET, this.properties);
    }

    @Test
    void testGetTarget() {
        assertSame(TEST_TARGET, this.context.getTarget());
    }

    @Test
    void testGetProperties() {
        assertSame(this.properties, this.context.getProperties());
    }

    @Test
    void testStartTimeOps() {
        assertSame(this.context, this.context.setStartTime(1L));
        assertEquals(Long.valueOf(1L), this.context.getStartTime());
    }

    @Test
    void testAttributeOps() {
        assertTrue(this.context.getAttributes().isEmpty());
        assertSame(this.context, this.context.removeAttributes());
        assertSame(this.context, this.context.setAttribute("name", "value"));
        assertTrue(this.context.hasAttribute("name"));
        assertEquals("value", this.context.getAttribute("name"));
        assertEquals("value", this.context.removeAttribute("name"));
        assertEquals("value", this.context.getAttribute("name", "value"));
        assertTrue(this.context.getAttributes().isEmpty());
        assertSame(this.context, this.context.removeAttributes());
    }

    @Test
    void testToString() {
        assertNotNull(this.context.toString());
    }
}