package eu.gaiax.wizard.core.service.keycloak;

import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.model.KeycloakRequiredActionsEnum;
import eu.gaiax.wizard.api.model.StringPool;
import eu.gaiax.wizard.api.model.setting.KeycloakSettings;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeycloakService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakService.class);

    private final KeycloakSettings keycloakSettings;

    protected Keycloak getKeycloak() {
        return KeycloakBuilder.builder()
                .clientId(this.keycloakSettings.clientId())
                .clientSecret(this.keycloakSettings.clientSecret())
                .realm(this.keycloakSettings.realm())
                .serverUrl(this.keycloakSettings.authServer())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }

    protected RealmResource getRealmResource() {
        Keycloak keycloak = this.getKeycloak();
        return keycloak.realm(this.keycloakSettings.realm());
    }

    public void addUser(String id, String legalName, String email, String role) {
        if (this.getKeycloakUserByEmail(email) != null) {
            return;
        }

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setEmail(email);
        userRepresentation.setFirstName(legalName);

        Map<String, List<String>> userAttributes = new HashMap<>();
        userAttributes.put(StringPool.ID, Collections.singletonList(id));
        userAttributes.put(StringPool.ROLE, Collections.singletonList(role));
        userRepresentation.setAttributes(userAttributes);

        RealmResource realmResource = this.getRealmResource();
        UsersResource usersResource = realmResource.users();

        Response response = usersResource.create(userRepresentation);
        LOGGER.info("Keycloak User Creation status: {}", response.getStatus());
        if (response.getStatus() != HttpStatus.CREATED.value()) {
            throw new BadDataException("Invalid request for user registration");
        }
    }

    public void sendRequiredActionsEmail(String email) {
        UserRepresentation userRepresentation = this.getKeycloakUserByEmail(email);
        UserResource userResource = this.getRealmResource().users().get(userRepresentation.getId());
        userResource.executeActionsEmail(List.of(KeycloakRequiredActionsEnum.WEBAUTHN_REGISTER_PASSWORDLESS.getValue()), this.keycloakSettings.actionTokenLifespan());
    }

    public UserRepresentation getKeycloakUserByEmail(String email) {
        LOGGER.debug("getKeycloakUserByEmail: email={}", email);

        RealmResource realmResource = this.getRealmResource();
        UsersResource usersResource = realmResource.users();
        List<UserRepresentation> users = usersResource.search(email);
        if (CollectionUtils.isEmpty(users)) {
            return null;
        } else {
            return users.get(0);
        }
    }

    /*public String getRequiredActionsUri(String email) {
        try {
            String requiredActionsToken = getRequiredActionsToken(email).token();
            return keycloakSettings.authServer() +
              "/realms/" +
              keycloakSettings.realm() +
              StringPool.REQUIRED_ACTIONS_PATH +
              requiredActionsToken;
        } catch (Exception e) {
            LOGGER.error("Error while generating action token for user with email: {}", email, e);
            return null;
        }
    }

    private RequiredActionsTokenDto getRequiredActionsToken(String email) {
        UserRepresentation userRepresentation = getKeycloakUserByEmail(email);
        if (userRepresentation == null) {
            throw new BadDataException();
        }
        RequiredActionsTokenRequest requiredActionsTokenRequest = new RequiredActionsTokenRequest(
          userRepresentation.getId(),
          email,
          Collections.singletonList(KeycloakRequiredActionsEnum.WEBAUTHN_REGISTER_PASSWORDLESS.getValue()),
          keycloakSettings.webAuthRedirectUrl(),
          keycloakSettings.actionTokenLifespan()
        );

        return keycloakClient.generateRequireActionsToken(keycloakSettings.realm(), requiredActionsTokenRequest, getAccessToken()).getBody();
    }

    private String getAccessToken() {
        return "Bearer " + getKeycloak().tokenManager().getAccessTokenString();

    }
     */

}
