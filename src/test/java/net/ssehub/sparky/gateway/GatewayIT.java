package net.ssehub.sparky.gateway;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.ContainerState;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Basic gateway tests.
 * 
 * @author spark
 *
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class GatewayIT {

    private static final DockerComposeContainer<?> SETUP_CT;
    private static final int OIDC_EXPOSED_PORT = 8090;
    private static final int REGISTRY_EXPOSED_PORT = 8761;
    private static final String OIDC_SERVICE_NAME = "oidc";
    private static final String REGISTRY_SERVICE_NAME = "registry";

    /*
     * Starts registry and fake OIDC docker container specified via docker compose. Must be done in the initializer
     * because the randomized ports are set as system property which spring can use for auto configuration later.
     */
    static {
        SETUP_CT = new DockerComposeContainer<>(new File("src/test/resources/compose-test.yml"))
                .withExposedService(OIDC_SERVICE_NAME, OIDC_EXPOSED_PORT)
                .withExposedService(REGISTRY_SERVICE_NAME, REGISTRY_EXPOSED_PORT);
        SETUP_CT.start();
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
        return SETUP_CT.getContainerByServiceName(serviceName)
                .orElseThrow(() -> new RuntimeException("service with name " + serviceName + " was not found"));
    }

    /**
     * Test if the application context of spring is configurable through spring. For this test the full system is
     * configured which means the whole spring auto-configuration will be applied. Because spring security needs a valid
     * OIDC configuration an OIDC server must be configured.
     */
    @Test
    public void contextLoads() {
        SETUP_CT.close();
    }

}