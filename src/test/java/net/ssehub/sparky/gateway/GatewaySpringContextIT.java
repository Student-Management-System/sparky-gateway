package net.ssehub.sparky.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Gateway application test.
 * 
 * @author spark
 *
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles({"eureka", "oidc"})
public class GatewaySpringContextIT {

    static {
        TestDockerEnvironment.startFullEnvWithSpringSetup();
    }

    /**
     * Test if the application context of spring is configurable through spring. For this test the full system is
     * configured which means the whole spring auto-configuration will be applied. Because spring security needs a valid
     * OIDC configuration an OIDC server must be configured.
     */
    @Test
    @DisplayName("Tests if the spring context is correctly configured")
    public void contextLoads() {
        TestDockerEnvironment.DOCKER_COMPOSE_CONTAINER.close();
    }

}