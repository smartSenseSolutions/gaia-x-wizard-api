package eu.gaiax.wizard.util;

import com.smartsensesolutions.java.commons.FilterRequest;
import eu.gaiax.wizard.api.model.FileUploadRequest;
import eu.gaiax.wizard.api.model.request.ParticipantOnboardRequest;
import eu.gaiax.wizard.api.model.request.ParticipantRegisterRequest;
import eu.gaiax.wizard.util.constant.TestConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static eu.gaiax.wizard.api.utils.StringPool.*;

public class HelperService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelperService.class);

    public static Map<String, Object> prepareDefaultCredential(String legalName, String headQuarterAddress, String legalAddress) {
        Map<String, Object> legalParticipantCredential = new HashMap<>();
        legalParticipantCredential.put("gx:legalName", legalName);
        legalParticipantCredential.put("gx:headquarterAddress", Map.of("gx:countrySubdivisionCode", headQuarterAddress));
        legalParticipantCredential.put("gx:legalAddress", Map.of("gx:countrySubdivisionCode", legalAddress));
        legalParticipantCredential.put(PARENT_ORGANIZATION, Collections.singletonList(Map.of(ID, "http://localhost/" + UUID.randomUUID())));
        legalParticipantCredential.put(SUB_ORGANIZATION, Collections.singletonList(Map.of(ID, "http://localhost/" + UUID.randomUUID())));

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

    public static FileUploadRequest getValidUpdateProfileImageRequest() {
        try {
            InputStream stream = new FileInputStream(ResourceUtils.getFile("classpath:update_profile.jpg"));
            return new FileUploadRequest(new MockMultipartFile("image", "update_profile.jpg", MediaType.IMAGE_JPEG_VALUE, stream));
        } catch (Exception e) {
            LOGGER.error("Error while getting upload file mock. ", e);
        }
        return new FileUploadRequest(new MockMultipartFile("image", (byte[]) null));
    }


    public static String generateLegalParticipantMock(String randomUUID) {
        return "{\"selfDescriptionCredential\":{\"verifiableCredential\":[{\"issuer\":\"did:web:" + randomUUID + "\"}]}}";
    }
}
