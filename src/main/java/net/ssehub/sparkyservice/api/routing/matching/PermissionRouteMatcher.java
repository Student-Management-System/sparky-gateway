package net.ssehub.sparkyservice.api.routing.matching;

import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;

public interface PermissionRouteMatcher extends ServerWebExchangeMatcher {
    boolean needsAuthentication();
    String[] getNeededPermissions();
}
