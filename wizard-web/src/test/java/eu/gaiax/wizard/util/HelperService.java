package eu.gaiax.wizard.util;

import com.smartsensesolutions.java.commons.FilterRequest;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantOnboardRequest;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantRegisterRequest;
import eu.gaiax.wizard.util.constant.TestConstant;

import java.util.HashMap;
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

    public static FilterRequest prepareDefaultFilterRequest() {
        return prepareFilterRequest(0, 10);
    }

    public static FilterRequest prepareFilterRequest(int page, int size) {
        FilterRequest request = new FilterRequest();
        request.setPage(page);
        request.setSize(size);
        return request;
    }
}
