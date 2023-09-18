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


    public static final String LEGAL_REGISTRATION_NUMBER = "legalRegistrationNumber";

    public static final String LEGAL_ADDRESS = "gx:legalAddress";
    public static final String LEGAL_PARTICIPANT = "legalParticipant";

    public static final String HEADQUARTER_ADDRESS = "gx:headquarterAddress";

    public static final String SUBDIVISION_CODE = "gx:countrySubdivisionCode";

    public static final String SUB_ORGANIZATION = "gx:subOrganization";

    public static final String PARENT_ORGANIZATION = "gx:parentOrganization";

    public static final String OBSOLETE_TIME = "gx:obsoleteDateTime";

    public static final String EXPIRATION_TIME = "gx:expirationDateTime";

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    public static final String NAME = "gx:name";

    public static final String PHYSICAL = "Physical";

    public static final String MAINTAINED_BY = "gx:maintainedBy";

    public static final String OWNED_BY = "gx:ownedBy";

    public static final String MANUFACTURED_BY = "gx:manufacturedBy";

    public static final String COPYRIGHT_OWNED_BY = "gx:copyrightOwnedBy";

    public static final String PRODUCED_BY = "gx:producedBy";

    public static final String CONTAINS_PII = "gx:containsPII";

    public static final String LEGAL_BASIS = "gx:legalBasis";

    public static final String GX_EMAIL = "gx:email";

    public static final String GX_URL = "gx:url";


    private StringPool() {
    }
}
