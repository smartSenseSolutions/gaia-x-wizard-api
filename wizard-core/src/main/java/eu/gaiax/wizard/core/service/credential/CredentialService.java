/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.core.service.credential;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import com.smartsensesolutions.java.commons.base.service.BaseService;
import com.smartsensesolutions.java.commons.specification.SpecificationUtil;
import eu.gaiax.wizard.api.client.SignerClient;
import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.exception.EntityNotFoundException;
import eu.gaiax.wizard.api.model.CreateVPRequest;
import eu.gaiax.wizard.api.utils.S3Utils;
import eu.gaiax.wizard.api.utils.Validate;
/*import eu.gaiax.wizard.dao.entity.Enterprise;
import eu.gaiax.wizard.dao.entity.EnterpriseCredential;
import eu.gaiax.wizard.dao.repository.EnterpriseCredentialRepository;
import eu.gaiax.wizard.dao.repository.EnterpriseRepository;*/
import eu.gaiax.wizard.dao.entity.Credential;
import eu.gaiax.wizard.dao.repository.CredentialRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The type Credential service.
 */
@Service
@RequiredArgsConstructor
public class CredentialService extends BaseService<Credential,UUID> {

   private final CredentialRepository credentialRepository;

   private final SpecificationUtil<Credential> specificationUtil;

    @Override
    protected BaseRepository<Credential, UUID> getRepository() {
        return credentialRepository;
    }

    @Override
    protected SpecificationUtil<Credential> getSpecificationUtil() {
        return specificationUtil;
    }
    public Credential createCredential(String vcType,String vcJson ,UUID participantId,String vcUrl,String metadata){
            Credential credential= Credential.builder()
                    .type(vcType)
                    .vcJson(vcJson)
                    .participantId(participantId)
                    .vcUrl(vcUrl)
                    .metadata(metadata)
                    .build();
            return credential;
    }
    public Credential getByParticipantId(UUID participantId){
        return credentialRepository.findByParticipantId(participantId);
    }

}
