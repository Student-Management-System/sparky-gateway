package net.ssehub.e_learning.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import net.ssehub.e_learning.gateway.matching.PermissionRouteMatcher;
import net.ssehub.e_learning.gateway.matching.RouteMatcherFactory;

/**
 * Enabled spring security which works on top of springs cloud gateway. 
 * 
 * @author spark
 *
 */
@EnableWebFluxSecurity
public class HttpSecurityConfig {
    
    @Autowired
    private ServerHttpSecurity http;

    /**
     * Configures the security chain. It takes account of the additional security settings for each configured
     * route. 
     * 
     * @param factory
     * @return The finished security chain
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(RouteMatcherFactory factory) {
        factory.createMatchers().collectList().block().stream().forEach(this::configureSecurityWhenNeeded);
        defaultEndpointConfig();
        return http.csrf().disable().build();
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
                .pathMatchers("/check").authenticated()
                .anyExchange().permitAll()
                .and()
                .cors(Customizer.withDefaults())
                .oauth2Login(Customizer.withDefaults()); // to redirect to oauth2 login page.
    }

}