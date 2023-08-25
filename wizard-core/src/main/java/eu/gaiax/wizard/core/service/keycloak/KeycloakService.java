package eu.gaiax.wizard.core.service.keycloak;


import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.model.KeycloakRequiredActionsEnum;
import eu.gaiax.wizard.api.model.setting.KeycloakSettings;
import eu.gaiax.wizard.api.utils.RoleConstant;
import eu.gaiax.wizard.api.utils.StringPool;
import eu.gaiax.wizard.api.utils.Validate;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakService {

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
        this.addUser(id, legalName, email);
        this.addClientRole(email, RoleConstant.PARTICIPANT_ROLE);
        this.sendRequiredActionsEmail(email);
    }

    public void addUser(String id, String legalName, String email) {
        if (this.getKeycloakUserByEmail(email) != null) {
            return;
        }

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setEmail(email);
        userRepresentation.setFirstName(legalName);

        Map<String, List<String>> userAttributes = new HashMap<>();
        userAttributes.put(StringPool.ID, Collections.singletonList(id));
        userRepresentation.setAttributes(userAttributes);

        RealmResource realmResource = this.getRealmResource();
        UsersResource usersResource = realmResource.users();

        Response response = usersResource.create(userRepresentation);
        log.info("Keycloak User Creation status: {}", response.getStatus());
        if (response.getStatus() != HttpStatus.CREATED.value()) {
            throw new BadDataException("Invalid request for user registration");
        }

        log.info("keycloak user created");
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

        log.info("Required actions email sent to the user");
    }

    public void addClientRole(String email, String role) {
        UserRepresentation userRepresentation = this.getKeycloakUserByEmail(email);
        Validate.isNull(userRepresentation).launch(new BadDataException("User not found"));

        List<ClientRepresentation> clientRepresentationList = this.getRealmResource().clients().findByClientId(this.keycloakSettings.clientId());
        Validate.isNull(clientRepresentationList).launch(new BadDataException("Keycloak client not found"));

        ClientRepresentation keycloakClient = clientRepresentationList.get(0);
        ClientResource clientResource = this.getRealmResource().clients().get(keycloakClient.getId());
        RoleResource participantRole = clientResource.roles().get(role);

        UserResource userResource = this.getRealmResource().users().get(userRepresentation.getId());
        userResource.roles().clientLevel(keycloakClient.getId()).add(Collections.singletonList(participantRole.toRepresentation()));

        log.info("client role added to keycloak user");
    }

    public UserRepresentation getKeycloakUserByEmail(String email) {
        log.debug("getKeycloakUserByEmail: email={}", email);

        RealmResource realmResource = this.getRealmResource();
        UsersResource usersResource = realmResource.users();
        List<UserRepresentation> users = usersResource.search(email);
        if (CollectionUtils.isEmpty(users)) {
            return null;
        } else {
            this.getRealmResource().users().get(users.get(0).getId());
            return users.get(0);
        }
    }

    public Boolean isLoginDeviceConfigured(UserRepresentation userRepresentation) {
        try {
            UserResource userResource = this.getRealmResource().users().get(userRepresentation.getId());
            return userResource.credentials().stream().anyMatch(credentialRepresentation -> credentialRepresentation.getType().equals(StringPool.WEBAUTHN_PASSWORDLESS));
        } catch (Exception e) {
            log.error("Error while fetching user credential list: ", e);
            return false;
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
            log.error("Error while generating action token for user with email: {}", email, e);
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
