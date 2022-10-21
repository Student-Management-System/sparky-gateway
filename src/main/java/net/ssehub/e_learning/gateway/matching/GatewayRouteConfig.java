package net.ssehub.e_learning.gateway.matching;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jboss.logging.Logger;
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
    private static final Logger LOGGER = Logger.getLogger(GatewayRouteConfig.class);
    
    /**
     * Search method for finding a specific config model by a spring cloud route.
     * 
     * @param route The route which is associated with the desired config model.
     * @return Partial configuration model which is associated with the given route
     */
    Optional<PartialRoutesConfigModel> findModel(Route route) {
        List<PartialRoutesConfigModel> routes;
        if (this.routes == null) {
            LOGGER.info("No routes configured. Routes through EUREKA discovery can be added but you MUST reload the application when adding authentication configurations");
            LOGGER.warn("Starting application without any gateway route");
            routes = Collections.emptyList();
        } else {
            routes = this.routes;
        }
        return routes.stream()
                .filter(r -> r.id().equalsIgnoreCase(route.getId()))
                .findFirst();
    }
}
