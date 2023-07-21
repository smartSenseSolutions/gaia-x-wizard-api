package eu.gaiax.wizard.api.client;

import eu.gaiax.wizard.api.model.RequiredActionsTokenDto;
import eu.gaiax.wizard.api.model.RequiredActionsTokenRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "KeycloakClient", url = "${wizard.keycloak.authServer}")
public interface KeycloakClient {

    @PostMapping(path = "realms/{realm}/action-token/webauthn-actions-token", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<RequiredActionsTokenDto> generateRequireActionsToken(
      @PathVariable(name = "realm") String realm,
      @RequestBody RequiredActionsTokenRequest requiredActionsTokenRequest,
      @RequestHeader(name = "Authorization") String bearerToken
    );
}
