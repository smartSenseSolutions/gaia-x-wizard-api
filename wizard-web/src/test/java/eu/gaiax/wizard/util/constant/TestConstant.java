package eu.gaiax.wizard.util.constant;

import java.util.UUID;

public class TestConstant {
    //Configuration Constant
    public static final String VAULT_TOKEN = "vault-token";
    public static final String BEARER = "Bearer ";
    public static final String REAL_FILE_PATH = "wizard-realm.json";
    public static final String KEYCLOAK_ADMIN_USERNAME = "admin";
    public static final String KEYCLOAK_REALM = "wizard-test-realm";
    public static final String KEYCLOAK_PUBLIC_CLIENT = "wizard-test-public-client";
    public static final String KEYCLOAK_PRIVATE_CLIENT = "wizard-test-app-client";
    public static final String KEYCLOAK_PRIVATE_CLIENT_SECRET = "wizard-test-app-client";//pragma: allowlist secret
    public static final String VALID_USER_NAME = "valid-user";
    public static final String INVALID_USER_NAME = "invalid-user";
    public static final String PASSWORD = "password"; //pragma: allowlist secret

    //Other Constant
    public static final String EMAIL = "test@gaia-x.com";
    public static final String LEGAL_NAME = UUID.randomUUID().toString();
    public static final String SHORT_NAME = "shortname";
}
