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

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

import static io.microsphere.mybatis.test.AbstractMyBatisTest.runCreateDatabaseScript;
import static io.microsphere.mybatis.test.AbstractMyBatisTest.runDestroyDatabaseScript;

/**
 * The Spring {@link Configuration @Configuration} class for MyBatis Database test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Configuration
 * @see DataSource
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
public class MyBatisDataBaseTestConfiguration {

    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshedEvent(ContextRefreshedEvent event) throws SQLException, IOException {
        ApplicationContext context = event.getApplicationContext();
        DataSource dataSource = context.getBean(DataSource.class);
        runCreateDatabaseScript(dataSource);
    }

    @EventListener(ContextClosedEvent.class)
    public void onContextClosedEvent(ContextClosedEvent event) throws SQLException, IOException {
        ApplicationContext context = event.getApplicationContext();
        DataSource dataSource = context.getBean(DataSource.class);
        runDestroyDatabaseScript(dataSource);
    }
}
