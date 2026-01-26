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

package io.microsphere.mybatis.spring.cloud.autoconfigure;

import io.microsphere.mybatis.executor.ExecutorFilter;
import io.microsphere.mybatis.executor.ExecutorInterceptor;
import io.microsphere.mybatis.spring.boot.autoconfigure.condition.ConditionalOnMyBatisEnabled;
import io.microsphere.spring.cloud.client.condition.ConditionalOnFeaturesEnabled;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cloud.client.actuator.HasFeatures;
import org.springframework.cloud.client.actuator.NamedFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Set;

import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.SetUtils.of;
import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.mybatis.constants.PropertyConstants.MICROSPHERE_MYBATIS_PROPERTY_NAME_PREFIX;
import static io.microsphere.spring.beans.BeanUtils.isBeanPresent;
import static java.util.Collections.emptyList;

/**
 * The Auto-{@link Configuration} for MyBatis Spring Cloud
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Configuration
 * @see org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration
 * @since 1.0.0
 */
@ConditionalOnMyBatisEnabled
@AutoConfigureAfter(name = {
        "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration"
})
@Import(MyBatisCloudAutoConfiguration.FeaturesConfiguration.class)
public class MyBatisCloudAutoConfiguration {

    @ConditionalOnFeaturesEnabled
    public static class FeaturesConfiguration {

        /**
         * The bean name of {@link HasFeatures}
         *
         * @see #mybatisFeatures(ListableBeanFactory)
         */
        public final static String MYBATIS_FEATURES_BEAN_NAME = "myBatisFeatures";

        private static Set<Class<?>> typeFeatures = of(
                org.apache.ibatis.session.Configuration.class,
                SqlSessionFactory.class,
                SqlSessionFactoryBean.class,
                SqlSessionTemplate.class,
                ExecutorFilter.class,
                ExecutorInterceptor.class
        );

        @Bean(name = MYBATIS_FEATURES_BEAN_NAME)
        public HasFeatures mybatisFeatures(ListableBeanFactory beanFactory) {
            List<NamedFeature> namedFeatures = newArrayList(typeFeatures.size());
            for (Class<?> type : typeFeatures) {
                if (isBeanPresent(beanFactory, type)) {
                    String name = MICROSPHERE_MYBATIS_PROPERTY_NAME_PREFIX + DOT + type.getSimpleName();
                    namedFeatures.add(new NamedFeature(name, type));
                }
            }
            return new HasFeatures(emptyList(), namedFeatures);
        }
    }
}