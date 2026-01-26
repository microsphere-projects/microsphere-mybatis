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

package io.microsphere.mybatis.constants;

import io.microsphere.annotation.ConfigurationProperty;

import static io.microsphere.annotation.ConfigurationProperty.APPLICATION_SOURCE;
import static io.microsphere.constants.PropertyConstants.ENABLED_PROPERTY_NAME;
import static io.microsphere.constants.PropertyConstants.MICROSPHERE_PROPERTY_NAME_PREFIX;
import static io.microsphere.constants.SymbolConstants.DOT;

/**
 * The constants class for MyBatis
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationProperty
 * @since 1.0.0
 */
public interface PropertyConstants {

    /**
     * The property Name prefix of Microsphere MyBatis: "microsphere.mybatis"
     */
    String MICROSPHERE_MYBATIS_PROPERTY_NAME_PREFIX = MICROSPHERE_PROPERTY_NAME_PREFIX + "mybatis";

    /**
     * The String presentation on default value of property name of Microsphere MyBatis enalbed : "true"
     */
    String DEFAULT_MICROSPHERE_MYBATIS_ENABLED = "true";

    /**
     * The property name of Microsphere MyBatis enalbed : "microsphere.mybatis.enabled"
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = DEFAULT_MICROSPHERE_MYBATIS_ENABLED,
            source = APPLICATION_SOURCE
    )
    String MICROSPHERE_MYBATIS_ENABLED_PROPERTY_NAME = MICROSPHERE_MYBATIS_PROPERTY_NAME_PREFIX + DOT + ENABLED_PROPERTY_NAME;
}
