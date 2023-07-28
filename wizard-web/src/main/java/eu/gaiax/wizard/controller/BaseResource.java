package eu.gaiax.wizard.controller;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.security.Principal;
import java.util.Objects;

public abstract class BaseResource {

    public Object requestForClaim(String claim, Principal principal) {
        if (Objects.isNull(principal)) {
            return 1L;
        }
        return ((JwtAuthenticationToken) principal).getTokenAttributes().get(claim);
    }
}
