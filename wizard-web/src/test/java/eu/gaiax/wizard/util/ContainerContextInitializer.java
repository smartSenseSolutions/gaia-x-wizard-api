package eu.gaiax.wizard.util;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.vault.VaultContainer;

import java.util.HashMap;
import java.util.Map;

import static eu.gaiax.wizard.util.constant.TestConstant.*;

public class ContainerContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final String MAILHOG = "mailhog"; //Do not change
    private final Network network = Network.newNetwork();

    private final PostgreSQLContainer postgreSQL = new PostgreSQLContainer("postgres:15.3");
    private final KeycloakContainer keycloak = new KeycloakContainer()
            .withNetwork(this.network)
            .withRealmImportFile(REAL_FILE_PATH)
            .withAdminPassword(KEYCLOAK_ADMIN_USERNAME)
            .withAdminPassword(PASSWORD);

    private final VaultContainer vault = new VaultContainer("hashicorp/vault:1.14.0")
            .withVaultToken(VAULT_TOKEN);

    private final GenericContainer mailHog = new GenericContainer("mailhog/mailhog")
            .withNetwork(this.network)
            .withNetworkAliases(MAILHOG)
            .withExposedPorts(1025, 8025);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        this.postgreSQL.start();
        this.keycloak.start();
        this.vault.start();
        this.mailHog.start();
        Map<String, String> properties = new HashMap<>();
        properties.put("spring.datasource.url", this.postgreSQL.getJdbcUrl());
        properties.put("spring.datasource.username", this.postgreSQL.getUsername());
        properties.put("spring.datasource.password", this.postgreSQL.getPassword());
        properties.put("wizard.keycloak.authServer", this.keycloak.getAuthServerUrl());
        properties.put("wizard.keycloak.realm", KEYCLOAK_REALM);
        properties.put("wizard.keycloak.clientId", KEYCLOAK_PRIVATE_CLIENT);
        properties.put("wizard.keycloak.clientSecret", KEYCLOAK_PRIVATE_CLIENT_SECRET);
        properties.put("wizard.keycloak.publicClientId", KEYCLOAK_PUBLIC_CLIENT);
        properties.put("wizard.keycloak.publicClientId", KEYCLOAK_PUBLIC_CLIENT);
        properties.put("wizard.keycloak.publicClientId", KEYCLOAK_PUBLIC_CLIENT);

        properties.put("wizard.vault.host", this.vault.getHttpHostAddress());
        try {
            //Enable vault approle, create wizard-role and read role-id
            this.vault.execInContainer("vault", "auth", "enable", "approle");
            this.vault.execInContainer("vault", "write", "-f", "auth/approle/role/wizard-role");
            String roleId = this.vault.execInContainer("vault", "read", "auth/approle/role/wizard-role/role-id").getStdout().split("role_id")[1].trim();
            String secretId = this.vault.execInContainer("vault", "write", "-f", "auth/approle/role/wizard-role/secret-id").getStdout().split("secret_id")[1].trim();
            properties.put("wizard.vault.role-id", roleId);
            properties.put("wizard.vault.secret-id", secretId);
            properties.put("wizard.vault.secret-path", "secret");
            properties.put("wizard.vault.authentication", "APPROLE");
            properties.put("wizard.vault.host", this.vault.getHost());
            properties.put("wizard.vault.port", this.vault.getFirstMappedPort().toString());
            properties.put("wizard.vault.scheme", "http");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        TestPropertyValues testProperties = TestPropertyValues.empty();
        testProperties.and(properties).applyTo(applicationContext.getEnvironment());
    }

    public String createToken(boolean validUser) {
        KeycloakBuilder keycloakBuilder = KeycloakBuilder.builder()
                .serverUrl(this.keycloak.getAuthServerUrl())
                .realm(KEYCLOAK_REALM)
                .clientId(KEYCLOAK_PUBLIC_CLIENT)
                .username(INVALID_USER_NAME)
                .password(PASSWORD);
        if (validUser) {
            keycloakBuilder.username(VALID_USER_NAME);
        }
        String access_token = keycloakBuilder.build().tokenManager().getAccessToken().getToken();
        return BEARER + access_token;
    }
}
