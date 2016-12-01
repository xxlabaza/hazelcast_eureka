/* 
 * Copyright 2016 Artem Labazin <xxlabaza@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xxlabaza.test.hazelcast.eureka.member;

import static java.util.Collections.emptyMap;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.properties.PropertyDefinition;
import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryStrategyFactory;
import java.util.Collection;
import java.util.Map;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Artem Labazin <xxlabaza@gmail.com>
 * @since 01.12.2016
 */
@Configuration
class HazelcastMemberConfiguration {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${app.hazelcast.port}")
    private Integer hazelcastPort;

    @Bean
    public Config hazelcastConfig () {
        val config = new Config();
        config.setProperty("hazelcast.discovery.enabled", "true");

        val networkingConfig = config.getNetworkConfig();
        networkingConfig.setPort(hazelcastPort);
        networkingConfig.setPortAutoIncrement(false);

        val joinConfig = networkingConfig.getJoin();

        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig().setEnabled(false);
        joinConfig.getAwsConfig().setEnabled(false);
        joinConfig.getDiscoveryConfig()
                .addDiscoveryStrategyConfig(new DiscoveryStrategyConfig(discoveryStrategyFactory()));

        return config;
    }

    @Bean
    public DiscoveryStrategyFactory discoveryStrategyFactory () {
        return new DiscoveryStrategyFactory() {

            @Override
            public Class<? extends DiscoveryStrategy> getDiscoveryStrategyType () {
                return EurekaDiscoveryStrategy.class;
            }

            @Override
            public DiscoveryStrategy newDiscoveryStrategy (DiscoveryNode discoveryNode,
                                                           ILogger logger,
                                                           Map<String, Comparable> properties
            ) {
                val eurekaDiscoveryStrategy = new EurekaDiscoveryStrategy(logger, emptyMap());
                eurekaDiscoveryStrategy.setDiscoveryClient(discoveryClient);
                eurekaDiscoveryStrategy.setHaselcastNodeServiceId(applicationName);
                return eurekaDiscoveryStrategy;
            }

            @Override
            public Collection<PropertyDefinition> getConfigurationProperties () {
                return null;
            }
        };
    }
}
