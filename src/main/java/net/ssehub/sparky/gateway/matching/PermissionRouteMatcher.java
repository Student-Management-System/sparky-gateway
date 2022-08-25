package net.ssehub.sparky.gateway.matching;

import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;

public interface PermissionRouteMatcher extends ServerWebExchangeMatcher {
    boolean needsAuthentication();

    String[] getNeededPermissions();
}
