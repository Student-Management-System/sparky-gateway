package net.ssehub.sparky.gateway.matching;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.cloud.gateway")
record GatewayRouteConfig(List<PartialRoutesConfigModel> routes) {
}

record PartialRoutesConfigModel(String id, String[] allowed, boolean authentication) {
}

@Component
@ConfigurationPropertiesScan 
public class QueryDecoratedGatewayConfig {

    private final GatewayRouteConfig routeConfig;

    public QueryDecoratedGatewayConfig(GatewayRouteConfig routeConfig) {
        this.routeConfig = routeConfig;
    }

    public Optional<PartialRoutesConfigModel> findModel(Route route) {
        return routeConfig.routes().stream()
                .filter(r -> r.id().equalsIgnoreCase(route.getId()))
                .findFirst();
    }

    public String[] getAllowedList(Route route) {
        return findModel(route).map(PartialRoutesConfigModel::allowed).orElse(new String[0]);
    }
}