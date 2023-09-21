package eu.gaiax.wizard.util;

import com.smartsensesolutions.java.commons.FilterRequest;
import eu.gaiax.wizard.api.model.request.ParticipantOnboardRequest;
import eu.gaiax.wizard.api.model.request.ParticipantRegisterRequest;
import eu.gaiax.wizard.api.model.service_offer.CreateServiceOfferingRequest;
import eu.gaiax.wizard.util.constant.TestConstant;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelperService {

    public static Map<String, Object> prepareDefaultCredential(String legalName, String headQuarterAddress, String legalAddress) {
        Map<String, Object> legalParticipantCredential = new HashMap<>();
        legalParticipantCredential.put("gx:legalName", legalName);
        legalParticipantCredential.put("gx:headquarterAddress", Map.of("gx:countrySubdivisionCode", headQuarterAddress));
        legalParticipantCredential.put("gx:legalAddress", Map.of("gx:countrySubdivisionCode", legalAddress));

        Map<String, Object> lpCredential = new HashMap<>();
        lpCredential.put("credentialSubject", legalParticipantCredential);

        Map<String, Object> legalRegistrationNumberCredential = new HashMap<>();
        legalRegistrationNumberCredential.put("gx:leiCode", "9695007586XZAKPYJ703");

        Map<String, Object> credential = new HashMap<>();
        credential.put("legalParticipant", lpCredential);
        credential.put("legalRegistrationNumber", legalRegistrationNumberCredential);
        return credential;
    }

    public static ParticipantRegisterRequest registerParticipantRequest(String legalName, String shortName, String entityType,
                                                                        Map<String, Object> credential, boolean ownDid, boolean store, boolean acceptedTnc) {
        ParticipantOnboardRequest onboardRequest = new ParticipantOnboardRequest(legalName, shortName, entityType,
                credential, ownDid, store, acceptedTnc);
        return new ParticipantRegisterRequest(TestConstant.EMAIL, onboardRequest);
    }

    public CreateServiceOfferingRequest addServiceOfferRequest(List<String> labelLevelCriteriaList) {
        Map<String, Object> credentialSubject = new HashMap<>();
        credentialSubject.put("gx:termsAndConditions", Map.of("gx:URL", "https://aws.amazon.com/service-terms/"));
        credentialSubject.put("gx:policy", Map.of("gx:location", Collections.singletonList("BE-BRU")));
        credentialSubject.put("gx:dataProtectionRegime", "GDPR2016");
        credentialSubject.put("type", "gx:ServiceOffering");

        Map<String, Object> dataAccountExport = new HashMap<>();
        dataAccountExport.put("gx:requestType", "API");
        dataAccountExport.put("gx:accessType", "physical");
        dataAccountExport.put("gx:formatType", "pdf");

        credentialSubject.put("gx:dataAccountExport", dataAccountExport);
        credentialSubject.put("gx:criteria", prepareLabelLevelMap(labelLevelCriteriaList));

        CreateServiceOfferingRequest createServiceOfferingRequest = new CreateServiceOfferingRequest();
        createServiceOfferingRequest.setName(TestConstant.SERVICE_OFFER_NAME);
        createServiceOfferingRequest.setCredentialSubject(credentialSubject);

        return createServiceOfferingRequest;
    }

    public static FilterRequest prepareDefaultFilterRequest() {
        return prepareFilterRequest(0, 10);
    }

    public static FilterRequest prepareFilterRequest(int page, int size) {
        FilterRequest request = new FilterRequest();
        request.setPage(page);
        request.setSize(size);
        return request;
    }

    private static Map<String, Object> prepareLabelLevelMap(List<String> labelLevelCriteriaList) {
        Map<String, Object> criteriaMap = new HashMap<>();

        labelLevelCriteriaList.parallelStream().forEach(criterion -> {
            Map<String, Object> criterionMap = Map.of("response", "Confirm");
            criteriaMap.put(criterion, criterionMap);
        });

        return criteriaMap;
    }
}
