package net.ssehub.sparky.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import net.ssehub.sparky.gateway.matching.PermissionRouteMatcher;
import net.ssehub.sparky.gateway.matching.RouteMatcherFactory;

//@Configuration
@EnableWebFluxSecurity
public class HttpSecurityConfig {
    
    @Autowired
    private ServerHttpSecurity http;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(RouteMatcherFactory factory) {
        factory.createMatchers().collectList().block().stream().forEach(this::configureSecurityWhenNeeded);
        defaultEndpointConfig();
        return http.build();
    }
    
    
    private void configureSecurityWhenNeeded(PermissionRouteMatcher routeMatcher) {
        if (routeMatcher.needsAuthentication()) {
            configureRoutePermissions(routeMatcher);
        } else {
            http.authorizeExchange().matchers(routeMatcher).permitAll();
        }
    }

    private void configureRoutePermissions(PermissionRouteMatcher routeMatcher) {
        var matcherConfig = http.authorizeExchange().matchers(routeMatcher);
        if (routeMatcher.getNeededPermissions().length > 0) {
            matcherConfig.hasAnyAuthority(routeMatcher.getNeededPermissions());
        } else {
            matcherConfig.authenticated();
        }
    }

    private void defaultEndpointConfig() {
        http.authorizeExchange()
                .pathMatchers("/health").permitAll()
                .pathMatchers("/check").authenticated()
                .and().oauth2Login(); // to redirect to oauth2 login page.
    }

}