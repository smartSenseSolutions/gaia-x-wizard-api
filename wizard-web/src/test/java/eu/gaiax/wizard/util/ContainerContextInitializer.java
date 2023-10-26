package eu.gaiax.wizard.util;

import dasniko.testcontainers.keycloak.KeycloakContainer;
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
    public static final String S3_BUCKET = "test-bucket"; //Do not change
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
            this.localStackContainer.execInContainer("awslocal", "s3api", "create-bucket", "--bucket", S3_BUCKET);
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
        
        properties.put("server.port", "8080");
        properties.put("server.compression.enabled", "true");
        properties.put("wizard.application.name", "gaia-x-wizard");
        properties.put("wizard.database.postgres.connection-timeout", "120000");
        properties.put("wizard.database.postgres.pool-size", "10");
        properties.put("wizard.keycloak.webAuthRedirectUrl", "http://localhost:8189/*");
        properties.put("wizard.keycloak.actionTokenLifespan", "300");
        properties.put("wizard.keycloak.requiredActionsEmailRedirectionUrl", "http://localhost:8189");
        properties.put("wizard.security.enabled", "true");
        
        properties.put("wizard.security.corsOrigins", "*");
        properties.put("wizard.signer-policies", "integrityCheck,holderSignature,complianceSignature,complianceCheck");
        properties.put("wizard.host.signer", "http://localhost:8080/");
        properties.put("wizard.host.wizard", "http://localhost:8080/");
        properties.put("wizard.host.messagingQueue", "http://localhost:8080/");
        properties.put("wizard.server.port", "8080");
        properties.put("wizard.quartz.scheduler.instanceName", "smartSense");
        properties.put("wizard.quartz.scheduler.instanceId", "AUTO");
        properties.put("wizard.quartz.scheduler.batchTriggerAcquisitionMaxCount", "10");
        properties.put("wizard.management.port", "8090");
        properties.put("wizard.gaia-x.registryService", "https://registry.gaia-x.eu/v1");
        properties.put("wizard.context.participant", "https://www.w3.org/2018/credentials/v1,https://w3id.org/security/suites/jws-2020/v1,https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#");
        properties.put("wizard.context.registrationNumber", "https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/participant");
        properties.put("wizard.context.tnc", "https://www.w3.org/2018/credentials/v1,https://w3id.org/security/suites/jws-2020/v1,https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#");
        properties.put("wizard.context.serviceOffer", "https://www.w3.org/2018/credentials/v1,https://w3id.org/security/suites/jws-2020/v1");
        properties.put("wizard.context.ODRLPolicy", "http://www.w3.org/ns/odrl.jsonld,https://www.w3.org/ns/odrl/2/ODRL22.json");
        properties.put("wizard.context.labelLevel", "https://www.w3.org/2018/credentials/v1,https://w3id.org/security/suites/jws-2020/v1,https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#");
        properties.put("wizard.context.resource", "https://www.w3.org/2018/credentials/v1,https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#");
        properties.put("wizard.gaiax.tnc", "In publishing and graphic design, Lorem ipsum is a placeholder text commonly used to demonstrate the visual form of a document or a typeface without relying on meaningful content.");
        properties.put("spring.liquibase.change-log", "classpath:/db/changelog/changelog-master.xml");
        properties.put("spring.main.allow-bean-definition-overriding", "true");
        properties.put("spring.application.name", "gaia-x-wizard");
        properties.put("spring.datasource.initialization-mode", "always");
        properties.put("spring.quartz.job-store-type", "jdbc");
        properties.put("spring.quartz.properties.org.quartz.scheduler.instanceName", "smartSense");
        properties.put("spring.quartz.properties.org.quartz.scheduler.instanceId", "AUTO");
        properties.put("spring.quartz.properties.org.quartz.scheduler.batchTriggerAcquisitionMaxCount", "10");
        properties.put("spring.quartz.properties.org.quartz.scheduler.batchTriggerAcquisitionFireAheadTimeWindow", "1000");
        properties.put("spring.quartz.properties.org.quartz.jobStore.isClustered", "true");
        properties.put("spring.quartz.properties.org.quartz.jobStore.clusterCheckinInterval", "10000");
        properties.put("spring.quartz.properties.org.quartz.jobStore.acquireTriggersWithinLock", "true");
        properties.put("spring.quartz.properties.org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
        properties.put("spring.quartz.properties.org.quartz.threadPool.threadCount", "10");
        
        TestPropertyValues testProperties = TestPropertyValues.empty();
        testProperties.and(properties).applyTo(applicationContext.getEnvironment());
    }
}
