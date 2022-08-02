package net.ssehub.sparkyservice.api.routing.matching;

import org.springframework.cloud.gateway.route.Route;

@FunctionalInterface
public interface AfterMatchVisitor {
    boolean visit(Route route);
}