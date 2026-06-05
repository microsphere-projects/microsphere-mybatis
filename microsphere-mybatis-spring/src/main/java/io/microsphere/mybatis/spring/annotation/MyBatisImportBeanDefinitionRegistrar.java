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
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.microsphere.constants.SeparatorConstants.LINE_SEPARATOR;
import static io.microsphere.constants.SymbolConstants.EQUAL;
import static io.microsphere.constants.SymbolConstants.WILDCARD;
import static io.microsphere.spring.beans.BeanUtils.getBeanNames;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.Assert.assertTrue;
import static io.microsphere.util.StringUtils.isBlank;
import static io.microsphere.util.StringUtils.split;
import static io.microsphere.util.StringUtils.trimAllWhitespace;
import static org.springframework.core.ResolvableType.forType;

/**
 * Abstract {@link ImportBeanDefinitionRegistrar} for Microsphere MyBatis @Enable Annotation-driven configuration
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ImportBeanDefinitionRegistrar
 * @since 1.0.0
 */
abstract class MyBatisImportBeanDefinitionRegistrar<A extends Annotation> extends BeanCapableImportCandidate
        implements ImportBeanDefinitionRegistrar {

    protected final Class<A> annotationType;

    MyBatisImportBeanDefinitionRegistrar() {
        this.annotationType = (Class<A>) forType(this.getClass())
                .as(MyBatisImportBeanDefinitionRegistrar.class)
                .getGeneric(0)
                .resolve();
    }

    @Override
    public final void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = getAnnotationAttributes(metadata, this.annotationType);
        registerBeanDefinitions(attributes, metadata, registry);
    }

    protected abstract void registerBeanDefinitions(AnnotationAttributes attributes, AnnotationMetadata metadata,
                                                    BeanDefinitionRegistry registry);


    protected void registerBeanDefinitionIfAbsent(AnnotationAttributes attributes, BeanDefinitionRegistry registry,
                                                  String beanName,
                                                  Function<AnnotationAttributes, BeanDefinition> beanDefinitionFunction) {
        if (registry.containsBeanDefinition(beanName)) {
            logger.info("The BeanDefinition named '{}' already exists. Skipping registration. {}", beanName, attributes);
            return;
        }
        BeanDefinition beanDefinition = beanDefinitionFunction.apply(attributes);
        registerBeanDefinition(registry, beanName, beanDefinition);
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
            ConfigurableListableBeanFactory beanFactory = getBeanFactory();
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