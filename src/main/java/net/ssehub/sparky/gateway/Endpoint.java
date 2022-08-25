package net.ssehub.sparky.gateway;

import java.security.Principal;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller mappings. 
 * 
 * @author spark
 *
 */
@RestController
public class Endpoint {

    /**
     * TODO remove when done with testing.
     * @param principal
     * @param auth
     * @return Current authenticated user name
     */
    @GetMapping("/check")
    public String test(Principal principal, Authentication auth) {
        return principal.getName();
    }
    
}
