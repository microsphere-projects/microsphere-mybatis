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

package io.microsphere.mybatis.spring.test.config;


import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import javax.sql.DataSource;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link MyBatisDataSourceTestConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MyBatisDataSourceTestConfiguration
 * @since 1.0.0
 */
@SpringJUnitConfig(classes = {
        MyBatisDataSourceTestConfiguration.class,
        MyBatisDataSourceTestConfigurationTest.class
})
class MyBatisDataSourceTestConfigurationTest {

    @Autowired
    private Configuration configuration;

    @Autowired
    private DataSource dataSource;

    @Test
    void test() throws IOException {
        assertNotNull(this.configuration);
        assertNotNull(this.dataSource);
    }
}