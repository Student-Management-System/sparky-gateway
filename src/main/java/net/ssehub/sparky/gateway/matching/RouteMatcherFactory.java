package net.ssehub.sparky.gateway.matching;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.security.web.server.MatcherSecurityWebFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * Provides factory method for {@link PermissionRouteMatcher}. 
 * 
 * @author spark
 *
 */
@Component
@ConfigurationPropertiesScan // necessary to scan for the gatewayconfig
public class RouteMatcherFactory {

    /**
     * A matcher which matches on spring cloud gateway routes. Should be used inside of spring security. 
     * It holds the configured permissions from the application properties which 
     * which the user should have to access the route. 
     *
     * <pre> <code> 
     *  @Bean
     *  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
     *    return http.authorizeExchange().matchers(routeMatcher).permitAll().build();
     * }
     * </code></pre>
     *
     * @author spark
     */
    private static class RouteConfigurationMatcher implements PermissionRouteMatcher {
        
        private final Route route;
        
        private final GatewayRouteConfig gatewayConfig;
        
        RouteConfigurationMatcher(Route route, GatewayRouteConfig gatewayConfig) {
            this.route = route;
            this.gatewayConfig = gatewayConfig;
        }
        
        @Override
        public Mono<MatchResult> matches(ServerWebExchange exchange) {
            return checkForMatch(exchange).flatMap(RouteConfigurationMatcher::getMonoResult);
        }
        
        @Override
        public String[] getNeededPermissions() {
            return gatewayConfig.findModel(route).map(PartialRoutesConfigModel::allowed).orElse(new String[0]);
        }
        
        @Override
        public boolean needsAuthentication() {
            return gatewayConfig.findModel(route).map(PartialRoutesConfigModel::authentication).orElse(false);
        }
        
        private Mono<Boolean> checkForMatch(ServerWebExchange exchange) {
            return Mono.from(route.getPredicate().apply(exchange));
        }
        
        private static Mono<MatchResult> getMonoResult(boolean matches) {
            return matches ? MatchResult.match() : MatchResult.notMatch();
        }
    }
    
    private final RouteLocator locator;

    private final GatewayRouteConfig gatewayConfig;

    /**
     * Factory for spring security {@link MatcherSecurityWebFilterChain} which matches on 
     * configured spring cloud gateway routes (which should be configured inside the application properties). 
     * 
     * @param locator
     * @param gatewayConfig
     */
    public RouteMatcherFactory(RouteLocator locator, GatewayRouteConfig gatewayConfig) {
        this.locator = locator;
        this.gatewayConfig = gatewayConfig;
    }

    /**
     * Creates a matcher for all configured gateway routes.
     * 
     * @return List of matchers
     */
    public Flux<PermissionRouteMatcher> createMatchers() {
        return locator.getRoutes().map(route -> new RouteConfigurationMatcher(route, gatewayConfig));
    }

}