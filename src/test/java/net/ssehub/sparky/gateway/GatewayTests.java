package net.ssehub.sparky.gateway;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Basic gateway tests.
 * 
 * @author spark
 *
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest 
@Testcontainers
@ActiveProfiles("test")
public class GatewayTests {

    static {
        try (GenericContainer<?> ct = new GenericContainer<>(DockerImageName.parse("sparky-test-setup"))
                .withExposedPorts(8671, 8090)) {
            ct.start();
            final String eurekaUri = "${EUREKA_SERVER:http://%s:%s/eureka}".formatted(
                    ct.getHost(), ct.getMappedPort(8671));
            final String issuerUri = "http://%s:%s/".formatted(ct.getHost(), ct.getMappedPort(8090));
            System.setProperty("eureka.client.serviceUrl.defaultZone", eurekaUri);
            System.setProperty("spring.security.oauth2.client.provider.test.issuer-uri", issuerUri);
        }
    }

    /**
     * Test if the application context of spring is configurable through spring.
     */
    @Test
    public void contextLoads() {
    }

}