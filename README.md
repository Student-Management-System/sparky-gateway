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
Clone repository and run: 

	mvn package
	JAR=$(ls target/sparky-gateway*.jar) && java -jar $JAR

The first line will run all tests and create a jar inside the target directory. The second command will run it. 
For testing you must install docker. To skip tests run `mvn package -DskipITs` instead.

# Development Setup

Use the `dev` spring profile to make a simple configuration. To use it pass the following property `-Dspring.profiles.active=dev`.
In order to use the profile, you need a running OIDC Server on localhost:8090 and an eureka registry on localhost:8761

If you don't need modifications on those services, you can use the `compose-dev.yml` with docker compose to provide a simple setup. For this run: 

```
docker compose -f compose-dev.yml up -d # starting
docker compose -f compose-dev.yml down  # stopping
```

## Run with docker
You can use maven to build the project with as a docker image:

```
mvn spring-boot:build-image -Ddocker.secret=empty -Ddocker.user=empty
```

Note: The `docker.secret` and `docker.user` must have **any** value. This is due to a configuration limitation of spring boot. If you don't wish to publish the image automatically, the 
values of those properties can be any arbitrary placeholder.

If you wish to publish the docker automatically you must provide the following settings:

```
 mvn clean spring-boot:build-image \
   -Ddocker.registry=https://<REGISTRY> \ 
   -Ddocker.user=<SECRET> \
   -Ddocker.secret=<SECRET> \
   -Dspring-boot.build-image.publish=true'
```


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


**For Administrators:**

This section shows the **routing and permission configuration** inside the application.yml. 

Before:

	zuul.routing.stmgmt.url=http://example.com
	zuul.routes.stmgmt.acl = test@MEMORY,test1@LDAP

This is not exactly possible anymore. The account support was dropped . Now you need to create a role and assign it
to the users which are allowed to access the resource. 

Now: 

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

When `authentication` is set but not `allowed`, then anyone which is authenticated can access the resource. 
	
See [Spring Cloud Configuration](https://cloud.spring.io/spring-cloud-gateway/multi/multi__configuration.html) for more information.

**For Service Developers:**

You need to register your service at the service registry in order to use the application gateway.
Please read the full infrastructure description found here TODO. There you find information about the new authentication service which replaced the auth functions of sparkyservice.
