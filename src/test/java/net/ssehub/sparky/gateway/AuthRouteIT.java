package net.ssehub.sparky.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Test for spring security configuration with spring cloud gateway routes. This test class must boot the whole
 * application context in order to load <br>
 * 1) spring cloud gateway beans (especially the needed RouteLocator) <br>
 * 2) and spring security beans <br>
 * 
 */
@SpringBootTest(properties = {
    "spring.cloud.gateway.discovery.locator.enabled=true", // enables lb:// lookup

    "spring.cloud.gateway.routes[0].id=free",
    "spring.cloud.gateway.routes[0].uri=lb://registry",
    "spring.cloud.gateway.routes[0].predicates[0]=Path=/free/**",

    "spring.cloud.gateway.routes[1].id=authenticated",
    "spring.cloud.gateway.routes[1].uri=lb://registry",
    "spring.cloud.gateway.routes[1].predicates[0]=Path=/authenticated/**",
    "spring.cloug.gateway.routes[1].authentication=asd",
    "spring.cloud.gateway.routes[1].sahdjkashdjk=test",

    "spring.cloud.gateway.routes[2].id=rolesNeeded",
    "spring.cloud.gateway.routes[2].uri=lb://registry",
    "spring.cloud.gateway.routes[2].predicates[0]=Path=/RolesNeeded/**",
    "spring.cloug.gateway.routes[2].authentication=true",
    "spring.cloug.gateway.routes[2].allowed[0]=ROLE_USER",

    "spring.cloud.gateway.routes[3].id=external",
    "spring.cloud.gateway.routes[3].uri=https://uni-hildesheim.de",
    "spring.cloud.gateway.routes[3].predicates[0]=Path=/external/**",
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ "eureka", "oidc" })
public class AuthRouteIT {

    static {
        TestDockerEnvironment.startFullEnvWithSpringSetup();
    }

    @Autowired
    private ApplicationContext context;

    private WebTestClient client() {
        return WebTestClient.bindToApplicationContext(this.context).build();
    }

    @Test
    @DisplayName("Test if a unprotected route IS accessable via eureka protocol")
    public void freeRouteTest() {
        client().get().uri("/free").exchange().expectStatus().isOk();
    }

    @Test
    @DisplayName("Test if a protected route IS accessable with any authentication")
    public void authenticatioOnlyRouteTest() {
        client().get().uri("/authenticated").exchange().expectStatus().isOk();
    }

    @Test
    @DisplayName("Test if a protected route is NOT accessable without authentication")
    public void authenticationOnlyRouteNegativeTest() {
        client().get().uri("/authenticated").exchange().expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Test if route IS accessable when user has correct permission roles")
    @WithMockUser(roles = { "USER" })
    public void routeSufficantPermissionsTest() {
        client().get().uri("/RolesNeeded").exchange().expectStatus().isOk();
    }

    @Test
    @DisplayName("Test if route is NOT accessable when user is authenticated but has insufficant permissions")
    @WithMockUser(roles = { "ANYTHING" })
    public void routeInsufficatnPermissionTest() {
        client().get().uri("/RolesNeeded").exchange().expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Test if external route is forwarded to external site")
    @WithMockUser
    public void externalRedirect() {
        client().get().uri("/external").exchange().expectStatus().is3xxRedirection();
    }

}
