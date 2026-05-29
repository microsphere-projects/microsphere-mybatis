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

package io.microsphere.mybatis.test.junit.jupiter.resolver;

import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Collection;

import static io.microsphere.lang.function.ThrowableSupplier.execute;

/**
 * {@link ComponentResolver} for MyBatis Mapper
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ComponentResolver
 * @since 1.0.0
 */
public class MapperComponentResolver extends AbstractComponentResolver<Object> {

    public static final MapperComponentResolver INSTANCE = new MapperComponentResolver();

    @Override
    public boolean supportsStaticField() {
        return false;
    }

    @Override
    protected Object doResolve(ExtensionContext extensionContext, Class<?> componentType) throws Exception {
        SqlSession sqlSession = SqlSessionResolver.INSTANCE.resolve(extensionContext);
        return sqlSession.getMapper(componentType);
    }

    @Override
    public boolean isComponentType(ExtensionContext extensionContext, Class<?> componentType) {
        return execute(() -> {
            Configuration configuration = ConfigurationResolver.INSTANCE.resolve(extensionContext);
            MapperRegistry mapperRegistry = configuration.getMapperRegistry();
            Collection<Class<?>> mappers = mapperRegistry.getMappers();
            return mappers.contains(componentType);
        });
    }
}
