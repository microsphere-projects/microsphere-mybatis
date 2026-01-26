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

import io.microsphere.mybatis.test.AbstractMyBatisTest;
import io.microsphere.mybatis.test.mapper.ChildMapper;
import io.microsphere.mybatis.test.mapper.FatherMapper;
import io.microsphere.mybatis.test.mapper.UserMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;

/**
 * The Spring {@link Configuration @Configuration} class for MyBatis Spring Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractMyBatisTest
 * @see SqlSessionFactory
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@Import(MyBatisDataSourceTestConfiguration.class)
public class MyBatisTestConfiguration {

    @Bean
    public SqlSessionFactory sqlSessionFactory(org.apache.ibatis.session.Configuration configuration,
                                               DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setConfiguration(configuration);
        sqlSessionFactoryBean.setDataSource(dataSource);
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public ChildMapper childMapper(SqlSessionFactory sqlSessionFactory) {
        return getMapper(sqlSessionFactory, ChildMapper.class);
    }

    @Bean
    public FatherMapper fatherMapper(SqlSessionFactory sqlSessionFactory) {
        return getMapper(sqlSessionFactory, FatherMapper.class);
    }

    @Bean
    public UserMapper userMapper(SqlSessionFactory sqlSessionFactory) {
        return getMapper(sqlSessionFactory, UserMapper.class);
    }

    <T> T getMapper(SqlSessionFactory sqlSessionFactory, Class<T> mapperClass) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            return sqlSession.getMapper(mapperClass);
        }
    }
}