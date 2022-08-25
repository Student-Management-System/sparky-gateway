package net.ssehub.sparky.gateway.matching;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.route.Route;

/**
 * Configuration model for automatic property matching through spring. Contains a single entry from the configuration
 * properties with the additional config parameters.
 *
 * @author spark
 */
record PartialRoutesConfigModel(String id, String[] allowed, boolean authentication) {
}

/**
 * Configuration model for automatic property matching through spring. Contains all configured spring cloud gateway
 * routes models.
 *
 * @author spark
 */
@ConfigurationProperties(prefix = "spring.cloud.gateway")
public record GatewayRouteConfig(List<PartialRoutesConfigModel> routes) {
    
    /**
     * Search method for finding a specific config model by a spring cloud route.
     * 
     * @param route The route which is associated with the desired config model.
     * @return Partial configuration model which is associated with the given route
     */
    Optional<PartialRoutesConfigModel> findModel(Route route) {
        return routes.stream()
                .filter(r -> r.id().equalsIgnoreCase(route.getId()))
                .findFirst();
    }
}
