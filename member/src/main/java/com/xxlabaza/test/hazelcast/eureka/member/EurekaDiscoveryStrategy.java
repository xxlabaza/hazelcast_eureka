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

import static com.netflix.appinfo.InstanceInfo.InstanceStatus.UP;
import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.hazelcast.logging.ILogger;
import com.hazelcast.nio.Address;
import com.hazelcast.spi.discovery.AbstractDiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;
import com.netflix.appinfo.InstanceInfo;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;

/**
 * @author Artem Labazin <xxlabaza@gmail.com>
 * @since 01.12.2016
 */
@Setter
public class EurekaDiscoveryStrategy extends AbstractDiscoveryStrategy {

    private DiscoveryClient discoveryClient;

    private String haselcastNodeServiceId;

    public EurekaDiscoveryStrategy (ILogger logger, Map<String, Comparable> properties) {
        super(logger, properties);
    }

    @Override
    public void start () {
        do {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException almostIgnore) {
                Thread.currentThread().interrupt();
            }
        } while (discoveryClient.getServices() == null);
    }

    @Override
    public Iterable<DiscoveryNode> discoverNodes () {
        return tryToFindInstances()
                .map(instances -> instances.stream()
                        .map(it -> (EurekaDiscoveryClient.EurekaServiceInstance) it)
                        .map(it -> it.getInstanceInfo())
                        .filter(it -> it.getStatus() == UP)
                        .map(this::createDiscoveryNode)
                        .collect(toList())
                )
                .orElse(emptyList());
    }

    private Optional<List<ServiceInstance>> tryToFindInstances () {
        for (int i = 0; i < 5; i++) {
            List<ServiceInstance> instances = discoveryClient.getInstances(haselcastNodeServiceId);
            if (instances != null && !instances.isEmpty()) {
                return Optional.of(instances);
            }

            // retry
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException almostIgnore) {
                Thread.currentThread().interrupt();
            }
        }
        return Optional.empty();
    }

    @SneakyThrows
    private DiscoveryNode createDiscoveryNode (InstanceInfo instanceInfo) {
        Map<String, Object> metadata = (Map) instanceInfo.getMetadata();
        String port = metadata.getOrDefault("hazelcast-port", "5701").toString();
        Address address = new Address(InetAddress.getByName(instanceInfo.getIPAddr()), parseInt(port));
        return new SimpleDiscoveryNode(address, metadata);
    }
}
