package eu.gaiax.wizard.controller;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.security.Principal;
import java.util.Objects;

public abstract class BaseResource {

    public Long getEnterpriseId(Principal principal) {
        if (Objects.isNull(principal)) {
            return 1L;
        }
        JwtAuthenticationToken jwt = (JwtAuthenticationToken) principal;
        Object enterpriseId = jwt.getTokenAttributes().get("enterpriseId");
        return (Long) enterpriseId;
    }
}
