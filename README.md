
# Overview

POC of dockerized hazelcast cluster with Eureka discovery.

## Usage

> **IMPORTANT:** You need [Docker](https://docs.docker.com/engine/installation/) and [Docker Compose](https://docs.docker.com/compose/install/) before start.

Build projects docker images:

```bash
$> mvn clean package docker:build -pl configserver,eurekaserver,admin,member
```

After this, you will have local images like this:

```bash
$> docker images

REPOSITORY              TAG                 IMAGE ID            CREATED             SIZE
xxlabaza/member         1.0.0               8ef12727ff41        2 minutes ago       221 MB
xxlabaza/member         latest              8ef12727ff41        2 minutes ago       221 MB
xxlabaza/admin          1.0.0               3091745bf90e        2 minutes ago       214 MB
xxlabaza/admin          latest              3091745bf90e        2 minutes ago       214 MB
xxlabaza/eurekaserver   1.0.0               200d6ba1352c        2 minutes ago       216 MB
xxlabaza/eurekaserver   latest              200d6ba1352c        2 minutes ago       216 MB
xxlabaza/configserver   1.0.0               4ceb89da12fd        3 minutes ago       213 MB
xxlabaza/configserver   latest              4ceb89da12fd        3 minutes ago       213 MB
xxlabaza/server_jre     jce                 2b7bf8cb7902        10 hours ago        167 MB
xxlabaza/server_jre     latest              5920a9f92b61        10 hours ago        167 MB
```

Run docker-compose file:

```bash
$> docker-compose up -d

Creating network "hazelcasteureka_default" with the default driver
Creating configserver
Creating eureka
Creating member1
Creating admin
Creating member2
```

> **IMPORTANT:** Because of different heartbeat settings, updating Eureka and Hazelcast status may be slow.

After few minutes all containers will be up, check it with [Spring Boot Admin service](http://localhost:9002):

![Spring Boot Admin picture 1](https://github.com/xxlabaza/hazelcast_eureka/blob/master/images/1.png?raw=true)

As you can see there are 2 hazelcast members (this info is from Eureka). You can also ask one of hazelcast member about cluster:

```bash
$> http :9011/cluster/members

HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
Date: Thu, 01 Dec 2016 10:50:19 GMT
Transfer-Encoding: chunked
X-Application-Context: hazelcast-member:docker:8080

[
    {
        "address": {
            "factoryId": 0,
            "host": "172.25.0.4",
            "id": 1,
            "inetAddress": "172.25.0.4",
            "inetSocketAddress": "172.25.0.4:5701",
            "ipv4": true,
            "ipv6": false,
            "port": 5701,
            "scopeId": null,
            "scopedHost": "172.25.0.4"
        },
        "attributes": {},
        "factoryId": 0,
        "id": 2,
        "inetAddress": "172.25.0.4",
        "inetSocketAddress": "172.25.0.4:5701",
        "liteMember": false,
        "port": 5701,
        "socketAddress": "172.25.0.4:5701",
        "uuid": "bade5cc5-7836-4595-a272-54a287e75204"
    },
    {
        "address": {
            "factoryId": 0,
            "host": "172.25.0.5",
            "id": 1,
            "inetAddress": "172.25.0.5",
            "inetSocketAddress": "172.25.0.5:5701",
            "ipv4": true,
            "ipv6": false,
            "port": 5701,
            "scopeId": null,
            "scopedHost": "172.25.0.5"
        },
        "attributes": {},
        "factoryId": 0,
        "id": 2,
        "inetAddress": "172.25.0.5",
        "inetSocketAddress": "172.25.0.5:5701",
        "liteMember": false,
        "port": 5701,
        "socketAddress": "172.25.0.5:5701",
        "uuid": "9e9d438d-12d0-4133-a2bc-934d3527a626"
    }
]
```

Now we can down on of the node:

```bash
$> docker-compose stop member2

Stopping member2 ... done
```

After that, check Spring Boot Admin:

![Spring Boot Admin picture 2](https://github.com/xxlabaza/hazelcast_eureka/blob/master/images/2.png?raw=true)

And cluster info:

```bash
$> http :9011/cluster/members

HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
Date: Thu, 01 Dec 2016 10:55:41 GMT
Transfer-Encoding: chunked
X-Application-Context: hazelcast-member:docker:8080

[
    {
        "address": {
            "factoryId": 0,
            "host": "172.25.0.4",
            "id": 1,
            "inetAddress": "172.25.0.4",
            "inetSocketAddress": "172.25.0.4:5701",
            "ipv4": true,
            "ipv6": false,
            "port": 5701,
            "scopeId": null,
            "scopedHost": "172.25.0.4"
        },
        "attributes": {},
        "factoryId": 0,
        "id": 2,
        "inetAddress": "172.25.0.4",
        "inetSocketAddress": "172.25.0.4:5701",
        "liteMember": false,
        "port": 5701,
        "socketAddress": "172.25.0.4:5701",
        "uuid": "bade5cc5-7836-4595-a272-54a287e75204"
    }
]
```

Accordingly, we can start the second node again:

```bash
$> docker-compose start member2

Starting member2 ... done
```

Spring Boot Admin:

![Spring Boot Admin picture 3](https://github.com/xxlabaza/hazelcast_eureka/blob/master/images/3.png?raw=true)

Hazelcast cluster info:

```bash
$> http :9011/cluster/members

HTTP/1.1 200 OK
Content-Type: application/json;charset=UTF-8
Date: Thu, 01 Dec 2016 10:58:40 GMT
Transfer-Encoding: chunked
X-Application-Context: hazelcast-member:docker:8080

[
    {
        "address": {
            "factoryId": 0,
            "host": "172.25.0.4",
            "id": 1,
            "inetAddress": "172.25.0.4",
            "inetSocketAddress": "172.25.0.4:5701",
            "ipv4": true,
            "ipv6": false,
            "port": 5701,
            "scopeId": null,
            "scopedHost": "172.25.0.4"
        },
        "attributes": {},
        "factoryId": 0,
        "id": 2,
        "inetAddress": "172.25.0.4",
        "inetSocketAddress": "172.25.0.4:5701",
        "liteMember": false,
        "port": 5701,
        "socketAddress": "172.25.0.4:5701",
        "uuid": "bade5cc5-7836-4595-a272-54a287e75204"
    },
    {
        "address": {
            "factoryId": 0,
            "host": "172.25.0.5",
            "id": 1,
            "inetAddress": "172.25.0.5",
            "inetSocketAddress": "172.25.0.5:5701",
            "ipv4": true,
            "ipv6": false,
            "port": 5701,
            "scopeId": null,
            "scopedHost": "172.25.0.5"
        },
        "attributes": {},
        "factoryId": 0,
        "id": 2,
        "inetAddress": "172.25.0.5",
        "inetSocketAddress": "172.25.0.5:5701",
        "liteMember": false,
        "port": 5701,
        "socketAddress": "172.25.0.5:5701",
        "uuid": "4ea10bfb-c1b7-4e87-bef2-88d21a55e165"
    }
]
```

That's all folks!