package net.ssehub.e_learning.gateway.matching;

import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;

/**
 * A matcher which matches on spring cloud gateway routes. Should be used inside of spring security. It holds the
 * configured permissions from the application properties which which the user should have to access the route.
 *
 * <pre>
 *  <code> 
 *  &#64;Bean
 * public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
 *    return http.authorizeExchange().matchers(permissionRouteMatcher).permitAll().build();
 * }
 * </code>
 * </pre>
 *
 * @author spark
 * @see RouteMatcherFactory
 */
public interface PermissionRouteMatcher extends ServerWebExchangeMatcher {

    /**
     * Indicator if a end user should be authenticated when the request matches (see
     * {@link #matches(org.springframework.web.server.ServerWebExchange)}).
     * 
     * @return true when the user should be authenticated
     */
    boolean needsAuthentication();

    /**
     * The necessary permission of the user for the path. When
     * {@link #matches(org.springframework.web.server.ServerWebExchange)} matches, the user must need the following
     * permission.
     * 
     * <pre>
     * <code>
     * &#64;Bean
     * public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, RouteMatcherFactory factory) {
     *    factory.createMatchers().collectList().block().stream().forEach(permissionRouteMatcher -> 
     *      http.authorizeExchange()
     *          .matchers(permissionRouteMatcher)
     *          .hasAnyAuthority(permissionRouteMatcher.getNeededPermissions());
     *    );
     *    return http.build();
     * }
     * </code>
     * </pre>
     * 
     * @return Authorities as string
     */
    String[] getNeededPermissions();
}
