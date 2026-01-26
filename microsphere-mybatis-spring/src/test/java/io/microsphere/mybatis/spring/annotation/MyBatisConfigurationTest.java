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

import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.executor.loader.CglibProxyFactory;
import org.apache.ibatis.io.DefaultVFS;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.scripting.LanguageDriverRegistry;
import org.apache.ibatis.scripting.defaults.RawLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.EnumTypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.mybatis.spring.annotation.MyBatisBeanDefinitionRegistrar.stringArrayToProperties;
import static io.microsphere.mybatis.spring.annotation.MyBatisConfigurationBeanDefintionRegistrar.CONFIGURATION_BEAN_NAME;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static io.microsphere.util.StringUtils.EMPTY_STRING;
import static org.apache.ibatis.mapping.ResultSetType.DEFAULT;
import static org.apache.ibatis.mapping.ResultSetType.FORWARD_ONLY;
import static org.apache.ibatis.session.AutoMappingBehavior.NONE;
import static org.apache.ibatis.session.AutoMappingUnknownColumnBehavior.WARNING;
import static org.apache.ibatis.session.ExecutorType.REUSE;
import static org.apache.ibatis.session.LocalCacheScope.STATEMENT;
import static org.apache.ibatis.type.JdbcType.UNDEFINED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link MyBatisConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MyBatisConfiguration
 * @since 1.0.0
 */
@MyBatisConfiguration
class MyBatisConfigurationTest {

    @Test
    void testDefaultConfig() {
        testInSpringContainer(context -> {
            Configuration configuration = context.getBean(CONFIGURATION_BEAN_NAME, Configuration.class);
            assertNotNull(configuration);
            MyBatisConfiguration annotation = DefaultConfig.class.getAnnotation(MyBatisConfiguration.class);
            assertConfiguration(annotation, configuration);
        }, DefaultConfig.class);
    }

    @Test
    void testFullConfig() {
        testInSpringContainer(context -> {
            Configuration configuration = context.getBean(CONFIGURATION_BEAN_NAME, Configuration.class);
            assertNotNull(configuration);
            MyBatisConfiguration annotation = FullConfig.class.getAnnotation(MyBatisConfiguration.class);
            assertConfiguration(annotation, configuration);
        }, FullConfig.class);
    }

    void assertConfiguration(MyBatisConfiguration annotation, Configuration configuration) {
        assertEquals(annotation.cacheEnabled(), configuration.isCacheEnabled());
        assertEquals(annotation.lazyLoadingEnabled(), configuration.isLazyLoadingEnabled());
        assertEquals(annotation.aggressiveLazyLoading(), configuration.isAggressiveLazyLoading());
        // always true
        assertEquals(annotation.multipleResultSetsEnabled(), configuration.isMultipleResultSetsEnabled());
        assertEquals(annotation.useColumnLabel(), configuration.isUseColumnLabel());
        assertEquals(annotation.useGeneratedKeys(), configuration.isUseGeneratedKeys());
        assertEquals(annotation.autoMappingBehavior(), configuration.getAutoMappingBehavior());
        assertEquals(annotation.autoMappingUnknownColumnBehavior(), configuration.getAutoMappingUnknownColumnBehavior());
        assertEquals(annotation.defaultExecutorType(), configuration.getDefaultExecutorType());
        assertInt(annotation::defaultStatementTimeout, configuration::getDefaultStatementTimeout);
        assertInt(annotation::defaultFetchSize, configuration::getDefaultFetchSize);

        ResultSetType resultSetType = annotation.defaultResultSetType();
        if (DEFAULT.equals(resultSetType)) {
            assertNull(configuration.getDefaultResultSetType());
        } else {
            assertEquals(annotation.defaultResultSetType(), configuration.getDefaultResultSetType());
        }
        assertEquals(annotation.safeRowBoundsEnabled(), configuration.isSafeRowBoundsEnabled());
        assertEquals(annotation.safeResultHandlerEnabled(), configuration.isSafeResultHandlerEnabled());
        assertEquals(annotation.mapUnderscoreToCamelCase(), configuration.isMapUnderscoreToCamelCase());
        assertEquals(annotation.localCacheScope(), configuration.getLocalCacheScope());
        assertEquals(annotation.jdbcTypeForNull(), configuration.getJdbcTypeForNull());
        assertEquals(ofSet(annotation.lazyLoadTriggerMethods()), configuration.getLazyLoadTriggerMethods());

        LanguageDriverRegistry languageRegistry = configuration.getLanguageRegistry();
        assertEquals(annotation.defaultScriptingLanguage(), languageRegistry.getDefaultDriverClass());

        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        assertEquals(annotation.defaultEnumTypeHandler(), typeHandlerRegistry.getTypeHandler(Enum.class).getClass());
        assertEquals(annotation.callSettersOnNulls(), configuration.isCallSettersOnNulls());
        assertEquals(annotation.returnInstanceForEmptyRow(), configuration.isReturnInstanceForEmptyRow());

        String logPrefix = annotation.logPrefix();
        if (EMPTY_STRING.equals(logPrefix)) {
            assertNull(configuration.getLogPrefix());
        } else {
            assertEquals(logPrefix, configuration.getLogPrefix());
        }

        Class<? extends Log> logImpl = annotation.logImpl();
        if (Log.class.equals(logImpl)) {
            assertNull(configuration.getLogImpl());
        } else {
            assertEquals(annotation.logImpl(), configuration.getLogImpl());
        }

        assertEquals(annotation.proxyFactory(), configuration.getProxyFactory().getClass());

        Class<? extends VFS> vfsImpl = annotation.vfsImpl();
        if (VFS.class.equals(vfsImpl)) {
            assertNull(configuration.getVfsImpl());
        } else {
            assertEquals(annotation.vfsImpl(), configuration.getVfsImpl());
        }

        assertEquals(annotation.useActualParamName(), configuration.isUseActualParamName());

        Class<?> configurationFactory = annotation.configurationFactory();
        if (Object.class.equals(configurationFactory)) {
            assertNull(configuration.getConfigurationFactory());
        } else {
            assertEquals(configurationFactory, configuration.getConfigurationFactory());
        }

        assertEquals(annotation.shrinkWhitespacesInSql(), configuration.isShrinkWhitespacesInSql());

        Class<?> defaultSqlProviderType = annotation.defaultSqlProviderType();
        if (Object.class.equals(defaultSqlProviderType)) {
            assertNull(configuration.getDefaultSqlProviderType());
        } else {
            assertEquals(defaultSqlProviderType, configuration.getDefaultSqlProviderType());
        }

        assertEquals(annotation.nullableOnForEach(), configuration.isNullableOnForEach());
        assertEquals(annotation.argNameBasedConstructorAutoMapping(), configuration.isArgNameBasedConstructorAutoMapping());

        String[] variables = annotation.variables();
        assertEquals(stringArrayToProperties(variables), configuration.getVariables());

        String databaseId = annotation.databaseId();
        if (EMPTY_STRING.equals(databaseId)) {
            assertNull(configuration.getDatabaseId());
        } else {
            assertEquals(databaseId, configuration.getDatabaseId());
        }
    }

    void assertInt(Supplier<? extends Number> expected, Supplier<? extends Number> actual) {
        int expectedInt = expected.get().intValue();
        Number actualNumber = actual.get();
        if (expectedInt == -1) {
            assertNull(actualNumber);
        } else {
            int actualInt = actualNumber.intValue();
            assertEquals(expectedInt, actualInt);
        }
    }

    @MyBatisConfiguration
    static class DefaultConfig {
    }

    @MyBatisConfiguration(
            cacheEnabled = false,
            lazyLoadingEnabled = true,
            aggressiveLazyLoading = true,
            multipleResultSetsEnabled = false,
            useColumnLabel = false,
            useGeneratedKeys = true,
            autoMappingBehavior = NONE,
            autoMappingUnknownColumnBehavior = WARNING,
            defaultExecutorType = REUSE,
            defaultStatementTimeout = 10,
            defaultFetchSize = 1,
            defaultResultSetType = FORWARD_ONLY,
            safeRowBoundsEnabled = true,
            safeResultHandlerEnabled = false,
            mapUnderscoreToCamelCase = true,
            localCacheScope = STATEMENT,
            jdbcTypeForNull = UNDEFINED,
            lazyLoadTriggerMethods = "equals",
            defaultScriptingLanguage = RawLanguageDriver.class,
            defaultEnumTypeHandler = EnumTypeHandlerExt.class,
            callSettersOnNulls = true,
            returnInstanceForEmptyRow = true,
            logPrefix = "test-",
            logImpl = Slf4jImpl.class,
            proxyFactory = CglibProxyFactory.class,
            vfsImpl = DefaultVFS.class,
            useActualParamName = false,
            configurationFactory = FullConfig.class,
            shrinkWhitespacesInSql = true,
            defaultSqlProviderType = SelectProvider.class,
            nullableOnForEach = true,
            argNameBasedConstructorAutoMapping = true,
            variables = {
                    "name = value"
            },
            databaseId = "test-database"
    )
    static class FullConfig {

        public static Configuration getConfiguration() {
            return new Configuration();
        }
    }

    public static class EnumTypeHandlerExt<E extends Enum<E>> extends EnumTypeHandler<E> {

        public EnumTypeHandlerExt(Class<E> type) {
            super(type);
        }
    }
}