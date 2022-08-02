package net.ssehub.sparkyservice.api.routing.matching;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "spring.cloud.gateway")
class GatewayRouteConfig {
    private List<PartialRoutesConfigModel> routes;

    public void setRoutes(List<PartialRoutesConfigModel> routes) {
        this.routes = routes;
    }

    public List<PartialRoutesConfigModel> getRoutes() {
        return routes;
    }
}

@ConstructorBinding
record PartialRoutesConfigModel(String id, String allowed, boolean authentication) {
    List<String> allowedAsList() {
        String[] elements;
        if (allowed != null) {
            elements = allowed.split(",");
        } else {
            elements = new String[0];
        }
        return Stream.of(elements).map(String::strip).toList();            
    }
}

@Component
public class GatewayConfigUtil {
    private final GatewayRouteConfig routeConfig;

    public GatewayConfigUtil(GatewayRouteConfig routeConfig) {
        this.routeConfig = routeConfig;
    }

    public Optional<PartialRoutesConfigModel> findModel(Route route) {
        return routeConfig.getRoutes().stream()
                .filter(r -> r.id().equalsIgnoreCase(route.getId()))
                .findFirst();
    }

    @SuppressWarnings("unchecked")
    public List<String> getAllowedList(Route route) {
        var list = findModel(route).map(PartialRoutesConfigModel::allowedAsList).orElse(Collections.EMPTY_LIST);
        return list;
    }
}