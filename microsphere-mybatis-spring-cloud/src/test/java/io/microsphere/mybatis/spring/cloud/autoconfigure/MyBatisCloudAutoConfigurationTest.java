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

package io.microsphere.mybatis.spring.cloud.autoconfigure;

import io.microsphere.mybatis.executor.LoggingExecutorFilter;
import io.microsphere.mybatis.executor.LoggingExecutorInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.actuator.FeaturesEndpoint;
import org.springframework.cloud.client.actuator.HasFeatures;
import org.springframework.cloud.client.actuator.NamedFeature;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 * {@link MyBatisCloudAutoConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MyBatisCloudAutoConfiguration
 * @since 1.0.0
 */
@SpringBootTest(classes = {
        LoggingExecutorFilter.class,
        LoggingExecutorInterceptor.class,
        MyBatisCloudAutoConfigurationTest.class
}, webEnvironment = NONE,
        properties = {
                "spring.cloud.features.enabled=true",
                "management.endpoints.web.exposure.include=*",
        }
)
@EnableAutoConfiguration
class MyBatisCloudAutoConfigurationTest {

    @Autowired
    private Map<String, HasFeatures> hasFeaturesMap;

    @Autowired
    private FeaturesEndpoint featuresEndpoint;

    @Test
    public void test() {
        assertTrue(this.hasFeaturesMap.size() > 0);
        HasFeatures hadFeatures = this.hasFeaturesMap.get("mybatis.features");
        assertNotNull(hadFeatures);

        hadFeatures = this.hasFeaturesMap.get("mybatis-spring.features");
        assertNotNull(hadFeatures);

        hadFeatures = this.hasFeaturesMap.get("mybatis-spring-boot-autoconfigure.features");
        assertNotNull(hadFeatures);

        hadFeatures = this.hasFeaturesMap.get("microsphere-mybatis-core.features");
        assertNotNull(hadFeatures);

        assertNotNull(featuresEndpoint.features());
    }

    private void assertNamedFeature(List<NamedFeature> namedFeatures, int index, String name, Class<?> type) {
        NamedFeature namedFeature = namedFeatures.get(index);
        assertEquals(name, namedFeature.getName());
        assertEquals(type, namedFeature.getType());
    }
}
