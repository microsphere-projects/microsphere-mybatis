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

import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Properties;

import static io.microsphere.mybatis.test.junit.jupiter.resolver.ComponentResolver.get;

/**
 * {@link ComponentResolver} for {@link Properties}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Properties
 * @since 1.0.0
 */
public class PropertiesResolver extends AbstractComponentResolver<Properties> {

    public static final PropertiesResolver INSTANCE = new PropertiesResolver();

    @Override
    public Properties doResolve(ExtensionContext extensionContext, Class<?> componentType) throws Exception {
        return get(extensionContext, Properties.class);
    }
}
