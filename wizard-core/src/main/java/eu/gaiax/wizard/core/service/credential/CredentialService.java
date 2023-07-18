/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.core.service.credential;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.gaiax.wizard.api.client.SignerClient;
import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.exception.EntityNotFoundException;
import eu.gaiax.wizard.api.model.CreateVPRequest;
import eu.gaiax.wizard.api.utils.S3Utils;
import eu.gaiax.wizard.api.utils.Validate;
import eu.gaiax.wizard.dao.entity.Enterprise;
import eu.gaiax.wizard.dao.entity.EnterpriseCredential;
import eu.gaiax.wizard.dao.repository.EnterpriseCredentialRepository;
import eu.gaiax.wizard.dao.repository.EnterpriseRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * The type Credential service.
 */
@Service
@RequiredArgsConstructor
public class CredentialService {

    private final EnterpriseRepository enterpriseRepository;

    private final EnterpriseCredentialRepository enterpriseCredentialRepository;

    private final ObjectMapper objectMapper;

    private final SignerClient signerClient;
    private final S3Utils s3Utils;

    /**
     * Create vp map.
     *
     * @param enterpriseId the enterprise id
     * @param name         the name
     * @return the map
     * @throws JsonProcessingException the json processing exception
     */
    public Map<String, Object> createVP(long enterpriseId, String name) throws JsonProcessingException {
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId).orElseThrow(BadDataException::new);
        EnterpriseCredential enterpriseCredential = enterpriseCredentialRepository.getByEnterpriseIdAndLabel(enterpriseId, name);
        Validate.isNull(enterpriseCredential).launch(new EntityNotFoundException("Can not find participant credential for enterprise id->" + enterpriseId));
        JSONObject verifiableCredential = new JSONObject(enterpriseCredential.getCredentials()).getJSONObject("selfDescriptionCredential").getJSONArray("verifiableCredential").getJSONObject(0);

        CreateVPRequest createVPRequest = CreateVPRequest.builder()
                .holderDID("did:web:" + enterprise.getSubDomainName())
                .privateKeyUrl(s3Utils.getPreSignedUrl(enterpriseId + "/pkcs8_" + enterprise.getSubDomainName() + ".key"))
                .claims(List.of(verifiableCredential.toMap()))
                .build();

        ResponseEntity<Map<String, Object>> vp = signerClient.createVP(createVPRequest);

        String serviceOfferingString = objectMapper.writeValueAsString(((Map<String, Object>) vp.getBody().get("data")).get("verifiablePresentation"));
        return new JSONObject(serviceOfferingString).toMap();
    }
}
