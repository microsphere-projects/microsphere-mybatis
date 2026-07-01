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
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import javax.sql.DataSource;
import java.util.Properties;

import static io.microsphere.mybatis.spring.annotation.MyBatisConfigurationBeanDefintionRegistrar.CONFIGURATION_BEAN_NAME;
import static io.microsphere.spring.core.env.PropertySourcesUtils.getPropertyNames;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.Assert.assertTrue;
import static io.microsphere.util.StringUtils.isBlank;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * {@link ImportBeanDefinitionRegistrar} class for {@link EnableMyBatis}
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // This registrar is triggered automatically when @EnableMyBatis is present.
 *   // It registers SqlSessionFactory, SqlSessionTemplate
 *
 *   @EnableMyBatis
 *   @Configuration
 *   public class AppConfig {
 *       // SqlSessionFactory  bean name: "sqlSessionFactory"
 *       // SqlSessionTemplate bean name: "sqlSessionTemplate"
 *   }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableMyBatis
 * @see BeanCapableImportCandidate
 * @see SqlSessionFactoryBean
 * @see SqlSessionFactory
 * @see SqlSessionTemplate
 * @since 1.0.0
 */
public class MyBatisBeanDefinitionRegistrar extends MyBatisImportBeanDefinitionRegistrar<EnableMyBatis> {

    /**
     * The Spring Bean name of {@link SqlSessionFactory}
     */
    public static final String SQL_SESSION_FACTORY_BEAN_NAME = "sqlSessionFactory";

    /**
     * The Spring Bean name of {@link SqlSessionTemplate}
     */
    public static final String SQL_SESSION_TEMPLATE_BEAN_NAME = "sqlSessionTemplate";

    @Override
    protected void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
                                           BeanNameGenerator importBeanNameGenerator,
                                           ResolvablePlaceholderAnnotationAttributes<EnableMyBatis> annotationAttributes) {

        // Register the BeanDefinition of SqlSessionFactoryBean if absent
        registerSqlSessionFactoryBeanIfAbsent(annotationAttributes, registry);

        // Register the BeanDefinition of SqlSessionTemplate if absent
        registerSqlSessionTemplateIfAbsent(annotationAttributes, registry);
    }

    /**
     * Register the {@link BeanDefinition} of {@link SqlSessionFactoryBean} if absent
     *
     * @param attributes {@link AnnotationAttributes}
     * @param registry   {@link BeanDefinitionRegistry}
     */
    void registerSqlSessionFactoryBeanIfAbsent(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        registerBeanDefinitionIfAbsent(attributes, registry, SQL_SESSION_FACTORY_BEAN_NAME, this::buildSqlSessionFactoryBeanDefinition);
    }

    /**
     * Register the {@link BeanDefinition} of {@link SqlSessionFactoryBean} if absent
     *
     * @param attributes {@link AnnotationAttributes}
     * @param registry   {@link BeanDefinitionRegistry}
     */
    void registerSqlSessionTemplateIfAbsent(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        registerBeanDefinitionIfAbsent(attributes, registry, SQL_SESSION_TEMPLATE_BEAN_NAME, this::buildSqlSessionTemplateBeanDefinition);
    }

    /**
     * Build the {@link BeanDefinition} for the {@link SqlSessionFactoryBean}.
     *
     * @param attributes the resolved {@link AnnotationAttributes} from {@link EnableMyBatis}
     * @return the constructed {@link BeanDefinition}
     */
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

    BeanDefinition buildSqlSessionTemplateBeanDefinition(AnnotationAttributes attributes) {
        BeanDefinitionBuilder builder = genericBeanDefinition(SqlSessionTemplate.class);
        ExecutorType executorType = attributes.getEnum("executorType");
        builder.addConstructorArgReference(SQL_SESSION_FACTORY_BEAN_NAME);
        builder.addConstructorArgValue(executorType);
        return builder.getBeanDefinition();
    }

    /**
     * Configure the {@link Configuration} or {@code configLocation} property of the
     * {@link SqlSessionFactoryBean} builder, depending on whether a config location is specified.
     *
     * @param builder    the {@link BeanDefinitionBuilder} for {@link SqlSessionFactoryBean}
     * @param attributes the resolved annotation attributes
     */
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

    /**
     * Validate the {@code configLocation} attribute when {@link EnableMyBatis#checkConfigLocation()} is
     * {@code true}.  Throws {@link IllegalArgumentException} if the resource does not exist.
     *
     * @param attributes the resolved annotation attributes
     * @throws IllegalArgumentException if the config location resource is not found
     */
    void checkConfigLocation(AnnotationAttributes attributes) {
        boolean checkConfigLocation = attributes.getBoolean("checkConfigLocation");
        if (checkConfigLocation) {
            String configLocation = attributes.getString("configLocation");
            ResourceLoader resourceLoader = getResourceLoader();
            Resource resource = resourceLoader.getResource(configLocation);
            assertTrue(resource.exists(), () -> format("The resource can't be found by the attribute 'configLocation' : '{}'", configLocation));
        }
    }

    /**
     * Resolve the MyBatis configuration {@link Properties} from the annotation attributes, optionally
     * merging them with the Spring {@link PropertySources} when
     * {@link EnableMyBatis#configurationPropertiesImportPropertySources()} is {@code true}.
     *
     * @param attributes the resolved annotation attributes
     * @return the merged {@link Properties}; never {@code null}
     */
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

}
