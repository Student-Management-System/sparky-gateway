package net.ssehub.sparkyservice.api.routing.matching;

import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;

public interface RouteMatcher extends ServerWebExchangeMatcher {
    boolean needsAuthentication();
    String[] getNeededPermissions();
}
