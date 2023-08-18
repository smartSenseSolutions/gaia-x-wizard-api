/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.model;

/**
 * The interface String pool.
 */
public class StringPool {
    /**
     * The constant JOB_TYPE_CREATE_SUB_DOMAIN.
     */
    public static final String JOB_TYPE_CREATE_SUB_DOMAIN = "JOB_TYPE_CREATE_SUB_DOMAIN";

    /**
     * The constant JOB_TYPE_CREATE_CERTIFICATE.
     */
    public static final String JOB_TYPE_CREATE_CERTIFICATE = "JOB_TYPE_CREATE_CERTIFICATE";

    /**
     * The constant JOB_TYPE_CREATE_INGRESS.
     */
    public static final String JOB_TYPE_CREATE_INGRESS = "JOB_TYPE_CREATE_INGRESS";

    /**
     * The constant JOB_TYPE_CREATE_DID.
     */
    public static final String JOB_TYPE_CREATE_DID = "JOB_TYPE_CREATE_DID";

    /**
     * The constant JOB_TYPE_CREATE_PARTICIPANT.
     */
    public static final String JOB_TYPE_CREATE_PARTICIPANT = "JOB_TYPE_CREATE_PARTICIPANT";

    /**
     * The constant ENTERPRISE_ID.
     */
    public static final String ENTERPRISE_ID = "enterpriseId";
    /**
     * The constant DID.
     */
    public static final String DID = "did";
    /**
     * The constant ID.
     */
    public static final String ID = "id";
    /**
     * The constant PARTICIPANT_ID.
     */
    public static final String PARTICIPANT_ID = "participantId";
    /**
     * The constant JOB_TYPE.
     */
    public static final String JOB_TYPE = "JOB_TYPE";
    /**
     * The constant ROLE.
     */
    public static final String ROLE = "role";
    /**
     * The constant EMAIL.
     */
    public static final String EMAIL = "email";

    /**
     * The constant INVALID_USERNAME_OR_PASSWORD.
     */
    public static final String INVALID_USERNAME_OR_PASSWORD = "invalid.username.or.password"; //pragma: allowlist secret

    /**
     * The constant TERMS_AND_CONDITIONS_HASH.
     */
    public static final String TERMS_AND_CONDITIONS_HASH = "hash_value"; //pragma: allowlist secret

    /**
     * The constant REQUIRED_ACTIONS_PATH.
     */
    public static final String REQUIRED_ACTIONS_PATH = "/login-actions/action-token?key=";

    /**
     * The constant VERIFIABLE_CREDENTIAL.
     */
    public static final String VERIFIABLE_CREDENTIAL = "VerifiableCredential";


    /**
     * The constant CREDENTIAL_SUBJECT.
     */
    public static final String CREDENTIAL_SUBJECT = "credentialSubject";

    /**
     * The constant USER_REGISTERED.
     */
    public static final String USER_REGISTERED = "userRegistered";

    public static final String GX_LEGAL_ADDRESS = "gx:legalAddress";
    public static final String GX_COUNTRY_SUBDIVISION = "gx:countrySubdivisionCode";
    public static final String GX_POLICY = "gx:policy";
    public static final String POLICY_LOCATION_LEFT_OPERAND = "verifiableCredential.credentialSubject.legalAddress.country";

    private StringPool() {
    }
}
