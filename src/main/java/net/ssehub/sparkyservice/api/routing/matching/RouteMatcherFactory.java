package net.ssehub.sparkyservice.api.routing.matching;

import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class RouteConfigurationMatcher implements PermissionRouteMatcher {

    private final Route route;

    private final QueryDecoratedGatewayConfig gatewayConfig;

    public RouteConfigurationMatcher(Route route, QueryDecoratedGatewayConfig gatewayConfig) {
        this.route = route;
        this.gatewayConfig = gatewayConfig;
    }

    @Override
    public Mono<MatchResult> matches(ServerWebExchange exchange) {
        return checkForMatch(route, exchange).flatMap(RouteConfigurationMatcher::getMonoResult);
    }

    @Override
    public String[] getNeededPermissions() {
        return gatewayConfig.getAllowedList(route);
    }

    @Override
    public boolean needsAuthentication() {
        return gatewayConfig.findModel(route).map(PartialRoutesConfigModel::authentication).orElse(false);
    }

    private static Mono<Boolean> checkForMatch(Route route, ServerWebExchange exchange) {
        return Mono.from(route.getPredicate().apply(exchange));
    }

    private static Mono<MatchResult> getMonoResult(boolean matches) {
        return matches ? MatchResult.match() : MatchResult.notMatch();
    }
}

@Component
public class RouteMatcherFactory {

    private final RouteLocator locator;

    private final QueryDecoratedGatewayConfig gatewayConfig;

    public RouteMatcherFactory(RouteLocator locator, QueryDecoratedGatewayConfig gatewayConfig) {
        this.locator = locator;
        this.gatewayConfig = gatewayConfig;
    }

    public Flux<PermissionRouteMatcher> createMatchers() {
        return locator.getRoutes().map(route -> new RouteConfigurationMatcher(route, gatewayConfig));
    }

}