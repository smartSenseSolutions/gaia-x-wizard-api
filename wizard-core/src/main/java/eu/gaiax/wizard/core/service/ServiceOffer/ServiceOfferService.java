package eu.gaiax.wizard.core.service.ServiceOffer;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.gaiax.wizard.api.VerifiableCredential;
import eu.gaiax.wizard.api.client.SignerClient;
import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.exception.FeignClientException;
import eu.gaiax.wizard.api.model.CredentialTypeEnum;
import eu.gaiax.wizard.api.model.ServiceOffer.CreateServiceOfferingRequest;
import eu.gaiax.wizard.api.model.ServiceOffer.SignerServiceRequest;
import eu.gaiax.wizard.api.model.StringPool;
import eu.gaiax.wizard.api.model.setting.ContextConfig;
import eu.gaiax.wizard.api.utils.CommonUtils;
import eu.gaiax.wizard.core.service.CommonService;
import eu.gaiax.wizard.core.service.credential.CredentialService;
import eu.gaiax.wizard.dao.entity.Credential;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.entity.serviceoffer.ServiceOffer;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import eu.gaiax.wizard.dao.repository.serviceoffer.ServiceOfferRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ServiceOfferService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceOfferService.class);

    private final SignerClient signerClient;
    private final CredentialService credentialService;
    private final ServiceOfferRepository serviceOfferRepository;
    private final ObjectMapper objectMapper;
    private final ParticipantRepository participantRepository;
    private final ContextConfig contextConfig;
    private final CommonService commonService;

    public ServiceOffer createServiceOffering(CreateServiceOfferingRequest request,String email) {
        Map<String,Object> response=new HashMap<>();
        Participant participant=participantRepository.getByEmail(email);
        if(participant==null){
            throw new BadDataException("No data found");
        }
        Credential participantCred=credentialService.getByParticipantId(participant.getId());
        VerifiableCredential verifiableCredential= VerifiableCredential.builder()
                .context(contextConfig.getServiceOffer())
                .type(StringPool.VERIFIABLE_CREDENTIAL)
                .id(participant.getDid())
                .issuer(participant.getDid())
                .credentialSubject(request.getCredentialSubject())
                .issuanceDate(commonService.getCurrentFormattedDate()).build();
        List<VerifiableCredential> verifiableCredentialList=new ArrayList<>();
        verifiableCredentialList.add(verifiableCredential);


        SignerServiceRequest signerServiceRequest= SignerServiceRequest.builder().verifiableCredential(verifiableCredentialList)
                .legalParticipate(participantCred.getVcUrl())
                .privateKeyID(request.getPrivateKey())
                .templateId("ServiceOffering")
                .verifiableCredential(verifiableCredentialList)
                .build();
        try {
            ResponseEntity<Map<String,Object>> signerResponse=  signerClient.createServiceOfferVc(signerServiceRequest);
            String serviceOfferingString = objectMapper.writeValueAsString(((Map<String, Object>) signerResponse.getBody().get("data")).get("verifiablePresentation"));
            response = new JSONObject(serviceOfferingString).toMap();
            LOGGER.debug("Send request to signer for service create vc");
        }catch (Exception e){
            LOGGER.debug("Service vc not created",e.getMessage());
        }
        try {
            Credential serviceOffVc=credentialService.createCredential(response.toString(),null,CredentialTypeEnum.SERVICE_OFFER.getCredentialType(), "",participant);
            ServiceOffer serviceOffer= ServiceOffer.builder()
                    .name(request.getName())
                    .credentialId(serviceOffVc.getId())
                    .description(request.getDescription())
                    .veracityData(response.get("veracityData").toString())
                    .build();
            serviceOfferRepository.save(serviceOffer);

        }catch (Exception e){

        }
        return null;
    }

}
