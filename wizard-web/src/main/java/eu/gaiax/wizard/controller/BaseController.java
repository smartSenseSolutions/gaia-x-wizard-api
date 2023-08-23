package eu.gaiax.wizard.controller;

import eu.gaiax.wizard.api.exception.ForbiddenAccessException;
import eu.gaiax.wizard.api.utils.StringPool;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Objects;

@RestController
public abstract class BaseController {
    public Object requestForClaim(String claim, Principal principal) {
        if (Objects.isNull(principal)) {
            throw new ForbiddenAccessException("access.not.allowed");
        }
        return ((JwtAuthenticationToken) principal).getTokenAttributes().get(claim);
    }

    public void validateParticipantId(String participantId, Principal principal) {
        String participantIdFromPrincipal = (String) this.requestForClaim(StringPool.ID, principal);

        if (!participantId.equalsIgnoreCase(participantIdFromPrincipal)) {
            throw new ForbiddenAccessException("access.not.allowed");
        }
    }
}
