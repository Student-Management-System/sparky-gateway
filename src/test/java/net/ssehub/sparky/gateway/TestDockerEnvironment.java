package net.ssehub.sparky.gateway;

import java.io.File;

import org.testcontainers.containers.ContainerState;
import org.testcontainers.containers.DockerComposeContainer;

/**
 * Provides test environments via docker.
 * @author spark
 */
public class TestDockerEnvironment {

    private static final int OIDC_EXPOSED_PORT = 8090;
    private static final int REGISTRY_EXPOSED_PORT = 8761;
    private static final String OIDC_SERVICE_NAME = "oidc";
    private static final String REGISTRY_SERVICE_NAME = "registry";

    public static final DockerComposeContainer<?> DOCKER_COMPOSE_CONTAINER = new DockerComposeContainer<>(
            new File("src/test/resources/compose-test.yml"))
                    .withExposedService(OIDC_SERVICE_NAME, OIDC_EXPOSED_PORT)
                    .withExposedService(REGISTRY_SERVICE_NAME, REGISTRY_EXPOSED_PORT);

    /**
     * Starts registry and fake OIDC docker container specified via docker compose. Must be done before spring starts
     * the auto-configuration because System properties needs to be set beforehand.
     */
    public static void startFullEnvWithSpringSetup() {
        DOCKER_COMPOSE_CONTAINER.start();
        System.setProperty("eureka.client.serviceUrl.defaultZone", eurekaUri());
        System.setProperty("spring.security.oauth2.client.provider.my-keycloak-provider.issuer-uri", issuerUri());
    }

    private static String issuerUri() {
        final ContainerState oidcCt = getCt(OIDC_SERVICE_NAME + "_1");
        final String issuerUri = "http://%s:%s/".formatted(oidcCt.getHost(), oidcCt.getFirstMappedPort());
        return issuerUri;
    }

    private static String eurekaUri() {
        final ContainerState registryCt = getCt(REGISTRY_SERVICE_NAME + "_1");
        final String eurekaUri = "${EUREKA_SERVER:http://%s:%s/eureka}".formatted(registryCt.getHost(),
                registryCt.getFirstMappedPort());
        return eurekaUri;
    }

    private static ContainerState getCt(String serviceName) {
        return DOCKER_COMPOSE_CONTAINER.getContainerByServiceName(serviceName)
                .orElseThrow(() -> new RuntimeException("service with name " + serviceName + " was not found"));
    }

}
