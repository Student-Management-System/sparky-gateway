# sparky-gateway

It is an application gateway which uses a runtime service discovery for finding services. It has a simple permission management build in (via oauth2)

**Features**:

- Dynamic Route Configuration (see Setup TODO)
- Find services via service discovery (see Setup TODO)
- oauth2 Authorization (see Setup TODO)

*This project is the successor of sparkyservice*

## Build & Run
Clone repository and run: 

	mvn package
	JAR=$(ls target/sparky-gateway*.jar) && java -jar $JAR
	

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

This is not exactly possible anymore. The subject ACL support was dropped. Now you need to create a role and assign it
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
		       	authenticated: true
		       	allowed: ROLE_stmgmt

When `authenticated` is set but not `allowed`, then anyone which is authenticated can access the resource. 
	
See [Spring Cloud Configuration](https://cloud.spring.io/spring-cloud-gateway/multi/multi__configuration.html) for more information.

**For Service Developers:**

You need to register your service at the service registry in order to use the application gateway.
Please read the full infrastructure description found here TODO. There you find information about the new authentication service which replaced the auth functions of sparkyservice.
