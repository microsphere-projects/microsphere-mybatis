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

import io.microsphere.constants.SymbolConstants;
import io.microsphere.mybatis.executor.ExecutorFilter;
import io.microsphere.mybatis.executor.ExecutorInterceptor;
import io.microsphere.mybatis.executor.InterceptingExecutor;
import io.microsphere.mybatis.plugin.InterceptingExecutorInterceptor;
import io.microsphere.util.StringUtils;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.PropertySources;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Properties;

import static io.microsphere.constants.SymbolConstants.WILDCARD;
import static io.microsphere.util.StringUtils.EMPTY_STRING;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.ibatis.session.ExecutorType.SIMPLE;

/**
 * Enables Spring's annotation-driven MyBatis capability, similar to the offical
 * <a href="https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/#Configuration">MyBatis Spring Boot Starter</a>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MyBatisBeanDefinitionRegistrar
 * @see MyBatisConfiguration
 * @see MapperScan
 * @see MapperScans
 * @see Configuration
 * @see SqlSessionFactoryBean
 * @see SqlSessionTemplate
 * @since 1.0.0
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
@Inherited
@Import(MyBatisBeanDefinitionRegistrar.class)
public @interface EnableMyBatis {

    /**
     * The Spring Bean name of {@link DataSource}
     *
     * @return the {@link SymbolConstants#WILDCARD "*"} as default, indicates primary bean will be applied.
     * If the value is {@link StringUtils#isBlank(String) blank}, indicates no bean specified.
     * (the placeholders in the value will be resolved)
     * @see DataSource
     * @see SqlSessionFactoryBean#setDataSource(DataSource)
     */
    String dataSource() default WILDCARD;

    /**
     * Location of MyBatis xml config file, for example,
     * {@code "classpath:/com/acme/config.xml"}
     *
     * @return the empty string as default.If the value is blank, it indicates primary bean of {@link Configuration}
     * will be applied. (the placeholders in the value will be resolved)
     * @see SqlSessionFactoryBean#setConfigLocation(Resource)
     * @see SqlSessionFactoryBean#setConfiguration(Configuration)
     */
    String configLocation() default EMPTY_STRING;

    /**
     * Indicates whether perform presence check of the MyBatis xml config file.
     *
     * @return <code>false</code> as default
     */
    boolean checkConfigLocation() default false;

    /**
     * Locations of Mapper xml config file, for example, {@code "classpath*:/com/acme/mapper/*.xml"} or
     * {@code "file:/path/to/mapper.xml"}.
     *
     * @return empty array as default. (the placeholders in each elements' value will be resolved)
     * @see SqlSessionFactoryBean#setMapperLocations(Resource...)
     */
    String[] mapperLocations() default {};

    /**
     * Packages to search for type aliases. (Package delimiters are “,; \t\n”)
     *
     * @return empty array as default. (the placeholders in each elements' value will be resolved)
     * @see SqlSessionFactoryBean#setTypeAliasesPackage(String)
     */
    String[] typeAliasesPackage() default {};

    /**
     * The super class for filtering type alias. If this not specifies, the MyBatis deal as type alias all classes that
     * searched from {@link #typeAliasesPackage()}.
     *
     * @return {@link Object} as default
     * @see SqlSessionFactoryBean#setTypeAliasesSuperType(Class)
     */
    Class<?> typeAliasesSuperType() default Object.class;

    /**
     * Packages to search for type handlers. (Package delimiters are “,; \t\n”)
     *
     * @return empty array as default. (the placeholders in each elements' value will be resolved)
     * @see SqlSessionFactoryBean#setTypeHandlersPackage(String)
     */
    String[] typeHandlersPackage() default {};

    /**
     * Executor type: {@link ExecutorType#SIMPLE}, {@link ExecutorType#REUSE}, {@link ExecutorType#BATCH}
     *
     * @return {@link ExecutorType#SIMPLE} as default
     * @see SqlSessionTemplate#SqlSessionTemplate(SqlSessionFactory, ExecutorType)
     */
    ExecutorType executorType() default SIMPLE;

    /**
     * The {@link VFS} class.
     *
     * @return {@link VFS} as the default, indicates no {@link VFS} specified.
     * @see SqlSessionFactoryBean#setVfs(Class)
     */
    Class<? extends VFS> vfs() default VFS.class;

    /**
     * The default scripting language driver class. This feature requires to use together with mybatis-spring 2.0.2+.
     *
     * @return {@link LanguageDriver} as the default, indicates no {@link LanguageDriver} specified.
     * @see SqlSessionFactoryBean#setDefaultScriptingLanguageDriver(Class)
     */
    Class<? extends LanguageDriver> defaultScriptingLanguageDriver() default LanguageDriver.class;

    /**
     * Externalized properties for MyBatis configuration. Specified properties can be used as placeholder on MyBatis config file and Mapper file.
     * For detail see the <a href="https://mybatis.org/mybatis-3/configuration.html#properties">MyBatis reference page</a>.
     *
     * @return empty array as default. (the placeholders in each elements' value will be resolved)
     * @see SqlSessionFactoryBean#setConfigurationProperties(Properties)
     */
    String[] configurationProperties() default {};

    /**
     * Indicates the externalized properties for MyBatis configuration importing Spring {@link PropertySources}.
     * The properties from {@link #configurationProperties()} method will overrides the Spring {@link PropertySources}
     * if present.
     *
     * @return <code>false</code> as default.
     * @see #configurationProperties()
     * @see PropertySources
     */
    boolean configurationPropertiesImportPropertySources() default false;

    /**
     * The Spring Bean name of {@link ObjectWrapperFactory}
     *
     * @return the {@link StringUtils#EMPTY_STRING empty string} as default.
     * If the value is {@link StringUtils#isBlank(String) blank}, indicates no bean specified.
     * If the value is {@link SymbolConstants#WILDCARD "*"}, the primray bean will be applied.
     * (the placeholders in the value will be resolved)
     * @see SqlSessionFactoryBean#setObjectWrapperFactory(ObjectWrapperFactory)
     * @see ObjectWrapperFactory
     * @since MyBatis Spring 1.1.2
     */
    String objectWrapperFactory() default EMPTY_STRING;

    /**
     * The Spring Bean name of {@link DatabaseIdProvider}
     *
     * @return the {@link SymbolConstants#WILDCARD "*"} as default, indicates primary bean will be applied.
     * If the value is {@link StringUtils#isBlank(String) blank}, indicates no bean specified.
     * (the placeholders in the value will be resolved)
     * @see SqlSessionFactoryBean#setDatabaseIdProvider(DatabaseIdProvider)
     * @see DatabaseIdProvider
     * @since MyBatis Spring 1.1.0
     */
    String databaseIdProvider() default WILDCARD;

    /**
     * The Spring Bean name of {@link Cache}
     *
     * @return the {@link StringUtils#EMPTY_STRING empty string} as default.
     * If the value is {@link StringUtils#isBlank(String) blank}, indicates no bean specified.
     * If the value is {@link SymbolConstants#WILDCARD "*"}, the primray bean will be applied.
     * (the placeholders in the value will be resolved)
     * @see SqlSessionFactoryBean#setCache(Cache)
     * @see Cache
     * @since MyBatis Spring 1.1.0
     */
    String cache() default EMPTY_STRING;

    /**
     * The Spring Bean names of {@link Interceptor} as plugins
     *
     * @return the {@link SymbolConstants#WILDCARD "*"} as default, indicates all beans will be applied.
     * If the value is empty string array(incluing the {@link StringUtils#isBlank(String) blank} string element),
     * it indicates no bean specified. (the placeholders in each elements' value will be resolved)
     * @see SqlSessionFactoryBean#setPlugins(Interceptor...)
     * @see Interceptor
     * @since MyBatis Spring 1.0.1
     */
    String[] plugins() default WILDCARD;

    /**
     * The Spring Bean names of {@link TypeHandler}
     *
     * @return the {@link SymbolConstants#WILDCARD "*"} as default, indicates all beans will be applied.
     * If the value is empty string array(incluing the {@link StringUtils#isBlank(String) blank} string element),
     * it indicates no bean specified. (the placeholders in each elements' value will be resolved)
     * @see SqlSessionFactoryBean#setTypeHandlers(TypeHandler...)
     * @see TypeHandler
     * @since MyBatis Spring 1.0.1
     */
    String[] typeHandlers() default WILDCARD;

    /**
     * The Spring Bean names of {@link LanguageDriver}
     *
     * @return the {@link SymbolConstants#WILDCARD "*"} as default, indicates all beans will be applied.
     * If the value is empty string array(incluing the {@link StringUtils#isBlank(String) blank} string element),
     * it indicates no bean specified. (the placeholders in each elements' value will be resolved)
     * @see SqlSessionFactoryBean#setScriptingLanguageDrivers(LanguageDriver...)
     * @see LanguageDriver
     * @since MyBatis Spring 2.0.2
     */
    String[] scriptingLanguageDrivers() default WILDCARD;

    /**
     * Indicate whether the methods of MyBatis {@link Executor} should be intercepted.
     * If <code>true</code>, {@link ExecutorFilter} and {@link ExecutorInterceptor} beans
     * will be initialized and then be invoked around {@link Method} being executed.
     *
     * @see Plugin
     * @see Executor
     * @see ExecutorFilter
     * @see ExecutorInterceptor
     * @see InterceptingExecutor
     * @see InterceptingExecutorInterceptor
     */
    boolean interceptExecutor() default true;
}