# Building and running dmi-plugin locally

## Building Java Archive only

Following command builds Java component to `ncmp-dmi-plugin/target/ncmp-dmi-plugin-x.y.z-SNAPSHOT.jar` JAR file
without generating any docker images:  

```bash
mvn clean install -Pdmi-docker -Djib.skip
```

## Building Java Archive and Docker image

* Following command builds the JAR file and also generates the Docker image for all dmi plugin:

```bash
mvn clean install -Pdmi-plugin -Ddocker.repository.push=
```

* Following command builds the JAR file and generates the Docker image for dmi-plugin:
  (with `<docker-profile>` use `dmi-docker`):

```bash
mvn clean install -P<docker-profile> -Ddocker.repository.push=
```

## Running Docker containers

`docker-compose/docker-compose.yml` file is provided to be run with `docker-compose` tool and images previously built.
It starts ncmp-dmi-plugin service.

Execute following command from `docker-compose` folder:

Use one of the below version type that has been generated in the local system's docker image list after the build.
```bash
VERSION=latest docker-compose up -d
or
VERSION=<version> CPS_USERNAME=<username> CPS_PASSWORD=<password> docker-compose up -d
``` 

## Running or debugging Java built code

dmi-pluginn can be started either using a Java Archive previously built or directly from Intellij IDE.

### Running from Jar Archive

Following command starts the application using JAR file:

```bash
CPS_USERNAME=cpsuser CPS_PASSWORD=cpsr0cks! \
  java -jar ncmp-dmi-plugin/target/ncmp-dmi-plugin-x.y.z-SNAPSHOT.jar
```

### Running from IntelliJ IDE

Here are the steps to run or debug the application from Intellij:

1. Enable the desired maven profile form Maven Tool Window
2. Run a configuration from `Run -> Edit configurations` with following settings:
   * `Environment variables`: `CPS_USERNAME=cpsuser CPS_PASSWORD=cpsr0cks!`

## Accessing services

Swagger UI and Open API specifications are available to discover service endpoints and send requests.

* `http://localhost:<port-number>/swagger-ui/index.html`
* `http://localhost:<port-number>/v3/api-docs?group=cps-docket`

with <port-number> being either `8080` if running the plain Java build or retrieved using following command
if running from `docker-compose`:

```bash
docker inspect \
  --format='{{range $p, $conf := .NetworkSettings.Ports}} {{$p}} -> {{(index $conf 0).HostPort}} {{end}}' \
  <dmi-plugin-docker-container>
```

