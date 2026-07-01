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

import io.microsphere.spring.core.annotation.AnnotationUtils;
import org.apache.ibatis.session.Configuration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map.Entry;

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.ClassUtils.newInstance;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * The {@link ImportBeanDefinitionRegistrar} class for {@link MyBatisConfiguration}
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // @MyBatisConfiguration triggers this registrar via
 *   // @Import(MyBatisConfigurationBeanDefintionRegistrar.class).
 *   // It registers a MyBatis {@link Configuration} bean named "configuration".
 *
 *   @MyBatisConfiguration(
 *       cacheEnabled = true,
 *       mapUnderscoreToCamelCase = true
 *   )
 *   @EnableMyBatis
 *   @Configuration
 *   public class AppConfig {
 *   }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MyBatisConfiguration
 * @see ImportBeanDefinitionRegistrar
 * @see EnableMyBatis
 * @see Configuration
 * @since 1.0.0
 */
class MyBatisConfigurationBeanDefintionRegistrar extends MyBatisImportBeanDefinitionRegistrar<MyBatisConfiguration> {

    /**
     * The Spring Bean name of {@link Configuration}
     */
    public static final String CONFIGURATION_BEAN_NAME = "configuration";

    @Override
    protected void registerBeanDefinitions(AnnotationAttributes attributes, AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        String configClassName = metadata.getClassName();
        Class<?> configClass = resolveClass(configClassName, super.classLoader);
        MyBatisConfiguration myBatisConfiguration = configClass.getAnnotation(super.annotationType);
        AnnotationAttributes annotationAttributes = AnnotationUtils.getAnnotationAttributes(myBatisConfiguration, super.environment, true);
        registerConfigurationIfAbsent(annotationAttributes, registry);
    }

    private void registerConfigurationIfAbsent(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        registerBeanDefinitionIfAbsent(attributes, registry, CONFIGURATION_BEAN_NAME, this::buildConfigurationBeanDefinition);
    }

    BeanDefinition buildConfigurationBeanDefinition(AnnotationAttributes attributes) {
        BeanDefinitionBuilder builder = genericBeanDefinition(Configuration.class);
        // Those property values need to convert
        // lazyLoadTriggerMethods String[] -> Set
        // proxyFactory Class -> Object
        // variables String[] -> Properties
        for (Entry<String, Object> entry : attributes.entrySet()) {
            String attributeName = entry.getKey();
            Object attributeValue = entry.getValue();
            if ("lazyLoadTriggerMethods".equals(attributeName)) {
                String[] methods = (String[]) attributeValue;
                attributeValue = ofSet(methods);
            } else if ("proxyFactory".equals(attributeName)) {
                Class<?> proxyFactoryClass = (Class<?>) attributeValue;
                attributeValue = newInstance(proxyFactoryClass);
            } else if ("variables".equals(attributeName)) {
                String[] variables = (String[]) attributeValue;
                attributeValue = stringArrayToProperties(variables);
            }
            builder.addPropertyValue(attributeName, attributeValue);
        }
        return builder.getBeanDefinition();
    }
}