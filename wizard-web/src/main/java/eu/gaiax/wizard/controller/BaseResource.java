package eu.gaiax.wizard.controller;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Objects;

@RestController
public abstract class BaseResource {
    public Object requestForClaim(String claim, Principal principal) {
        if (Objects.isNull(principal)) {
            return "test@email.com";
        }
        return ((JwtAuthenticationToken) principal).getTokenAttributes().get(claim);
    }
}
