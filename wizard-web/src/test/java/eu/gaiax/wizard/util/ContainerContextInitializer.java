package eu.gaiax.wizard.util;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.keycloak.admin.client.KeycloakBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.vault.VaultContainer;

import java.util.HashMap;
import java.util.Map;

import static eu.gaiax.wizard.util.constant.TestConstant.*;

public class ContainerContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerContextInitializer.class);

    public static final String MAILHOG = "mailhog"; //Do not change
    public static final String S3 = "s3"; //Do not change
    public static final String S3_BUCKET = "s3Bucket"; //Do not change
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

    private final LocalStackContainer localStackContainer = new LocalStackContainer("2.0")
            .withNetwork(this.network)
            .withNetworkAliases(S3) // the last alias is used for HOSTNAME_EXTERNAL
            .withServices(LocalStackContainer.Service.S3, LocalStackContainer.Service.ROUTE53);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        this.postgreSQL.start();
        this.keycloak.start();
        this.vault.start();
        this.mailHog.start();
        this.localStackContainer.start();

        Map<String, String> properties = new HashMap<>();
        properties.put("spring.datasource.url", this.postgreSQL.getJdbcUrl());
        properties.put("spring.datasource.username", this.postgreSQL.getUsername());
        properties.put("spring.datasource.password", this.postgreSQL.getPassword());
        properties.put("wizard.keycloak.authServer", this.keycloak.getAuthServerUrl());
        properties.put("wizard.keycloak.realm", KEYCLOAK_REALM);
        properties.put("wizard.keycloak.clientId", KEYCLOAK_PRIVATE_CLIENT);
        properties.put("wizard.keycloak.clientSecret", KEYCLOAK_PRIVATE_CLIENT_SECRET);
        properties.put("wizard.keycloak.publicClientId", KEYCLOAK_PUBLIC_CLIENT);
        properties.put("wizard.security.enabled", "false");

        properties.put("wizard.aws.access_key", this.localStackContainer.getAccessKey());
        properties.put("wizard.aws.hostedZoneId", this.localStackContainer.getHost());
        properties.put("wizard.aws.secretKey", this.localStackContainer.getSecretKey());
        properties.put("wizard.aws.s3Endpoint", this.localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3).toString());
        properties.put("wizard.aws.region", this.localStackContainer.getRegion());

        properties.put("wizard.vault.host", this.vault.getHttpHostAddress());


        try {
            //Create S3 bucket
//            this.localStackContainer.execInContainer("awslocal", "s3", "mb", "s3://" + S3_BUCKET);
            this.localStackContainer.execInContainer("awslocal", "s3api", "create-bucket", "--bucket", S3_BUCKET);
            LOGGER.info("S3 buckets: {}", this.localStackContainer.execInContainer("awslocal", "s3api", "list-buckets").getStdout());

            properties.put("wizard.aws.bucket", S3_BUCKET);
        } catch (Exception e) {
            LOGGER.error("Error while creating S3 bucket: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }

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
