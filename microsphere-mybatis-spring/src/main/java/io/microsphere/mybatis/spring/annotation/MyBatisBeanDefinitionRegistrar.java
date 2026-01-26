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

import io.microsphere.logging.Logger;
import io.microsphere.mybatis.executor.ExecutorFilter;
import io.microsphere.mybatis.executor.ExecutorInterceptor;
import io.microsphere.mybatis.plugin.InterceptingExecutorInterceptor;
import io.microsphere.spring.context.annotation.BeanCapableImportCandidate;
import io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.stream.Stream;

import static io.microsphere.constants.SeparatorConstants.LINE_SEPARATOR;
import static io.microsphere.constants.SymbolConstants.EQUAL;
import static io.microsphere.constants.SymbolConstants.WILDCARD;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.mybatis.spring.annotation.MyBatisConfigurationBeanDefintionRegistrar.CONFIGURATION_BEAN_NAME;
import static io.microsphere.spring.beans.BeanUtils.getBeanNames;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes.of;
import static io.microsphere.spring.core.env.PropertySourcesUtils.getPropertyNames;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static io.microsphere.util.ArrayUtils.forEach;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.Assert.assertTrue;
import static io.microsphere.util.StringUtils.isBlank;
import static io.microsphere.util.StringUtils.split;
import static io.microsphere.util.StringUtils.trimAllWhitespace;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * {@link ImportBeanDefinitionRegistrar} class for {@link EnableMyBatis}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableMyBatis
 * @see BeanCapableImportCandidate
 * @see SqlSessionFactoryBean
 * @see SqlSessionFactory
 * @see SqlSessionTemplate
 * @since 1.0.0
 */
public class MyBatisBeanDefinitionRegistrar extends BeanCapableImportCandidate implements ImportBeanDefinitionRegistrar {

    static final Class<EnableMyBatis> ANNOTATION_CLASS = EnableMyBatis.class;

    static final String ANNOTATION_CLASS_NAME = ANNOTATION_CLASS.getName();

    /**
     * The Spring Bean name of {@link SqlSessionFactory}
     */
    public static final String SQL_SESSION_FACTORY_BEAN_NAME = "sqlSessionFactory";

    /**
     * The Spring Bean name of {@link SqlSessionTemplate}
     */
    public static final String SQL_SESSION_TEMPLATE_BEAN_NAME = "sqlSessionTemplate";

    /**
     * The Spring Bean name of {@link InterceptingExecutorInterceptor}
     */
    public static final String INTERCEPTING_EXECUTOR_INTERCEPTOR_BEAN_NAME = "interceptingExecutorInterceptor";

    private static final Logger logger = getLogger(ANNOTATION_CLASS_NAME);

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(ANNOTATION_CLASS_NAME);
        ResolvablePlaceholderAnnotationAttributes attributes = of(annotationAttributes, ANNOTATION_CLASS, getEnvironment());
        // Register the BeanDefintion of InterceptingExecutorInterceptor if required
        registerInterceptingExecutorInterceptorIfRequired(attributes, registry);

        // Register the BeanDefinition of SqlSessionFactoryBean if absent
        registerSqlSessionFactoryBeanIfAbsent(attributes, registry);

        // Register the BeanDefinition of SqlSessionTemplate if absent
        registerSqlSessionTemplateIfAbsent(attributes, registry);
    }

    /**
     * Register the {@link BeanDefinition} of {@link SqlSessionFactoryBean} if absent
     *
     * @param attributes {@link AnnotationAttributes}
     * @param registry   {@link BeanDefinitionRegistry}
     */
    void registerSqlSessionFactoryBeanIfAbsent(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        BeanDefinition beanDefinition = buildSqlSessionFactoryBeanDefinition(attributes);
        registerBeanDefinition(registry, SQL_SESSION_FACTORY_BEAN_NAME, beanDefinition);
    }

    /**
     * Register the {@link BeanDefinition} of {@link SqlSessionFactoryBean} if absent
     *
     * @param attributes {@link AnnotationAttributes}
     * @param registry   {@link BeanDefinitionRegistry}
     */
    void registerSqlSessionTemplateIfAbsent(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder builder = genericBeanDefinition(SqlSessionTemplate.class);
        ExecutorType executorType = attributes.getEnum("executorType");
        builder.addConstructorArgReference(SQL_SESSION_FACTORY_BEAN_NAME);
        builder.addConstructorArgValue(executorType);
        BeanDefinition beanDefinition = builder.getBeanDefinition();
        registerBeanDefinition(registry, SQL_SESSION_TEMPLATE_BEAN_NAME, beanDefinition);
    }

    /**
     * Register the {@link BeanDefinition} of {@link SqlSessionFactoryBean} if Any {@link ExecutorFilter}  or
     * {@link ExecutorInterceptor} bean is present.
     *
     * @param attributes {@link AnnotationAttributes}
     * @param registry   {@link BeanDefinitionRegistry}
     * @see ExecutorFilter
     * @see ExecutorInterceptor
     * @see InterceptingExecutorInterceptor
     */
    private void registerInterceptingExecutorInterceptorIfRequired(ResolvablePlaceholderAnnotationAttributes attributes,
                                                                   BeanDefinitionRegistry registry) {
        if (attributes.getBoolean("interceptExecutor")) {
            String[] executorFilterBeanNames = getBeanNamesByType(ExecutorFilter.class);
            String[] executorInterceptorBeanNames = getBeanNamesByType(ExecutorInterceptor.class);
            int executorFilterBeanCount = length(executorFilterBeanNames);
            int executorInterceptorBeanCount = length(executorInterceptorBeanNames);
            logger.trace("Found {} ExecutorFilter and {} ExecutorInterceptor BeanDefinition(s)",
                    executorFilterBeanCount, executorInterceptorBeanCount);
            if (executorFilterBeanCount == 0 && executorInterceptorBeanCount == 0) {
                logger.trace("No bean of ExecutorFilter or ExecutorInterceptor was found.");
                return;
            }
            BeanDefinitionBuilder builder = genericBeanDefinition(InterceptingExecutorInterceptor.class);
            forEach(executorFilterBeanNames, builder::addDependsOn);
            forEach(executorInterceptorBeanNames, builder::addDependsOn);

            BeanDefinition beanDefinition = builder.getBeanDefinition();
            registerBeanDefinition(registry, INTERCEPTING_EXECUTOR_INTERCEPTOR_BEAN_NAME, beanDefinition);
        }
    }

    BeanDefinition buildSqlSessionFactoryBeanDefinition(AnnotationAttributes attributes) {

        checkConfigLocation(attributes);

        BeanDefinitionBuilder builder = genericBeanDefinition(SqlSessionFactoryBean.class);

        // References the DataSource Bean
        setBeanReferencePropertyValue(builder, attributes, "dataSource", DataSource.class);
        // Set the attribute "configLocation"
        setConfiguration(builder, attributes);
        // Set the attribute "mapperLocations"
        setPropertyValue(builder, attributes, "mapperLocations");
        // Set the attribute "typeAliasesPackage"
        setPackagePropertyValue(builder, attributes, "typeAliasesPackage");
        // Set the attribute "typeAliasesSuperType"
        setPropertyValue(builder, attributes, "typeAliasesSuperType", Object.class);
        // Set the attribute "typeHandlersPackage"
        setPackagePropertyValue(builder, attributes, "typeHandlersPackage");
        // Set the attribute "vfs"
        setPropertyValue(builder, attributes, "vfs", VFS.class);
        // Set the attribute "defaultScriptingLanguageDriver"
        setPropertyValue(builder, attributes, "defaultScriptingLanguageDriver", LanguageDriver.class);
        // Set the attribute "configurationProperties"
        Properties configurationProperties = resolveConfigurationProperties(attributes);
        setPropertyValue(builder, "configurationProperties", configurationProperties);

        // References the ObjectWrapperFactory Bean
        setBeanReferencePropertyValue(builder, attributes, "objectWrapperFactory", ObjectWrapperFactory.class);
        // References the DatabaseIdProvider Bean
        setBeanReferencePropertyValue(builder, attributes, "databaseIdProvider", DatabaseIdProvider.class);
        // References the Cache Bean
        setBeanReferencePropertyValue(builder, attributes, "cache", Cache.class);
        // References the Interceptor Beans
        setBeanReferencePropertyValues(builder, attributes, "plugins", Interceptor.class);
        // References the TypeHandler Beans
        setBeanReferencePropertyValues(builder, attributes, "typeHandlers", TypeHandler.class);
        // References the LanguageDriver Beans
        setBeanReferencePropertyValues(builder, attributes, "scriptingLanguageDrivers", LanguageDriver.class);

        return builder.getBeanDefinition();
    }

    void setConfiguration(BeanDefinitionBuilder builder, AnnotationAttributes attributes) {
        String attributeName = "configLocation";
        String configLocation = attributes.getString(attributeName);
        if (isBlank(configLocation)) {
            String targetBeanName = findTargetBeanName(Configuration.class);
            if (isBlank(targetBeanName)) {
                setBeanReferencePropertyValue(builder, "configuration", CONFIGURATION_BEAN_NAME);
            } else {
                setBeanReferencePropertyValue(builder, "configuration", targetBeanName);
            }
        } else {
            setPropertyValue(builder, attributeName, configLocation);
        }
    }

    void checkConfigLocation(AnnotationAttributes attributes) {
        boolean checkConfigLocation = attributes.getBoolean("checkConfigLocation");
        if (checkConfigLocation) {
            String configLocation = attributes.getString("configLocation");
            ResourceLoader resourceLoader = getResourceLoader();
            Resource resource = resourceLoader.getResource(configLocation);
            assertTrue(resource.exists(), () -> format("The resource can't be found by the attribute 'configLocation' : '{}'", configLocation));
        }
    }

    Properties resolveConfigurationProperties(AnnotationAttributes attributes) {
        String[] configurationProperties = attributes.getStringArray("configurationProperties");
        Properties properties = new Properties();
        boolean importingPropertySources = attributes.getBoolean("configurationPropertiesImportPropertySources");
        if (importingPropertySources) {
            ConfigurableEnvironment environment = getEnvironment();
            logger.trace("The MyBatis configuration properties will import the Spring PropertySources.");
            for (PropertySource propertySource : environment.getPropertySources()) {
                String[] propertyNames = getPropertyNames(propertySource);
                for (String propertyName : propertyNames) {
                    Object propertyValue = propertySource.getProperty(propertyName);
                    properties.putIfAbsent(propertyName, propertyValue);
                }
            }
        }
        properties.putAll(stringArrayToProperties(configurationProperties));
        return properties;
    }

    static Properties stringArrayToProperties(String[] lines) {
        Properties properties = new Properties();
        for (String line : lines) {
            String[] keyAndValue = split(line, EQUAL);
            assertTrue(length(keyAndValue) == 2, () -> format("The configuration property is invalid, the content must contain key and value : '{}'", line));
            String key = trimAllWhitespace(keyAndValue[0]);
            String value = trimAllWhitespace(keyAndValue[1]);
            properties.setProperty(key, value);
        }
        return properties;
    }

    void setPackagePropertyValue(BeanDefinitionBuilder builder, AnnotationAttributes attributes, String attributeName) {
        String[] packages = attributes.getStringArray(attributeName);
        logger.trace("Try to set the package({}) property value by the attribute[name : '{}']", arrayToString(packages), attributeName);
        int length = length(packages);
        final String packageName;
        if (length == 0) {
            packageName = null;
        } else if (length == 1) {
            packageName = packages[0];
        } else {
            StringJoiner packageJoiner = new StringJoiner(LINE_SEPARATOR);
            for (String pkg : packages) {
                packageJoiner.add(pkg);
            }
            packageName = packageJoiner.toString();
        }
        if (isBlank(packageName)) {
            logger.trace("No package property value specified by the attribute[name : '{}']", attributeName);
        } else {
            setPropertyValue(builder, attributeName, packageName);
        }
    }

    void setBeanReferencePropertyValue(BeanDefinitionBuilder builder, AnnotationAttributes attributes, String attributeName, Class<?> beanType) {
        String beanName = attributes.getString(attributeName);
        logger.trace("Try to set the Spring Bean[{} , name : '{}'] Reference property value by the attribute[name : '{}']", beanType, beanName, attributeName);
        setBeanReferencePropertyValue(builder, attributeName, beanName, beanType);
    }

    void setBeanReferencePropertyValues(BeanDefinitionBuilder builder, AnnotationAttributes attributes, String attributeName, Class<?> beanType) {
        String[] beanNames = attributes.getStringArray(attributeName);
        logger.trace("Try to set the Spring Bean[{} , names : '{}'] Reference property values by the attribute[name : '{}']", beanType, arrayToString(beanNames), attributeName);

        int length = length(beanNames);
        if (length == 0) {
            logger.debug("No Spring Bean was speicified by the attribute[name : '{}']", attributeName);
        } else {
            for (int i = 0; i < length; i++) {
                String beanName = beanNames[i];
                setBeanReferencePropertyValue(builder, attributeName, beanName, beanType);
            }
        }
    }

    void setBeanReferencePropertyValue(BeanDefinitionBuilder builder, String attributeName, String beanName, Class<?> beanType) {
        if (isBlank(beanName)) {
            logger.trace("No Spring Bean[{}] was speicified by the attribute[name : '{}']", beanType, attributeName);
        } else if (WILDCARD.equals(beanName)) {
            String targetBeanName = findTargetBeanName(beanType);
            setBeanReferencePropertyValue(builder, attributeName, targetBeanName, beanType);
        } else {
            setBeanReferencePropertyValue(builder, attributeName, beanName);
        }
    }

    void setBeanReferencePropertyValue(BeanDefinitionBuilder builder, String attributeName, String beanName) {
        setPropertyValue(builder, attributeName, new RuntimeBeanReference(beanName));
    }

    String findTargetBeanName(Class<?> beanType) {
        String[] beanNames = getBeanNamesByType(beanType);
        int length = length(beanNames);
        final String targetBeanName;
        if (length == 0) {
            targetBeanName = null;
        } else if (length == 1) {
            targetBeanName = beanNames[0];
        } else {
            ConfigurableListableBeanFactory beanFactory = super.getBeanFactory();
            // Find the name of primary bean
            targetBeanName = Stream.of(beanNames).filter(beanName -> {
                        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
                        return beanDefinition.isPrimary();
                    })
                    .findFirst()
                    .orElse(null);
        }
        return targetBeanName;
    }

    String[] getBeanNamesByType(Class<?> beanType) {
        ConfigurableListableBeanFactory beanFactory = super.getBeanFactory();
        return getBeanNames(beanFactory, beanType, true);
    }

    static void setPropertyValue(BeanDefinitionBuilder builder, AnnotationAttributes attributes, String attributeName, Object defaultValue) {
        Object value = attributes.get(attributeName);
        if (Objects.equals(defaultValue, value)) {
            logger.trace("Default property value[{}] will ignored the attribute[name : '{}']", defaultValue, attributeName);
            return;
        }
        setPropertyValue(builder, attributeName, value);
    }

    static void setPropertyValue(BeanDefinitionBuilder builder, AnnotationAttributes attributes, String attributeName) {
        Object attributeValue = attributes.get(attributeName);
        setPropertyValue(builder, attributeName, attributeValue);
    }

    static void setPropertyValue(BeanDefinitionBuilder builder, String attributeName, Object attributeValue) {
        logger.trace("Set the BeanDefinition[{}] property[name : '{}'  , value : '{}']", builder.getRawBeanDefinition(), attributeName, attributeValue);
        builder.addPropertyValue(attributeName, attributeValue);
    }
}