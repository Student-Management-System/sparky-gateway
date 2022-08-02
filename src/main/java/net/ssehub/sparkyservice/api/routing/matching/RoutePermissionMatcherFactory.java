package net.ssehub.sparkyservice.api.routing.matching;

import java.util.List;

import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;


@Component
public class RoutePermissionMatcherFactory {

    private final RouteLocator locator;

    private final GatewayConfigUtil utils;

    public RoutePermissionMatcherFactory(RouteLocator locator, GatewayConfigUtil utils) {
        this.locator = locator;
        this.utils = utils;
    }

    public List<RouteMatcher> createMatchers() {
        return locator.getRoutes().map(this::createMatcher).collectList().block();
    }

    private RouteMatcher createMatcher(Route route) {
        return new RouteMatcher() {

            @Override
            public Mono<MatchResult> matches(ServerWebExchange exchange) {
                return checkForMatch(route, exchange).flatMap(RoutePermissionMatcherFactory::getMonoResult);
            }

            @Override
            public String[] getNeededPermissions() {
                return utils.getAllowedList(route).toArray(String[]::new);
            }
            
            @Override
            public boolean needsAuthentication() {
                return utils.findModel(route).map(PartialRoutesConfigModel::authentication).orElse(false);
            }
        };
    }

    private static Mono<Boolean> checkForMatch(Route route, ServerWebExchange exchange) {
        return Mono.from(route.getPredicate().apply(exchange));
    }

    private static Mono<MatchResult> getMonoResult(boolean matches) {
        return matches ? MatchResult.match() : MatchResult.notMatch();
    }
}