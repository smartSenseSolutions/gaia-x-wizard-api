/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.utils;

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
     * The constant ID.
     */
    public static final String ID = "id";
    /**
     * The constant PARTICIPANT_ID.
     */
    public static final String PARTICIPANT_ID = "participantId";
    public static final String FILTER_PARTICIPANT_ID = "participant.id";
    /**
     * The constant JOB_TYPE.
     */
    public static final String JOB_TYPE = "JOB_TYPE";

    /**
     * The constant VERIFIABLE_CREDENTIAL.
     */
    public static final String VERIFIABLE_CREDENTIAL = "VerifiableCredential";


    /**
     * The constant CREDENTIAL_SUBJECT.
     */
    public static final String CREDENTIAL_SUBJECT = "credentialSubject";

    public static final String GX_LEGAL_ADDRESS = "gx:legalAddress";
    public static final String GX_COUNTRY_SUBDIVISION = "gx:countrySubdivisionCode";
    public static final String GX_POLICY = "gx:policy";
    public static final String GX_DATA_PROTECTION_REGIME = "gx:dataProtectionRegime";

    public static final String POLICY_LOCATION_LEFT_OPERAND = "spatial";

    public static final String WEBAUTHN_PASSWORDLESS = "webauthn-passwordless"; //pragma: allowlist secret

    public static final String CONTEXT = "@context";
    public static final String TYPE = "type";
    public static final String TARGET = "target";
    public static final String ASSIGNER = "assigner";
    public static final String ACTION = "action";
    public static final String SUBTYPE = "subtype";
    public static final String JSON_EXTENSION = ".json";
    public static final String AGGREGATION_OF = "gx:aggregationOf";

    public static final String CUSTOM_ATTRIBUTE = "gx:customAttribute";


    private StringPool() {
    }
}
