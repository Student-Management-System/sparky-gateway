package net.ssehub.sparkyservice.api.routing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import net.ssehub.sparkyservice.api.routing.matching.RouteMatcher;
import net.ssehub.sparkyservice.api.routing.matching.RoutePermissionMatcherFactory;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
            RoutePermissionMatcherFactory matcherFactory) {
        var matchers = matcherFactory.createMatchers();
        var authConfig = http.authorizeExchange();
        for (RouteMatcher routeMatcher : matchers) {
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
        authConfig.pathMatchers("/health").permitAll()
                .pathMatchers("/check").authenticated()
                .and().oauth2Login(); // to redirect to oauth2 login page.

        return http.build();
    }

}