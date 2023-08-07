package eu.gaiax.wizard.core.service.keycloak;

import eu.gaiax.wizard.api.exception.*;
import eu.gaiax.wizard.api.model.*;
import eu.gaiax.wizard.api.model.setting.*;
import eu.gaiax.wizard.api.utils.*;
import jakarta.ws.rs.core.*;
import lombok.*;
import org.keycloak.*;
import org.keycloak.admin.client.*;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.*;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;

import java.util.*;

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

    public void createParticipantUser(String id, String legalName, String email) {
        this.addUser(id, legalName, email, "Participant");
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
        Validate.isNull(userRepresentation).launch(new BadDataException("User not found"));

        UserResource userResource = this.getRealmResource().users().get(userRepresentation.getId());
        userResource.executeActionsEmail(
                this.keycloakSettings.publicClientId(),
                this.keycloakSettings.requiredActionsEmailRedirectionUrl(),
                this.keycloakSettings.actionTokenLifespan(),
                List.of(KeycloakRequiredActionsEnum.WEBAUTHN_REGISTER_PASSWORDLESS.getValue())
        );
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
