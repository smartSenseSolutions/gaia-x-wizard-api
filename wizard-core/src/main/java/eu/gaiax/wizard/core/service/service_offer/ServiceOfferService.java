package eu.gaiax.wizard.core.service.service_offer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.smartsensesolutions.java.commons.FilterRequest;
import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import com.smartsensesolutions.java.commons.base.service.BaseService;
import com.smartsensesolutions.java.commons.filter.FilterCriteria;
import com.smartsensesolutions.java.commons.operator.Operator;
import com.smartsensesolutions.java.commons.specification.SpecificationUtil;
import eu.gaiax.wizard.api.client.MessagingQueueClient;
import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.exception.EntityNotFoundException;
import eu.gaiax.wizard.api.model.*;
import eu.gaiax.wizard.api.model.did.ServiceEndpointConfig;
import eu.gaiax.wizard.api.model.policy.SubdivisionName;
import eu.gaiax.wizard.api.model.service_offer.*;
import eu.gaiax.wizard.api.utils.StringPool;
import eu.gaiax.wizard.api.utils.Validate;
import eu.gaiax.wizard.core.service.credential.CredentialService;
import eu.gaiax.wizard.core.service.data_master.StandardTypeMasterService;
import eu.gaiax.wizard.core.service.data_master.SubdivisionCodeMasterService;
import eu.gaiax.wizard.core.service.hashing.HashingService;
import eu.gaiax.wizard.core.service.participant.InvokeService;
import eu.gaiax.wizard.core.service.participant.ParticipantService;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantValidatorRequest;
import eu.gaiax.wizard.core.service.signer.SignerService;
import eu.gaiax.wizard.core.service.ssl.CertificateService;
import eu.gaiax.wizard.dao.entity.Credential;
import eu.gaiax.wizard.dao.entity.data_master.StandardTypeMaster;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.entity.service_offer.ServiceOffer;
import eu.gaiax.wizard.dao.repository.participant.ParticipantRepository;
import eu.gaiax.wizard.dao.repository.service_offer.ServiceOfferRepository;
import eu.gaiax.wizard.vault.Vault;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceOfferService extends BaseService<ServiceOffer, UUID> {

    private final CredentialService credentialService;
    private final ServiceOfferRepository serviceOfferRepository;
    private final ObjectMapper objectMapper;
    private final ParticipantRepository participantRepository;
    private final ParticipantService participantService;
    private final SignerService signerService;
    private final PolicyService policyService;
    private final SpecificationUtil<ServiceOffer> serviceOfferSpecificationUtil;
    private final ServiceEndpointConfig serviceEndpointConfig;
    private final StandardTypeMasterService standardTypeMasterService;
    private final ServiceLabelLevelService labelLevelService;
    private final Vault vault;
    private final CertificateService certificateService;
    private final MessagingQueueClient messagingQueueClient;
    private final SubdivisionCodeMasterService subdivisionCodeMasterService;

    @Value("${wizard.host.wizard}")
    private String wizardHost;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED, propagation = Propagation.REQUIRED)
    public ServiceOfferResponse createServiceOffering(CreateServiceOfferingRequest request, String id, boolean isOwnDid) throws IOException {
        Map<String, Object> response = new HashMap<>();
        this.validateServiceOfferMainRequest(request);
        Participant participant;
        if (id != null) {
            participant = this.participantRepository.findById(UUID.fromString(id)).orElse(null);
            Validate.isNull(participant).launch(new BadDataException("participant.not.found"));
            Credential participantCred = this.credentialService.getByParticipantWithCredentialType(participant.getId(), CredentialTypeEnum.LEGAL_PARTICIPANT.getCredentialType());
            this.signerService.validateRequestUrl(Collections.singletonList(participantCred.getVcUrl()), "participant.json.not.found", null);
            request.setParticipantJsonUrl(participantCred.getVcUrl());
        } else {
            ParticipantValidatorRequest participantValidatorRequest = new ParticipantValidatorRequest(request.getParticipantJsonUrl(), request.getVerificationMethod(), request.getPrivateKey(), false, isOwnDid);
            participant = this.participantService.validateParticipant(participantValidatorRequest);
            Validate.isNull(participant).launch(new BadDataException("participant.not.found"));
        }

        if (participant.isKeyStored()) {
            if (!this.vault.get(participant.getId().toString()).containsKey("pkcs8.key")) {
                throw new BadDataException("private.key.not.found");
            }
            request.setPrivateKey(this.vault.get(participant.getId().toString()).get("pkcs8.key").toString());
            request.setVerificationMethod(participant.getDid());
        }
        if (request.isStoreVault() && !participant.isKeyStored()) {
            this.certificateService.uploadCertificatesToVault(participant.getId().toString(), null, null, null, request.getPrivateKey());
        }
        String serviceName = "service_" + this.getRandomString();
        String serviceHostUrl = this.wizardHost + participant.getId() + "/" + serviceName + ".json";

        Map<String, Object> credentialSubject = request.getCredentialSubject();
        if (request.getCredentialSubject().containsKey("gx:policy")) {
            String policyId = participant.getId() + "/" + serviceName + "_policy";
            String policyUrl = this.wizardHost + policyId + ".json";
            Map<String, List<String>> policy = this.objectMapper.convertValue(request.getCredentialSubject().get("gx:policy"), Map.class);
            List<String> country = policy.get("gx:location");
            ODRLPolicyRequest odrlPolicyRequest = new ODRLPolicyRequest(country, StringPool.POLICY_LOCATION_LEFT_OPERAND, serviceHostUrl, participant.getDid(), this.wizardHost, serviceName);

            String hostPolicyJson = this.objectMapper.writeValueAsString(this.policyService.createPolicy(odrlPolicyRequest, policyUrl));
            if (!org.apache.commons.lang3.StringUtils.isAllBlank(hostPolicyJson)) {
                this.policyService.hostODRLPolicy(hostPolicyJson, policyId);
                if (credentialSubject.containsKey("gx:policy")) {
                    credentialSubject.put("gx:policy", List.of(policyUrl));
                }
                this.credentialService.createCredential(hostPolicyJson, policyUrl, CredentialTypeEnum.ODRL_POLICY.getCredentialType(), "", participant);
            }
        }

        this.createTermsConditionHash(credentialSubject);

        // todo sign label level vc
        Map<String, String> labelLevelVc = new HashMap<>();

        if (request.getCredentialSubject().containsKey("gx:criteria")) {
            LabelLevelRequest labelLevelRequest = new LabelLevelRequest(this.objectMapper.convertValue(request.getCredentialSubject().get("gx:criteria"), Map.class), request.getPrivateKey(), request.getParticipantJsonUrl(), request.getVerificationMethod(), request.isStoreVault());
            labelLevelVc = this.labelLevelService.createLabelLevelVc(labelLevelRequest, participant, serviceHostUrl);
            request.getCredentialSubject().remove("gx:criteria");
            if (labelLevelVc != null) {
                request.getCredentialSubject().put("gx:labelLevel", labelLevelVc.get("vcUrl"));
            }
        }
        request.setCredentialSubject(credentialSubject);
        Map<String, String> complianceCredential = this.signerService.signService(participant, request, serviceName);
        if (!participant.isOwnDidSolution()) {
            this.signerService.addServiceEndpoint(participant.getId(), serviceHostUrl, this.serviceEndpointConfig.linkDomainType(), serviceHostUrl);
        }
        Credential serviceOffVc = this.credentialService.createCredential(complianceCredential.get("serviceVc"), serviceHostUrl, CredentialTypeEnum.SERVICE_OFFER.getCredentialType(), "", participant);
        List<StandardTypeMaster> supportedStandardList = this.getSupportedStandardList(complianceCredential.get("serviceVc"));

        ServiceOffer serviceOffer = ServiceOffer.builder()
                .name(request.getName())
                .participant(participant)
                .credential(serviceOffVc)
                .serviceOfferStandardType(supportedStandardList)
                .description(request.getDescription() == null ? "" : request.getDescription())
                .build();

        if (complianceCredential.containsKey("trustIndex")) {
            serviceOffer.setVeracityData(complianceCredential.get("trustIndex"));
        }
        if (Objects.requireNonNull(labelLevelVc).containsKey("labelLevelVc")) {
            JsonNode descriptionCredential = this.objectMapper.readTree(labelLevelVc.get("labelLevelVc")).path("credentialSubject");
            if (descriptionCredential != null) {
                serviceOffer.setLabelLevel(descriptionCredential.path("gx:labelLevel").asText());
            }
        }

        try {
            String messageReferenceId = this.publishServiceComplianceToMessagingQueue(complianceCredential.get("serviceVc"));
            log.info("service: {}, messageReferenceId: {}", request.getName(), messageReferenceId);
            serviceOffer.setMessageReferenceId(messageReferenceId);
        } catch (Exception e) {
            log.error("Error encountered while publishing service to message queue", e);
        }

        serviceOffer = this.serviceOfferRepository.save(serviceOffer);

        if (!CollectionUtils.isEmpty(labelLevelVc)) {
            this.labelLevelService.saveServiceLabelLevelLink(labelLevelVc.get("labelLevelVc"), labelLevelVc.get("vcUrl"), participant, serviceOffer);
        }
        TypeReference<List<Map<String, Object>>> typeReference = new TypeReference<>() {
        };
        this.objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        List<Map<String, Object>> vc = this.objectMapper.readValue(serviceOffer.getCredential().getVcJson(), typeReference);
        return ServiceOfferResponse.builder()
                .vcUrl(serviceOffer.getCredential().getVcUrl())
                .name(serviceOffer.getName())
                .veracityData(serviceOffer.getVeracityData())
                .vcJson(vc)
                .description(serviceOffer.getDescription())
                .build();
    }

    private String publishServiceComplianceToMessagingQueue(String complianceCredential) throws JsonProcessingException {
        PublishToQueueRequest publishToQueueRequest = new PublishToQueueRequest();
        publishToQueueRequest.setSource(this.wizardHost);
        publishToQueueRequest.setData((Map<String, Object>) this.objectMapper.readValue(complianceCredential, Map.class).get("complianceCredential"));

        ResponseEntity<Object> publishServiceComplianceResponse = this.messagingQueueClient.publishServiceCompliance(publishToQueueRequest);
        if (publishServiceComplianceResponse.getStatusCode().equals(HttpStatus.CREATED)) {
            log.info("Publish Service Response Headers Set: {}", publishServiceComplianceResponse.getHeaders().keySet());
            String rawMessageId = publishServiceComplianceResponse.getHeaders().get("location").get(0);
            return rawMessageId.substring(rawMessageId.lastIndexOf("/") + 1);
        }
        return null;
    }

    @SneakyThrows
    private List<StandardTypeMaster> getSupportedStandardList(String serviceJsonString) {
        JsonNode serviceOfferingJsonNode = this.getServiceCredentialSubject(serviceJsonString);
        assert serviceOfferingJsonNode != null;

        if (!serviceOfferingJsonNode.has(StringPool.GX_DATA_PROTECTION_REGIME)) {
            return Collections.emptyList();
        }

        if (serviceOfferingJsonNode.get(StringPool.GX_DATA_PROTECTION_REGIME).isValueNode()) {
            String dataProtectionRegime = serviceOfferingJsonNode.get(StringPool.GX_DATA_PROTECTION_REGIME).asText();
            return this.standardTypeMasterService.findAllByTypeIn(List.of(dataProtectionRegime));
        } else {
            ObjectReader reader = this.objectMapper.readerFor(new TypeReference<List<String>>() {
            });

            List<String> standardNameList = reader.readValue(serviceOfferingJsonNode.get(StringPool.GX_DATA_PROTECTION_REGIME));
            return this.standardTypeMasterService.findAllByTypeIn(standardNameList);
        }
    }

    @SneakyThrows
    private JsonNode getServiceCredentialSubject(String serviceJsonString) {
        JsonNode serviceOffer = this.objectMapper.readTree(serviceJsonString);
        JsonNode verifiableCredential = serviceOffer.get("selfDescriptionCredential").get("verifiableCredential");

        for (JsonNode credential : verifiableCredential) {
            if (credential.get("credentialSubject").get("type").asText().equals("gx:ServiceOffering")) {
                return credential.get("credentialSubject");
            }
        }
        return null;
    }

    private String getRandomString() {
        final String possibleCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder randomString = new StringBuilder(5);
        for (int i = 0; i < 4; i++) {
            int randomIndex = random.nextInt(possibleCharacters.length());
            char randomChar = possibleCharacters.charAt(randomIndex);
            randomString.append(randomChar);
        }
        return randomString.toString();
    }

    private void createTermsConditionHash(Map<String, Object> credentialSubject) throws IOException {
        if (credentialSubject.containsKey("gx:termsAndConditions")) {
            Map<String, Object> termsAndConditions = this.objectMapper.convertValue(credentialSubject.get("gx:termsAndConditions"), Map.class);
            if (termsAndConditions.containsKey("gx:URL")) {
                String content = HashingService.fetchJsonContent(termsAndConditions.get("gx:URL").toString());
                termsAndConditions.put("gx:hash", HashingService.generateSha256Hash(content));
                credentialSubject.put("gx:termsAndConditions", termsAndConditions);
            }
        }
    }

    public void validateServiceOfferRequest(CreateServiceOfferingRequest request) {
        Validate.isFalse(StringUtils.hasText(request.getName())).launch("invalid.service.name");
        Validate.isTrue(CollectionUtils.isEmpty(request.getCredentialSubject())).launch("invalid.credential");
    }

    public List<String> getLocationFromService(ServiceIdRequest serviceIdRequest) {
        String[] subdivisionCodeArray = this.policyService.getLocationByServiceOfferingId(serviceIdRequest.id());
        if (subdivisionCodeArray.length > 0) {
            List<SubdivisionName> subdivisionNameList = this.subdivisionCodeMasterService.getNameListBySubdivisionCode(subdivisionCodeArray);
            if (!CollectionUtils.isEmpty(subdivisionNameList)) {
                return subdivisionNameList.stream().map(SubdivisionName::name).toList();
            }
        }

        return Collections.emptyList();
    }

    @Override
    protected BaseRepository<ServiceOffer, UUID> getRepository() {
        return this.serviceOfferRepository;
    }

    @Override
    protected SpecificationUtil<ServiceOffer> getSpecificationUtil() {
        return this.serviceOfferSpecificationUtil;
    }

    public void validateServiceOfferMainRequest(CreateServiceOfferingRequest request) throws JsonProcessingException {
        this.validateCredentialSubject(request);
/*
        this.validateTermsAndConditions(request);
*/
        this.validateAggregationOf(request);
        this.validateDependsOn(request);
        this.validateDataAccountExport(request);
    }

    private void validateCredentialSubject(CreateServiceOfferingRequest request) {
        if (CollectionUtils.isEmpty(request.getCredentialSubject())) {
            throw new BadDataException("invalid.credential");
        }
    }

    private void validateTermsAndConditions(CreateServiceOfferingRequest request) {
        Map<String, Object> credentialSubject = request.getCredentialSubject();
        if (!credentialSubject.containsKey("gx:termsAndConditions")) {
            throw new BadDataException("term.condition.not.found");
        }

        Map termsCondition = this.objectMapper.convertValue(credentialSubject.get("gx:termsAndConditions"), Map.class);

        if (!termsCondition.containsKey("gx:URL")) {
            throw new BadDataException("term.condition.not.found");
        }

        String termsAndConditionsUrl = termsCondition.get("gx:URL").toString();
        this.signerService.validateRequestUrl(Collections.singletonList(termsAndConditionsUrl), "term.condition.not.found ", null);
    }

    private void validateAggregationOf(CreateServiceOfferingRequest request) throws JsonProcessingException {
        if (!request.getCredentialSubject().containsKey("gx:aggregationOf") || !StringUtils.hasText(request.getCredentialSubject().get("gx:aggregationOf").toString())) {
            throw new BadDataException("aggregation.of.not.found");
        }
        JsonNode jsonNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(request.getCredentialSubject()));

        JsonNode aggregationOfArray = jsonNode.at("/gx:aggregationOf");

        List<String> ids = new ArrayList<>();
        aggregationOfArray.forEach(item -> {
            if (item.has("id")) {
                String id = item.get("id").asText();
                ids.add(id);
            }
        });
        this.signerService.validateRequestUrl(ids, "aggregation.of.not.found", Collections.singletonList("holderSignature"));
    }

    private void validateDependsOn(CreateServiceOfferingRequest request) throws JsonProcessingException {
        if (request.getCredentialSubject().get("gx:dependsOn") != null) {
            JsonNode jsonNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(request.getCredentialSubject()));

            JsonNode aggregationOfArray = jsonNode.at("/gx:dependsOn");

            List<String> ids = new ArrayList<>();
            aggregationOfArray.forEach(item -> {
                if (item.has("id")) {
                    String id = item.get("id").asText();
                    ids.add(id);
                }
            });
            this.signerService.validateRequestUrl(ids, "depends.on.not.found", null);
        }

    }

    private void validateDataAccountExport(CreateServiceOfferingRequest request) throws JsonProcessingException {
        Map<String, Object> credentialSubject = request.getCredentialSubject();
        if (!credentialSubject.containsKey("gx:dataAccountExport")) {
            throw new BadDataException("data.account.export.not.found");
        }

        TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
        };
        this.objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        Map<String, Object> export = this.objectMapper.convertValue(credentialSubject.get("gx:dataAccountExport"), typeReference);

        this.validateExportField(export, "gx:requestType", "requestType.of.not.found");
        this.validateExportField(export, "gx:accessType", "accessType.of.not.found");
        this.validateExportField(export, "gx:formatType", "formatType.of.not.found");
    }

    private void validateExportField(Map<String, Object> export, String fieldName, String errorMessage) {
        if (!export.containsKey(fieldName) || !StringUtils.hasText(export.get(fieldName).toString())) {
            throw new BadDataException(errorMessage);
        }
    }

    public PageResponse<ServiceFilterResponse> filterServiceOffering(FilterRequest filterRequest, String participantId) {

        if (StringUtils.hasText(participantId)) {
            FilterCriteria participantCriteria = new FilterCriteria(StringPool.FILTER_PARTICIPANT_ID, Operator.CONTAIN, Collections.singletonList(participantId));
            List<FilterCriteria> filterCriteriaList = filterRequest.getCriteria() != null ? filterRequest.getCriteria() : new ArrayList<>();
            filterCriteriaList.add(participantCriteria);
            filterRequest.setCriteria(filterCriteriaList);
        }

        Page<ServiceOffer> serviceOfferPage = this.filter(filterRequest);
        List<ServiceFilterResponse> resourceList = this.objectMapper.convertValue(serviceOfferPage.getContent(), new TypeReference<>() {
        });

        return PageResponse.of(resourceList, serviceOfferPage, filterRequest.getSort());
    }

    @SneakyThrows
    public ServiceDetailResponse getServiceOfferingById(UUID serviceOfferId) {
        ServiceOffer serviceOffer = this.serviceOfferRepository.findById(serviceOfferId).orElse(null);
        Validate.isNull(serviceOffer).launch(new EntityNotFoundException("service.offer.not.found"));

        ServiceDetailResponse serviceDetailResponse = this.objectMapper.convertValue(serviceOffer, ServiceDetailResponse.class);
        JsonNode veracityData = this.objectMapper.readTree(serviceOffer.getVeracityData());
        serviceDetailResponse.setTrustIndex(veracityData.get("trustIndex").asDouble());

        String serviceOfferJsonString = InvokeService.executeRequest(serviceOffer.getVcUrl(), HttpMethod.GET);
        JsonNode serviceOfferJson = new ObjectMapper().readTree(serviceOfferJsonString);
        ArrayNode verifiableCredentialList = (ArrayNode) serviceOfferJson.get("selfDescriptionCredential").get("verifiableCredential");
        JsonNode serviceOfferCredentialSubject = this.getServiceOfferCredentialSubject(verifiableCredentialList);

        serviceDetailResponse.setDataAccountExport(this.getDataAccountExportDto(serviceOfferCredentialSubject));
        serviceDetailResponse.setTnCUrl(serviceOfferCredentialSubject.get("gx:termsAndConditions").get("gx:URL").asText());
        serviceDetailResponse.setProtectionRegime(this.objectMapper.convertValue(serviceOfferCredentialSubject.get("gx:dataProtectionRegime"), new TypeReference<>() {
        }));

        serviceDetailResponse.setLocations(Set.of(this.policyService.getLocationByServiceOfferingId(serviceDetailResponse.getCredential().getVcUrl())));

        if (serviceOfferCredentialSubject.has("gx:aggregationOf")) {
            serviceDetailResponse.setResources(this.getAggregationOrDependentDtoSet((ArrayNode) serviceOfferCredentialSubject.get("gx:aggregationOf"), false));
        }
        if (serviceOfferCredentialSubject.has("gx:dependsOn")) {
            serviceDetailResponse.setDependedServices(this.getAggregationOrDependentDtoSet((ArrayNode) serviceOfferCredentialSubject.get("gx:dependsOn"), true));
        }

        return serviceDetailResponse;
    }

    private DataAccountExportDto getDataAccountExportDto(JsonNode credentialSubject) {

        return DataAccountExportDto.builder()
                .requestType(credentialSubject.get("gx:dataAccountExport").get("gx:requestType").asText())
                .accessType(credentialSubject.get("gx:dataAccountExport").get("gx:accessType").asText())
                .formatType(this.getFormatSet(credentialSubject.get("gx:dataAccountExport").get("gx:formatType")))
                .build();
    }

    private Set<String> getFormatSet(JsonNode formatTypeNode) {
        return formatTypeNode.isArray() ? this.objectMapper.convertValue(formatTypeNode, new TypeReference<>() {
        }) : Collections.singleton(formatTypeNode.asText());
    }

    private JsonNode getServiceOfferCredentialSubject(JsonNode credentialSubjectList) {
        for (JsonNode credential : credentialSubjectList) {
            if (credential.get(StringPool.CREDENTIAL_SUBJECT).get("type").asText().equals("gx:ServiceOffering")) {
                return credential.get(StringPool.CREDENTIAL_SUBJECT);
            }
        }

        return null;
    }

    private JsonNode getResourceCredentialSubject(JsonNode credentialSubjectList) {
        for (JsonNode credential : credentialSubjectList) {
            if (ResourceType.getValueSet().contains(credential.get(StringPool.CREDENTIAL_SUBJECT).get("type").asText())) {
                return credential.get(StringPool.CREDENTIAL_SUBJECT);
            }
        }

        return null;
    }


    private Set<AggregateAndDependantDto> getAggregationOrDependentDtoSet(ArrayNode aggregationOrDependentArrayNode, boolean isService) {
        Set<AggregateAndDependantDto> aggregateAndDependantDtoSet = new HashSet<>();

        StreamSupport.stream(aggregationOrDependentArrayNode.spliterator(), true).forEach(node -> {
            AggregateAndDependantDto aggregateAndDependantDto = new AggregateAndDependantDto();
            aggregateAndDependantDto.setCredentialSubjectId(node.get("id").asText());

            String serviceOrResourceJsonString = InvokeService.executeRequest(node.get("id").asText(), HttpMethod.GET);
            JsonNode serviceOrResourceJson;
            try {
                serviceOrResourceJson = new ObjectMapper().readTree(serviceOrResourceJsonString);
                ArrayNode verifiableCredentialList = (ArrayNode) serviceOrResourceJson.get("selfDescriptionCredential").get("verifiableCredential");

                JsonNode serviceOfferOrResourceCredentialSubject;
                if (isService) {
                    serviceOfferOrResourceCredentialSubject = this.getServiceOfferCredentialSubject(verifiableCredentialList);
                } else {
                    serviceOfferOrResourceCredentialSubject = this.getResourceCredentialSubject(verifiableCredentialList);
                }
                aggregateAndDependantDto.setName(serviceOfferOrResourceCredentialSubject.get("gx:name").asText());

            } catch (JsonProcessingException e) {
                log.error("Error while parsing JSON. url: " + node.get("id").asText());
            }

            aggregateAndDependantDtoSet.add(aggregateAndDependantDto);

        });

        return aggregateAndDependantDtoSet;
    }
}
