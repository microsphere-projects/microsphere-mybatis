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

package io.microsphere.mybatis.spring.annotation;

import io.microsphere.mybatis.plugin.InterceptingExecutorInterceptor;
import io.microsphere.spring.beans.factory.config.GenericBeanPostProcessorAdapter;
import org.apache.ibatis.plugin.Interceptor;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.util.ArrayUtils.contains;

/**
 * The {@link BeanPostProcessor} for {@link SqlSessionFactoryBean} to setup {@link InterceptingExecutorInterceptor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see InterceptingExecutorInterceptor
 * @see SqlSessionFactoryBean
 * @see SqlSessionFactoryBean#afterPropertiesSet()
 * @see BeanPostProcessor
 * @since 1.0.0
 */
class SqlSessionFactoryBeanPostProcessor extends GenericBeanPostProcessorAdapter<SqlSessionFactoryBean> {

    private final InterceptingExecutorInterceptor interceptingExecutorInterceptor;

    public SqlSessionFactoryBeanPostProcessor(InterceptingExecutorInterceptor interceptingExecutorInterceptor) {
        this.interceptingExecutorInterceptor = interceptingExecutorInterceptor;
    }

    @Override
    protected void processBeforeInitialization(SqlSessionFactoryBean bean, String beanName) throws BeansException {
        Interceptor[] plugins = getFieldValue(bean, "plugins");
        if (contains(plugins, this.interceptingExecutorInterceptor)) {
            return;
        }
        bean.addPlugins(this.interceptingExecutorInterceptor);
    }
}
