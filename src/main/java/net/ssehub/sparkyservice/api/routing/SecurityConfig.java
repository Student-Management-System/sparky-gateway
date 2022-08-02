package net.ssehub.sparkyservice.api.routing;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Component;

import net.ssehub.sparkyservice.api.routing.matching.AfterMatchVisitor;
import net.ssehub.sparkyservice.api.routing.matching.GatewayRoutePathMatcher;

@Configuration
public class SecurityConfig {

    @Configuration
    @ConfigurationProperties(prefix = "spring.cloud.gateway")
    static class RoutesConfiguration {
        private List<PartialRoutedConfigModel> routes;

        public void setRoutes(List<PartialRoutedConfigModel> routes) {
            this.routes = routes;
        }

        public List<PartialRoutedConfigModel> getRoutes() {
            return routes;
        }
    }

    @ConstructorBinding
    static record PartialRoutedConfigModel(String id, String allowed, boolean authenticated) {
        List<String> allowedAsList() {
            var elements = allowed.split(",");
            return Stream.of(elements).map(String::strip).toList();
        }
    }

    @Component
    static class AuthenticationRouteConfigMatcher implements AfterMatchVisitor {

        private RoutesConfiguration routeConfig;

        public AuthenticationRouteConfigMatcher(RoutesConfiguration routeConfig) {
            this.routeConfig = routeConfig;
        }

        protected Optional<PartialRoutedConfigModel> findModel(Route route) {
            return routeConfig.getRoutes().stream()
                    .filter(r -> r.id().equalsIgnoreCase(route.getId()))
                    .findFirst();
        }

        @Override
        public boolean visit(Route route) {
            return findModel(route).map(model -> model.authenticated()).orElse(false);
        }
        
        @SuppressWarnings("unchecked")
        public List<String> getAllowedList(Route route) {
            return findModel(route).map(PartialRoutedConfigModel::allowedAsList).orElse(Collections.EMPTY_LIST);
        }
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, RoutesConfiguration routeConfig,
            RouteLocator locator) {
        var authenticationMatcher = new GatewayRoutePathMatcher(locator);
        authenticationMatcher.accept(new AuthenticationRouteConfigMatcher(routeConfig));

        http
                .authorizeExchange()
                .pathMatchers("/health").permitAll()
                .matchers(authenticationMatcher).authenticated()
                .and()
                .oauth2Login(); // to redirect to oauth2 login page.

        return http.build();
    }

}