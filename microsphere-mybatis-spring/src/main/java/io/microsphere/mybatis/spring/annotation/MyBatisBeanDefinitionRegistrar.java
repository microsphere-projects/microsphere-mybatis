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

import io.microsphere.mybatis.executor.ExecutorFilter;
import io.microsphere.mybatis.executor.ExecutorInterceptor;
import io.microsphere.mybatis.plugin.InterceptingExecutorInterceptor;
import io.microsphere.spring.beans.BeanSource;
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
import org.springframework.core.env.PropertySources;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Stream;

import static io.microsphere.constants.SeparatorConstants.LINE_SEPARATOR;
import static io.microsphere.constants.SymbolConstants.EQUAL;
import static io.microsphere.constants.SymbolConstants.WILDCARD;
import static io.microsphere.mybatis.spring.annotation.MyBatisConfigurationBeanDefintionRegistrar.CONFIGURATION_BEAN_NAME;
import static io.microsphere.spring.beans.BeanSource.registerBeans;
import static io.microsphere.spring.beans.BeanUtils.getBeanNames;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.core.env.PropertySourcesUtils.getPropertyNames;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static io.microsphere.util.ArrayUtils.forEach;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.Assert.assertTrue;
import static io.microsphere.util.ServiceLoaderUtils.getServiceClasses;
import static io.microsphere.util.StringUtils.isBlank;
import static io.microsphere.util.StringUtils.split;
import static io.microsphere.util.StringUtils.trimAllWhitespace;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * {@link ImportBeanDefinitionRegistrar} class for {@link EnableMyBatis}
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // This registrar is triggered automatically when @EnableMyBatis is present.
 *   // It registers SqlSessionFactory, SqlSessionTemplate, and optionally
 *   // InterceptingExecutorInterceptor bean definitions.
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
public class MyBatisBeanDefinitionRegistrar extends BeanCapableImportCandidate implements ImportBeanDefinitionRegistrar {

    static final Class<EnableMyBatis> ANNOTATION_CLASS = EnableMyBatis.class;

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

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        ResolvablePlaceholderAnnotationAttributes attributes = getAnnotationAttributes(metadata, EnableMyBatis.class);

        // Register the relevant BeanDefinitions of InterceptingExecutor if required
        registeInterceptingExecutorBeansIfRequired(attributes, registry);

        // Register the BeanDefinition of SqlSessionFactoryBean if absent
        registerSqlSessionFactoryBeanIfAbsent(attributes, registry);

        // Register the BeanDefinition of SqlSessionTemplate if absent
        registerSqlSessionTemplateIfAbsent(attributes, registry);
    }

    /**
     * Registers the relevant {@link BeanDefinition}s for the intercepting executor components
     * if the {@link EnableMyBatis#interceptExecutor()} attribute is enabled.
     * <p>
     * This includes registering {@link ExecutorFilter}s and {@link ExecutorInterceptor}s
     * from the specified sources, and conditionally registering the
     * {@link InterceptingExecutorInterceptor} if any filters or interceptors are present.
     *
     * @param attributes the resolved {@link ResolvablePlaceholderAnnotationAttributes} from {@link EnableMyBatis}
     * @param registry   the {@link BeanDefinitionRegistry} to register bean definitions with
     */
    private void registeInterceptingExecutorBeansIfRequired(ResolvablePlaceholderAnnotationAttributes attributes,
                                                            BeanDefinitionRegistry registry) {
        if (attributes.getBoolean("interceptExecutor")) {
            BeanSource[] sources = (BeanSource[]) attributes.get("sources");
            registerExecutorFilters(registry, sources);
            registerExecutorInterceptors(registry, sources);
            registerInterceptingExecutorInterceptorIfRequired(registry);
        }
    }

    /**
     * Registers {@link ExecutorFilter} beans from the specified {@link BeanSource}s.
     *
     * @param registry the {@link BeanDefinitionRegistry} to register bean definitions with
     * @param sources  the array of {@link BeanSource}s to scan for {@link ExecutorFilter} implementations
     */
    private void registerExecutorFilters(BeanDefinitionRegistry registry, BeanSource[] sources) {
        registerBeansFromSources(registry, ExecutorFilter.class, sources);
    }

    /**
     * Registers {@link ExecutorInterceptor} beans from the specified {@link BeanSource}s.
     *
     * @param registry the {@link BeanDefinitionRegistry} to register bean definitions with
     * @param sources  the array of {@link BeanSource}s to scan for {@link ExecutorInterceptor} implementations
     */
    private void registerExecutorInterceptors(BeanDefinitionRegistry registry, BeanSource[] sources) {
        registerBeansFromSources(registry, ExecutorInterceptor.class, sources);
    }

    /**
     * Registers beans of the specified type from the given {@link BeanSource}s.
     * <p>
     * Depending on the source type, this method will register beans via Spring Factories,
     * Java Service Provider Interface (SPI), or expect them to be manually registered
     * in the Bean Factory.
     *
     * @param registry the {@link BeanDefinitionRegistry} to register bean definitions with
     * @param beanType the type of beans to register
     * @param sources  the array of {@link BeanSource}s indicating where to look for bean implementations
     */
    private void registerBeansFromSources(BeanDefinitionRegistry registry, Class<?> beanType, BeanSource[] sources) {
        Map<Class<?>, String> beanTypesAndNames = registerBeans(this.beanFactory, sources, beanType);
        logger.trace("Registered {} implementation(s) from the sources : {} : {}", beanType, sources, beanTypesAndNames);
    }

    /**
     * Registers beans of the specified type discovered via the Java Service Provider Interface (SPI).
     * <p>
     * This method uses {@link ServiceLoader} to find implementations of the given {@code beanType}
     * and registers each discovered class as a bean definition in the registry.
     *
     * @param registry the {@link BeanDefinitionRegistry} to register bean definitions with
     * @param beanType the interface or abstract class whose implementations are to be discovered and registered
     */
    private void registerJavaServiceProviderBeans(BeanDefinitionRegistry registry, Class<?> beanType) {
        Set<? extends Class<?>> serviceClasses = getServiceClasses(beanType, this.classLoader);
        for (Class<?> serviceClass : serviceClasses) {
            registerBeanDefinition(registry, serviceClass);
        }
    }

    /**
     * Register the {@link BeanDefinition} of {@link InterceptingExecutorInterceptor} if Any {@link ExecutorFilter}  or
     * {@link ExecutorInterceptor} bean is present.
     *
     * @param registry {@link BeanDefinitionRegistry}
     * @see ExecutorFilter
     * @see ExecutorInterceptor
     * @see InterceptingExecutorInterceptor
     */
    private void registerInterceptingExecutorInterceptorIfRequired(BeanDefinitionRegistry registry) {
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

    /**
     * Convert an array of {@code "key = value"} strings into a {@link Properties} object.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Properties props = MyBatisBeanDefinitionRegistrar.stringArrayToProperties(new String[]{
     *       "jdbc.driver = org.h2.Driver",
     *       "jdbc.url    = jdbc:h2:mem:test"
     *   });
     * }</pre>
     *
     * @param lines an array of {@code "key=value"} strings
     * @return a {@link Properties} populated from the given lines; never {@code null}
     * @throws IllegalArgumentException if any line does not contain exactly one {@code =} separator
     */
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

    /**
     * Set a package-based property value on the builder from the annotation attributes, joining
     * multiple packages with a line separator when more than one is provided.
     *
     * @param builder       the {@link BeanDefinitionBuilder}
     * @param attributes    the resolved annotation attributes
     * @param attributeName the name of the attribute holding the package(s)
     */
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

    /**
     * Resolve the Spring bean name from the annotation attribute and set it as a
     * {@link RuntimeBeanReference} property value on the builder.
     *
     * @param builder       the {@link BeanDefinitionBuilder}
     * @param attributes    the resolved annotation attributes
     * @param attributeName the name of the attribute holding the bean name (or {@code *} for primary)
     * @param beanType      the expected type of the referenced bean, used for primary-bean lookup
     */
    void setBeanReferencePropertyValue(BeanDefinitionBuilder builder, AnnotationAttributes attributes, String attributeName, Class<?> beanType) {
        String beanName = attributes.getString(attributeName);
        logger.trace("Try to set the Spring Bean[{} , name : '{}'] Reference property value by the attribute[name : '{}']", beanType, beanName, attributeName);
        setBeanReferencePropertyValue(builder, attributeName, beanName, beanType);
    }

    /**
     * Resolve multiple Spring bean names from the annotation attribute and add each as a
     * {@link RuntimeBeanReference} property value on the builder.
     *
     * @param builder       the {@link BeanDefinitionBuilder}
     * @param attributes    the resolved annotation attributes
     * @param attributeName the name of the attribute holding the bean name(s)
     * @param beanType      the expected type of each referenced bean
     */
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

    /**
     * Set a {@link RuntimeBeanReference} property on the builder, resolving the primary bean when
     * the name is the wildcard {@code *}.
     *
     * @param builder       the {@link BeanDefinitionBuilder}
     * @param attributeName the property name to set
     * @param beanName      the explicit bean name, {@code *} for primary, or blank to skip
     * @param beanType      the type used for primary-bean lookup when the name is {@code *}
     */
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

    /**
     * Set a {@link RuntimeBeanReference} for the named bean as a property on the builder.
     *
     * @param builder       the {@link BeanDefinitionBuilder}
     * @param attributeName the property name to set
     * @param beanName      the name of the bean to reference; must not be blank
     */
    void setBeanReferencePropertyValue(BeanDefinitionBuilder builder, String attributeName, String beanName) {
        setPropertyValue(builder, attributeName, new RuntimeBeanReference(beanName));
    }

    /**
     * Find the single bean name for the given type: returns the sole name, the primary-bean name
     * (when multiple beans are registered), or {@code null} when none is found.
     *
     * @param beanType the bean type to look up
     * @return the resolved bean name or {@code null}
     */
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

    /**
     * Retrieve all Spring bean names (including non-singleton and factory beans) of the given type.
     *
     * @param beanType the type to look up
     * @return the array of bean names; never {@code null}, may be empty
     */
    String[] getBeanNamesByType(Class<?> beanType) {
        ConfigurableListableBeanFactory beanFactory = super.getBeanFactory();
        return getBeanNames(beanFactory, beanType, true);
    }

    /**
     * Set a property value on the builder, skipping the attribute when its value equals the
     * supplied {@code defaultValue}.
     *
     * @param builder       the {@link BeanDefinitionBuilder}
     * @param attributes    the resolved annotation attributes
     * @param attributeName the attribute (and property) name
     * @param defaultValue  the value to compare against; when equal the property is not set
     */
    void setPropertyValue(BeanDefinitionBuilder builder, AnnotationAttributes attributes, String attributeName, Object defaultValue) {
        Object value = attributes.get(attributeName);
        if (Objects.equals(defaultValue, value)) {
            logger.trace("Default property value[{}] will ignored the attribute[name : '{}']", defaultValue, attributeName);
            return;
        }
        setPropertyValue(builder, attributeName, value);
    }

    /**
     * Set a property value on the builder directly from the annotation attribute value.
     *
     * @param builder       the {@link BeanDefinitionBuilder}
     * @param attributes    the resolved annotation attributes
     * @param attributeName the attribute (and property) name
     */
    void setPropertyValue(BeanDefinitionBuilder builder, AnnotationAttributes attributes, String attributeName) {
        Object attributeValue = attributes.get(attributeName);
        setPropertyValue(builder, attributeName, attributeValue);
    }

    /**
     * Add a property value directly to the builder.
     *
     * @param builder        the {@link BeanDefinitionBuilder}
     * @param attributeName  the property name
     * @param attributeValue the property value
     */
    void setPropertyValue(BeanDefinitionBuilder builder, String attributeName, Object attributeValue) {
        logger.trace("Set the BeanDefinition[{}] property[name : '{}'  , value : '{}']", builder.getRawBeanDefinition(), attributeName, attributeValue);
        builder.addPropertyValue(attributeName, attributeValue);
    }
}
