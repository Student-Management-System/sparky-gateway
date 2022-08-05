package net.ssehub.sparkyservice.api.routing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import net.ssehub.sparkyservice.api.routing.matching.PermissionRouteMatcher;
import net.ssehub.sparkyservice.api.routing.matching.RouteMatcherFactory;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Autowired
    private ServerHttpSecurity http;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, RouteMatcherFactory factory) {
        factory.createMatchers().collectList().block().stream().forEach(this::gatewayRoutePermConfig);
        defaultEndpointConfig(http);
        return http.build();
    }

    private void gatewayRoutePermConfig(PermissionRouteMatcher routeMatcher) {
        var authConfig = http.authorizeExchange();
        if (routeMatcher.needsAuthentication()) {
            if (routeMatcher.getNeededPermissions().length > 0) {
                authConfig.matchers(routeMatcher).hasAnyAuthority(routeMatcher.getNeededPermissions());
            } else {
                authConfig.matchers(routeMatcher).authenticated();
            }
        } else {
            authConfig.matchers(routeMatcher).permitAll();
        }
    }

    private void defaultEndpointConfig(ServerHttpSecurity http) {
        http.authorizeExchange()
            .pathMatchers("/health").permitAll()
            .pathMatchers("/check").authenticated()
            .and().oauth2Login(); // to redirect to oauth2 login page.
    }

}