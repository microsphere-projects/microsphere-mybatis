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
package io.microsphere.mybatis.spring.boot.autoconfigure.condition;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@link org.springframework.context.annotation.Conditional} that only matches when MyBatis is available.
 * <p>
 * Specifically, it checks if:
 * <ul>
 *     <li>The property {@code microsphere.mybatis.enabled} is set to {@code true} (or not explicitly disabled).</li>
 *     <li>The class {@code org.apache.ibatis.session.SqlSessionFactory} is present on the classpath.</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * @Configuration
 * @ConditionalOnMyBatisAvailable
 * public class MyBatisAutoConfiguration {
 *
 *     @Bean
 *     public MyService myService(SqlSessionFactory sqlSessionFactory) {
 *         return new MyService(sqlSessionFactory);
 *     }
 * }
 * }</pre>
 *
 * @see ConditionalOnMyBatisEnabled
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnClass
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@ConditionalOnMyBatisEnabled
@ConditionalOnClass(name = {
        "org.apache.ibatis.session.SqlSession",                     // MyBatis Core API
        "org.apache.ibatis.session.SqlSessionFactory"               // MyBatis Spring API
})
public @interface ConditionalOnMyBatisAvailable {
}