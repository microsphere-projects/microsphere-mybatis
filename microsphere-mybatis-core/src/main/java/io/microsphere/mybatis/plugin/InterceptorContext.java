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
package io.microsphere.mybatis.plugin;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.plugin.Interceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static io.microsphere.util.Assert.assertNotNull;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

/**
 * The Context of {@link Interceptor}
 *
 * @param <T> the type of intercepted target, e.g: {@link Executor}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Interceptor
 * @since 1.0.0
 */
public class InterceptorContext<T> {

    /**
     * The intercepted target, e.g: {@link Executor}
     */
    private final T target;

    /**
     * the copy {@link Map} of {@link Interceptor#setProperties(Properties)}
     */
    @Nullable
    private final Properties properties;

    /**
     * The start time of the execution.
     */
    @Nullable
    private Long startTime;

    /**
     * The attributes
     */
    @Nullable
    private Map<String, Object> attributes;

    /**
     * Constructor
     *
     * @param target     The intercepted target, e.g: {@link Executor}
     * @param properties the reference of {@link Interceptor#setProperties(Properties)}
     */
    public InterceptorContext(@Nonnull T target, @Nullable Properties properties) {
        assertNotNull(target, () -> "The 'target' argument must not be null!");
        this.target = target;
        this.properties = properties;
    }

    /**
     * Get the intercepted target, e.g: {@link Executor}
     *
     * @return non-null
     */
    @Nonnull
    public T getTarget() {
        return target;
    }

    /**
     * Get the reference of {@link Interceptor#setProperties(Properties)}
     *
     * @return <code>null</code> if {@link Interceptor#setProperties(Properties)} was not set
     */
    @Nullable
    public Properties getProperties() {
        return properties;
    }

    /**
     * Set the start time of the execution.
     *
     * @param startTime the start time of the execution
     */
    public InterceptorContext setStartTime(Long startTime) {
        this.startTime = startTime;
        return this;
    }


    /**
     * Get the start time of the execution.
     *
     * @return <code>null</code> if {@link #setStartTime(Long)} method will be invoked
     */
    @Nullable
    public Long getStartTime() {
        return startTime;
    }

    /**
     * Set the attribute name and value.
     *
     * @param name  the attribute name
     * @param value the attribute value
     * @return {@link InterceptorContext}
     */
    public InterceptorContext setAttribute(String name, Object value) {
        Map<String, Object> attributes = getOrCreateAttributes();
        attributes.put(name, value);
        return this;
    }

    /**
     * Check whether the attribute exists by name.
     *
     * @param name the attribute name
     * @return <code>true</code> if exists, otherwise <code>false</code>
     */
    public boolean hasAttribute(String name) {
        Map<String, Object> attributes = getOrCreateAttributes();
        return attributes.containsKey(name);
    }

    /**
     * Get the attribute value by name.
     *
     * @param name the attribute name
     * @return the attribute value if found, otherwise <code>null</code>
     */
    public <T> T getAttribute(String name) {
        Map<String, Object> attributes = getOrCreateAttributes();
        return (T) attributes.get(name);
    }

    /**
     * Get the attribute value by name.
     *
     * @param name         the attribute name
     * @param defaultValue the default value of attribute
     * @return the attribute value if found, otherwise <code>defaultValue</code>
     */
    public <T> T getAttribute(String name, T defaultValue) {
        Map<String, Object> attributes = getOrCreateAttributes();
        return (T) attributes.getOrDefault(name, defaultValue);
    }

    /**
     * Remove the attribute by name.
     *
     * @param name the attribute name
     * @return the attribute value if removed, otherwise <code>null</code>
     */
    public <T> T removeAttribute(String name) {
        Map<String, Object> attributes = getOrCreateAttributes();
        return (T) attributes.remove(name);
    }

    /**
     * Remove all attributes.
     *
     * @return {@link InterceptorContext}
     */
    public InterceptorContext removeAttributes() {
        Map<String, Object> attributes = this.attributes;
        if (attributes != null) {
            attributes.clear();
        }
        return this;
    }

    /**
     * Get the attributes.
     *
     * @return the read-only attributes
     */
    public Map<String, Object> getAttributes() {
        Map<String, Object> attributes = this.attributes;
        if (attributes == null) {
            return emptyMap();
        }
        return unmodifiableMap(attributes);
    }

    protected Map<String, Object> getOrCreateAttributes() {
        Map<String, Object> attributes = this.attributes;
        if (attributes == null) {
            attributes = new HashMap<>();
            this.attributes = attributes;
        }
        return attributes;
    }

    @Override
    public String toString() {
        return "InterceptorContext{" +
                "target=" + target +
                ", properties=" + properties +
                ", startTime=" + startTime +
                ", attributes=" + attributes +
                '}';
    }
}
