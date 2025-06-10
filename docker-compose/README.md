<!--
  ============LICENSE_START=======================================================
  Copyright (c) 2021-2025 OpenInfra Foundation Europe. All rights reserved.
  ================================================================================
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

  SPDX-License-Identifier: Apache-2.0
  ============LICENSE_END=========================================================
-->
# Building and running dmi-plugin locally

## Building Java Archive only

Following command builds Java component to `ncmp-dmi-plugin/target/ncmp-dmi-plugin-x.y.z-SNAPSHOT.jar` JAR file
without generating any docker images:

```bash
mvn clean install -Djib.skip
```

## Building Java Archive and Docker image

* Following command builds the JAR file and also generates the Docker image for the dmi plugin:

```bash
mvn clean install
```

## Running Docker containers

`docker-compose/docker-compose.yml` file is provided to be run with `docker-compose` tool and images previously built.
It starts ncmp-dmi-plugin service.

Execute following command from `docker-compose` folder:

Use one of the below types that has been generated in the local system's docker image list after the build.
```bash
docker-compose up -d
or
VERSION=<version> docker-compose up -d
or
DOCKER_REPO=<docker.repo> docker-compose up -d
or
VERSION=<version> DOCKER_REPO=<docker_repo> docker-compose up -d
```

## Running or debugging Java built code

dmi-plugin can be started either using a Java Archive previously built or directly from Intellij IDE.

### Running from Jar Archive

Following command starts the application using JAR file:

```bash
  java -jar ncmp-dmi-plugin/target/ncmp-dmi-plugin-x.y.z-SNAPSHOT.jar
```

### Running from IntelliJ IDE

Here are the steps to run or debug the application from Intellij:

1. Enable the desired maven profile from Maven Tool Window
2. Run a configuration from `Run -> Edit configurations`

## Accessing services

Swagger UI and Open API specifications are available to discover service endpoints and send requests.

* `http://localhost:<port-number>/swagger-ui/index.html`
* `http://localhost:<port-number>/v3/api-docs?group=dmi-plugin-docket`

with <port-number> being either `8080` if running the plain Java build or retrieved using following command
if running from `docker-compose`:

```bash
docker inspect \
  --format='{{range $p, $conf := .NetworkSettings.Ports}} {{$p}} -> {{(index $conf 0).HostPort}} {{end}}' \
  <dmi-plugin-docker-container>
```
