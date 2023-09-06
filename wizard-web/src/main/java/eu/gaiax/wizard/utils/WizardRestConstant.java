package eu.gaiax.wizard.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WizardRestConstant {

    public static final String PARTICIPANT_ROOT = "/participant";

    public static final String REGISTER = "/public/register";

    public static final String SEND_REQUIRED_ACTIONS_EMAIL = "/public/registration/send-email";

    /*public static final String ENTERPRISE = "/enterprise";//ER
    public static final String ENTERPRISE_LIST = ENTERPRISE + "/list";//AR
    public static final String ENTERPRISE_BY_ID = ENTERPRISE + "/{id}";//AR
    public static final String ENTERPRISE_VC = ENTERPRISE + "/vcs";//ER
    public static final String SERVICE_OFFERING = ENTERPRISE + "/service-offers";//ER
    public static final String SERVICE_OFFER_BY_ID = ENTERPRISE + "/service-offers/{id}";//ER
    public static final String CATALOGUE = "/catalogue";//ER
    public static final String CREATE_VP = ENTERPRISE + "/vc/{name}/vp";//ER
    public static final String SERVICE_OFFER_DETAILS = ENTERPRISE + "/service-offers/{offerId}/details";//ER

    public static final String EXPORT_KEYS = ENTERPRISE + "/keys/export"; //ER*/

    public static final String MASTER_DATA_FILTER = "/public/master-data/{dataType}/filter";

    public static final String LABEL_LEVEL_QUESTIONS = "/public/label-level-questions";

    public static final String CHECK_REGISTRATION = "/public/check-registration";

    public static final String POLICY_EVALUATE = "/public/policy/evaluate";

    public static final String POLICY_EVALUATE_V2 = "/public/policy/evaluate/v2";

    public static final String PARTICIPANT_CONFIG = "/participant/config";

    public static final String PARTICIPANT_EXPORT = "/participant/{participantId}/export";

    public static final String SERVICE_OFFER = "/service-offer";
    public static final String LABEL_LEVEL = "/public/label-level";
    public static final String LABEL_LEVEL_FILE_UPLOAD = "/public/label-level/file";
    public static final String LABEL_LEVEL_FILE_DOWNLOAD = "label-level/file/{fileName}/**";

    public static final String PUBLIC_SERVICE_OFFER = "/public/service-offer";

    public static final String PUBLIC_POLICY = "/public/policy";

    public static final String VALIDATE_SERVICE_OFFER = "/public/service-offer/validate";

    public static final String SERVICE_OFFER_LOCATION = "/public/service-offer/location";

    public static final String SERVICE_OFFER_FILTER = "/public/service-offer/filter";

    public static final String PARTICIPANT_SERVICE_OFFER_FILTER = "/participant/{participantId}/service-offer/filter";

    public static final String RESOURCE_FILTER = "/public/resource/filter";

    public static final String PARTICIPANT_RESOURCE_FILTER = "/participant/{participantId}/resource/filter";
    public static final String PARTICIPANT_RESOURCE = "/participant/{participantId}/resource";

    public static final String ONBOARD_PARTICIPANT = "/onboard/participant/{participantId}";

    public static final String VALIDATE_PARTICIPANT = "/public/validate/participant";

    public static final String WELL_KNOWN = "/.well-known/{fileName}";

    public static final String PARTICIPANT_JSON = "/{participantId}/{fileName}";

    public static final String PARTICIPANT_SUBDOMAIN = "/subdomain/{participantId}";

    public static final String PARTICIPANT_CERTIFICATE = "/certificate/{participantId}";

    public static final String PARTICIPANT_INGRESS = "/ingress/{participantId}";

    public static final String PARTICIPANT_DID = "/did/{participantId}";

    public static final String CREATE_PARTICIPANT = "/participant/{participantId}";

}
