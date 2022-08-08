package net.ssehub.sparky.gateway;

import java.security.Principal;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Endpoint {

    // TODO remove when done with testing
    @GetMapping("/check")
    public String test(Principal principal, Authentication auth) {
        return principal.getName();
    }
    
    @GetMapping("/health")
    public String health(Principal principal) {
        return "Ok";
    }
}
