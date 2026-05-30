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

import io.microsphere.logging.Logger;
import io.microsphere.mybatis.executor.ExecutorFilter;
import io.microsphere.mybatis.executor.ExecutorInterceptor;
import io.microsphere.mybatis.executor.InterceptingExecutor;
import io.microsphere.mybatis.executor.InterceptorsExecutorFilterAdapter;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static io.microsphere.collection.MapUtils.isNotEmpty;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.mybatis.executor.Executors.getDelegate;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.Assert.assertNoNullElements;
import static io.microsphere.util.Assert.assertTrue;
import static io.microsphere.util.PriorityComparator.INSTANCE;
import static java.lang.System.arraycopy;
import static java.util.Arrays.sort;
import static java.util.Collections.addAll;

/**
 * {@link Interceptor} class for {@link Executor} delegates to {@link ExecutorInterceptor} instances
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Create an interceptor with filters and interceptors
 *   ExecutorFilter loggingFilter = new LoggingExecutorFilter();
 *   ExecutorInterceptor loggingInterceptor = new LoggingExecutorInterceptor();
 *   InterceptingExecutorInterceptor interceptor =
 *       new InterceptingExecutorInterceptor(new ExecutorFilter[]{loggingFilter}, loggingInterceptor);
 *
 *   // Register the interceptor as a MyBatis plugin
 *   configuration.addInterceptor(interceptor);
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Interceptor
 * @since 1.0.0
 */
public class InterceptingExecutorInterceptor implements Interceptor {

    private static final Logger logger = getLogger(InterceptingExecutorInterceptor.class);

    private final ExecutorFilter[] executorFilters;

    private Properties properties;

    /**
     * Constructor with {@link ExecutorFilter} array and optional {@link ExecutorInterceptor} instances.
     *
     * @param executorFilters      the array of {@link ExecutorFilter} instances; must not be empty when no
     *                             {@code executorInterceptors} are provided
     * @param executorInterceptors optional {@link ExecutorInterceptor} instances; at least one filter or interceptor
     *                             must be supplied
     * @throws IllegalArgumentException if both arrays are empty or any element is {@code null}
     */
    public InterceptingExecutorInterceptor(ExecutorFilter[] executorFilters, ExecutorInterceptor... executorInterceptors) {
        int executorFiltersCount = length(executorFilters);
        int executorInterceptorsCount = length(executorInterceptors);
        boolean hasExecutorInterceptors = executorInterceptorsCount > 0;
        assertTrue(executorFiltersCount > 0 || hasExecutorInterceptors, () -> "No filter or interceptor for Executor");
        assertNoNullElements(executorFilters, () -> "Any element of filters must not be null!");

        int size = hasExecutorInterceptors ? executorFiltersCount + 1 : executorFiltersCount;
        ExecutorFilter[] allExecutorFilters = new ExecutorFilter[size];

        arraycopy(executorFilters, 0, allExecutorFilters, 0, executorFiltersCount);

        if (hasExecutorInterceptors) {
            allExecutorFilters[executorFiltersCount] = new InterceptorsExecutorFilterAdapter(executorInterceptors);
        }
        // sort by its priority
        sort(allExecutorFilters, INSTANCE);

        this.executorFilters = allExecutorFilters;
    }

    /**
     * This method should not be called directly; it logs a warning and delegates to
     * {@link Invocation#proceed()}.  The actual interception is performed by
     * {@link #plugin(Object)} which wraps the {@link Executor} in an {@link InterceptingExecutor}.
     *
     * @param invocation the {@link Invocation}
     * @return the result of {@link Invocation#proceed()}
     * @throws Throwable any exception thrown by the downstream invocation
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        logger.warn("The intercept method should not be invoked : {}", invocation);
        return invocation.proceed();
    }

    /**
     * Wraps the target {@link Executor} in an {@link InterceptingExecutor} so that all registered
     * {@link ExecutorFilter} and {@link ExecutorInterceptor} instances are applied.  Non-{@link Executor}
     * targets are returned unchanged.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   InterceptingExecutorInterceptor interceptor =
     *       new InterceptingExecutorInterceptor(new ExecutorFilter[]{new LoggingExecutorFilter()});
     *   configuration.addInterceptor(interceptor);
     *   // MyBatis will call plugin(executor) automatically when opening a session
     * }</pre>
     *
     * @param target the MyBatis component (e.g. {@link Executor}) to potentially wrap
     * @return the wrapped {@link InterceptingExecutor} (or {@link CachingExecutor} wrapping it),
     * or {@code target} unchanged when it is not an {@link Executor}
     */
    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor executor) {

            boolean isCachingExecutor = executor instanceof CachingExecutor;
            Executor delegate = executor;
            if (isCachingExecutor) {
                CachingExecutor cachingExecutor = (CachingExecutor) executor;
                delegate = getDelegate(cachingExecutor);
            }

            Properties newProperties = new Properties();
            ExecutorFilter[] executorFilters = this.executorFilters;

            if (delegate instanceof InterceptingExecutor previousInterceptingExecutor) {
                List<ExecutorFilter> newExecutorFiltersList = new LinkedList<>();
                delegate = previousInterceptingExecutor.getDelegate();
                // merge Properties
                Properties previousProperties = previousInterceptingExecutor.getProperties();
                if (isNotEmpty(previousProperties)) {
                    newProperties.putAll(previousProperties);
                }

                // merge ExecutorFilters
                addAll(newExecutorFiltersList, previousInterceptingExecutor.getExecutorFilters());
                addAll(newExecutorFiltersList, executorFilters);
                executorFilters = newExecutorFiltersList.toArray(new ExecutorFilter[0]);
            }

            if (isNotEmpty(this.properties)) {
                newProperties.putAll(this.properties);
            }
            newProperties = newProperties.isEmpty() ? null : newProperties;

            InterceptingExecutor interceptingExecutor = new InterceptingExecutor(delegate, newProperties, executorFilters);
            return isCachingExecutor ? new CachingExecutor(interceptingExecutor) : interceptingExecutor;
        }
        logger.trace("The non-executor [{}] instance simply returns without any dynamic proxy interception", target);
        return target;
    }

    /**
     * Stores the given {@link Properties} so they can later be made available to filters and
     * interceptors via {@link InterceptorContext#getProperties()}.
     *
     * @param properties the {@link Properties} set by the MyBatis configuration; may be {@code null}
     */
    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
        logger.trace("setProperties : {}", properties);
    }
}
