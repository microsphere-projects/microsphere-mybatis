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

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * {@link ComponentResolver} for {@link Environment}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Environment
 * @since 1.0.0
 */
public class EnvironmentResolver extends AbstractComponentResolver<Environment> {

    public static final EnvironmentResolver INSTANCE = new EnvironmentResolver();

    @Override
    protected Environment doResolve(ExtensionContext extensionContext, Class<?> componentType) throws Exception {
        Configuration configuration = ConfigurationResolver.INSTANCE.resolve(extensionContext);
        return configuration.getEnvironment();
    }
}
