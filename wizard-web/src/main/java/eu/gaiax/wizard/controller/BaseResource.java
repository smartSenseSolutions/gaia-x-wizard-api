package eu.gaiax.wizard.controller;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.security.Principal;
import java.util.Objects;

import static eu.gaiax.wizard.api.model.StringPool.ENTERPRISE_ID;

public abstract class BaseResource {

    public Long getEnterpriseId(Principal principal) {
        if (Objects.isNull(principal)) {
            return 1L;
        }
        return (Long) ((JwtAuthenticationToken) principal).getTokenAttributes().get(ENTERPRISE_ID);
    }
}
