package net.ssehub.sparkyservice.api.routing;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Endpoint {

    @GetMapping("/check")
    public String test(Principal principal) {
        return principal.getName();
    }
    
    @GetMapping("/health")
    public String health(Principal principal) {
        return "Ok";
    }
}
