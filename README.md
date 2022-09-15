# sparky-gateway
*This project is the successor of sparkyservice*

sparky-gateway is an application gateway which uses a runtime service discovery for finding services. It has a simple permission management build in (via oauth2)

**Features**:

- Dynamic Route Configuration (see Setup TODO)
- Find services via service discovery (see Setup TODO)
- oauth2 Authorization (see Setup TODO)

Pre-build jars are available on our group-jenkins: [here](https://jenkins-2.sse.uni-hildesheim.de/view/Teaching/job/Teaching_infrastructure-gateway-service/)
The project is automatically build as docker image and is released on github: [here](https://github.com/orgs/e-Learning-by-SSE/packages/container/package/infrastructure-gateway)


## Build & Run

### First run
We need an additional maven repository for dependency management. If you want to run the build for the first time, you have to add a github token of your account to access our github maven repository. 
 or use our `mvn-settings.xml` to apply the needed settings. 
In case of regular development, we recommend to apply these settings to the `~/.m2/settings.xml` (see link above).


The `mvn-settings.xml` uses environment variables for github:

```
export GITHUB_USER=<username>
export GITHUB_PASSWORD=<token>
mvn clean -s mvn-settings.xml install
```
This will download the needed parent-pom. You must do this again, if you change the version of the parent project (or if you wan't to publish the project). 

Note: You must provide the settings in each maven command if you don't run the `install` phase.

### Latern runs:

Simply run:

```bash
mvn package
JAR=$(ls target/sparky-gateway*.jar) && java -jar $JAR
```

The first line will read pre-defined environment variables. The second line will run all tests and create a jar inside the target directory. The third command will run it. 
For testing you must install docker. To skip tests run `mvn package -DskipITs` instead.

### Run via Docker
We provide a docker container image to run the application. The project contains a spring docker profile for a convenient configuration. 

The following environment properties are available if you use the `docker` spring profile:
- `GW_OIDC_ISSUER` OIDC Server which is used for authentication
- `GW_OIDC_CLIENTID` Client ID for the OIDC Server for the authorization flow
- `GW_OIDC_SECRET` Secret for the Client ID uses for the authorization flow
- `REG_EUREKA_IP` The IP for the eureka registry which is used for runtime service lookup

To use this you must set `SPRING_PROFILES_ACTIVE=docker` as environment. 
All docker images are built with spring-boot, so you can always provide all spring properties as environment variables. Just write them in screaming snake case:
`spring.profiles.active=docker` --> `SPRING_PROFILES_ACTIVE=docker`

Docker compose sample:
```
version: '3.8'
services:
   gateway:
      image: ghcr.io/e-learning-by-sse/infrastructure-gateway:latest
      environment:
         SPRING_PROFILES_ACTIVE: docker
         GW_OIDC_CLIENTID: sse-client
         GW_OIDC_SECRET: samplesecret
         GW_OIDC_ISSUER: unihi.de
         REG_EUREKA_IP: localhost
```

To provide a application configuration (see Configuration section) mount an application profile into the containers under the /workspace directory.<br/> 
Example for docker-compose: 
```
version: '3.8'
services:
  gateway:
    image: ghcr.io/e-learning-by-sse/infrastructure-gateway:latest
    environment:
      SPRING_PROFILES_ACTIVE: prod
    volumes:
      - ./application-prod.yml:/workspace/application-prod.yml
```

## Development Setup

**Spring Dev Profile:**<br/>
Use the `dev` spring profile to make a simple configuration. To use it pass the following property `-Dspring.profiles.active=dev` (for example in eclipse as run Configuration).

**Service Setup:**<br/>
In order to use the profile, you need a running OIDC Server on localhost:8090 and an eureka registry on localhost:8761 If you don't need modifications on those services, you can use the `compose-dev.yml` with docker compose to provide a simple setup. For this run: 

```
docker compose -f compose-dev.yml up -d # starting
docker compose -f compose-dev.yml down  # stopping
```

**Permanent Maven Settings:**<br/>
For a more convenient maven usage, we recommend to copy the content of the `mvn-settings.xml` to `~/.m2/settings.xml` and change environment variables to your needs. Through this you do not need to 
set the `mvn-settings.xml` and don't need to use environment variables.

### Docker Images
You can use maven to build the project with as a docker image:

```
mvn spring-boot:build-image
```
The build results in a local image named `ghcr.io/e-learning-by-sse/infrastrcuture-gateway:0.0.1` (the tag could be different).

You can publish the docker image through maven as well: 

```
export DOCKER_USER=<user>
export DOCKER_PASSWORD=<password>
export DOCKER_REGISTRY=<registry>
export DOCKER_GROUP=<group>
mvn -s mvn-settings.xml spring-boot:build-image -Dspring-boot.build-image.publish=true
```

Note: The `DOCKER_USER` and `DOCKER_PASSWORD` must always have **any** value even if you don't intend to publish the image to a docker registry. This is due to a configuration limitation of spring boot. If you don't wish to publish the image automatically, the values of those properties can be any arbitrary placeholder. The parent project pom takes care of this, as long as you don't try to override the properties in your local maven settings or via `mvn-settings.xml`. 


If you wish to publish the docker image automatically to our group-registry you must provide the following settings:


## Configuration

Configure the oauth2 connection (replace the < > content):

```
spring:
  security:
    oauth2:
      client:
        provider:
          any-provider-name:
            issuer-uri: <URI>
            registration:
              keycloak-spring-gateway-client:
                provider: any-provider-name
                client-id: <clientID>
                client-secret: <SECRET>
                authorization-grant-type: authorization_code
                redirect-uri: '{baseUrl}/login/oauth2/code/keycloak'
                scope: openid

```


In order to create a new target for routing:

```
spring:
  cloud:
    gateway:
      routes:
        - id: stmgmt # is not visible to any user
          uri: lb://servicename # redirect target - 
          predicates: # list which determines that a request should routed to the target
            - Path=/stmgmt/** # /** for ignoring trailing slash
	       	authentication: true # optional; default false
	       	allowed: ROLE_stmgmt # optional; default empty	
```

In order to use the eureka registry lookup, you simple use `lb://` URI to specify a ressource location. For example, `lb://stmgmt` would search for a service which registered itself with the "stmgmt" name. 

See [the spring security documentation](https://spring.io/projects/spring-security) for security settings related to oauth2/oidc and [spring cloud gateway reference guide](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/) for spring cloud gateway settings.

## Migration from Sparkyservice
Sparkyservice had three main application areas, service registry, application routing with access management for specified routes and user management. Those features were seperated in different projects

- sparky-gateway (This project) as application gateway for routing
- keycloak (keycloak.org/, Test instance: staging.sse.uni-hildesheim.de:8443/) for user management with OIDC/oauth2
- runtime-registry (https://github.com/Student-Management-System/runtime-registry) as service registry with runtime discovery


**For Service Developers:**

You need to register your service at the service registry in order to use the application gateway.
Please read the full infrastructure description found here TODO. There you find information about the new authentication service which replaced the auth functions of sparkyservice.


**For Administrators:**

This section shows the **routing and permission configuration** inside the application.yml. 

Before:
```
zuul.routing.stmgmt.url=http://example.com
zuul.routes.stmgmt.acl = test@MEMORY,test1@LDAP
```

This is not exactly possible anymore. The account support was dropped . Now you need to create a role and assign it
to the users which are allowed to access the resource. 

Now: 
```
spring:
  cloud:
  gateway:
    routes:
      - id: stmgmt
        uri: http://example.com
        predicates:
          - Path=/stmgmt/**
          authentication: true
          allowed: ROLE_stmgmt
```

When `authentication` is set but not `allowed`, then anyone which is authenticated can access the resource. 
	
See [Spring Cloud Configuration](https://cloud.spring.io/spring-cloud-gateway/multi/multi__configuration.html) for more information.
