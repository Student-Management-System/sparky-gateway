package net.ssehub.sparkyservice.api.routing.matching;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

public class GatewayRoutePathMatcher implements ServerWebExchangeMatcher {
    
    private record RouteMatchResult(Route route, boolean isMatch) {}
 
    private RouteLocator locator;
    
    private final List<AfterMatchVisitor> visitors;
    
    public GatewayRoutePathMatcher(RouteLocator locator) {
        super();
        this.locator = locator;
        this.visitors = new ArrayList<>();
    }

    public void accept(AfterMatchVisitor visitor) {
        visitors.add(visitor);
    }
    
    private boolean runVisistorsSequentially(RouteMatchResult result) {
        boolean matches = result.isMatch();
        if (matches) {
            int i = 0;
            while (matches && i < visitors.size()) {
                matches = visitors.get(i).visit(result.route());
                i++;
            }
        }
        return matches;
    }

    @Override
    public Mono<MatchResult> matches(ServerWebExchange exchange) {
        return locator.getRoutes()
                .flatMap(route -> checkForMatch(route, exchange))
                .map(result -> runVisistorsSequentially(result))
                .reduce((a, b) -> a || b)
                .flatMap(GatewayRoutePathMatcher::getMonoResult);
    }

    private static Mono<RouteMatchResult> checkForMatch(Route route, ServerWebExchange exchange) {
        return Mono.from(route.getPredicate().apply(exchange))
                .map(matches -> new RouteMatchResult(route, matches));
    }
    

    private static Mono<MatchResult> getMonoResult(boolean matches) {
        return matches ? MatchResult.match() : MatchResult.notMatch();
    }

}