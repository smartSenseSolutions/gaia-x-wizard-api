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
import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.exception.EntityNotFoundException;
import eu.gaiax.wizard.api.model.CredentialTypeEnum;
import eu.gaiax.wizard.api.model.PageResponse;
import eu.gaiax.wizard.api.model.ResourceType;
import eu.gaiax.wizard.api.model.ServiceFilterResponse;
import eu.gaiax.wizard.api.model.did.ServiceEndpointConfig;
import eu.gaiax.wizard.api.model.policy.ServiceOfferPolicyDto;
import eu.gaiax.wizard.api.model.policy.SubdivisionName;
import eu.gaiax.wizard.api.model.request.ParticipantValidatorRequest;
import eu.gaiax.wizard.api.model.service_offer.*;
import eu.gaiax.wizard.api.utils.StringPool;
import eu.gaiax.wizard.api.utils.Validate;
import eu.gaiax.wizard.core.service.InvokeService;
import eu.gaiax.wizard.core.service.credential.CredentialService;
import eu.gaiax.wizard.core.service.data_master.StandardTypeMasterService;
import eu.gaiax.wizard.core.service.data_master.SubdivisionCodeMasterService;
import eu.gaiax.wizard.core.service.hashing.HashingService;
import eu.gaiax.wizard.core.service.participant.ParticipantService;
import eu.gaiax.wizard.core.service.participant.VaultService;
import eu.gaiax.wizard.core.service.signer.SignerService;
import eu.gaiax.wizard.dao.entity.Credential;
import eu.gaiax.wizard.dao.entity.data_master.StandardTypeMaster;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import eu.gaiax.wizard.dao.entity.service_offer.ServiceOffer;
import eu.gaiax.wizard.dao.repository.service_offer.ServiceOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.StreamSupport;

import static eu.gaiax.wizard.api.utils.StringPool.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceOfferService extends BaseService<ServiceOffer, UUID> {

    private final CredentialService credentialService;
    private final ServiceOfferRepository serviceOfferRepository;
    private final ObjectMapper objectMapper;
    private final ParticipantService participantService;
    private final SignerService signerService;
    private final PolicyService policyService;
    private final SpecificationUtil<ServiceOffer> serviceOfferSpecificationUtil;
    private final ServiceEndpointConfig serviceEndpointConfig;
    private final StandardTypeMasterService standardTypeMasterService;
    private final ServiceLabelLevelService labelLevelService;
    private final VaultService vaultService;
    private final PublishService publishService;
    private final SubdivisionCodeMasterService subdivisionCodeMasterService;
    private final SecureRandom random = new SecureRandom();

    @Value("${wizard.host.wizard}")
    private String wizardHost;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED, propagation = Propagation.REQUIRED)
    public ServiceOfferResponse createServiceOffering(CreateServiceOfferingRequest request, String id, boolean isOwnDid) throws IOException {
        this.validateServiceOfferMainRequest(request);

        Participant participant;
        if (id != null) {
            participant = this.participantService.findParticipantById(UUID.fromString(id));

            Credential participantCred = this.credentialService.getByParticipantWithCredentialType(participant.getId(), CredentialTypeEnum.LEGAL_PARTICIPANT.getCredentialType());
            this.signerService.validateRequestUrl(Collections.singletonList(participantCred.getVcUrl()), List.of(GX_LEGAL_PARTICIPANT), null, "participant.url.not.found", null);
            request.setParticipantJsonUrl(participantCred.getVcUrl());
        } else {
            ParticipantValidatorRequest participantValidatorRequest = new ParticipantValidatorRequest(request.getParticipantJsonUrl(), request.getVerificationMethod(), request.getPrivateKey(), false, isOwnDid);
            participant = this.participantService.validateParticipant(participantValidatorRequest);
            Validate.isNull(participant).launch(new BadDataException("participant.not.found"));
        }

        if (participant.isKeyStored()) {
            this.addParticipantPrivateKey(participant.getId().toString(), participant.getDid(), request);
        }

        if (request.isStoreVault() && !participant.isKeyStored()) {
            this.vaultService.uploadCertificatesToVault(participant.getId().toString(), null, null, null, request.getPrivateKey());
            participant.setKeyStored(true);
            this.participantService.save(participant);
        }

        String serviceName = "service_" + this.getRandomString();
        String serviceHostUrl = this.wizardHost + participant.getId() + "/" + serviceName + ".json";

        Map<String, String> labelLevelVc = this.createServiceOfferLabelLevel(participant, request, serviceHostUrl);
        Map<String, Object> credentialSubject = request.getCredentialSubject();

        if (credentialSubject.containsKey(GX_POLICY)) {
            this.generateServiceOfferPolicy(participant, serviceName, serviceHostUrl, credentialSubject);
        }
        this.createTermsConditionHash(credentialSubject);
        request.setCredentialSubject(credentialSubject);

        Map<String, String> complianceCredential = this.signerService.signService(participant, request, serviceName);
								String vcUrl=StringUtils.hasText(request.getCredentialSubject().get("id").toString())?request.getCredentialSubject().get("id").toString():serviceHostUrl;

								Credential serviceOffVc = this.credentialService.createCredential(complianceCredential.get(SERVICE_VC), vcUrl, CredentialTypeEnum.SERVICE_OFFER.getCredentialType(), "", participant);
        List<StandardTypeMaster> supportedStandardList = this.getSupportedStandardList(complianceCredential.get(SERVICE_VC));

        ServiceOffer serviceOffer = ServiceOffer.builder()
                .name(request.getName())
                .participant(participant)
                .credential(serviceOffVc)
                .serviceOfferStandardType(supportedStandardList)
                .description(request.getDescription() == null ? "" : request.getDescription())
                .veracityData(complianceCredential.getOrDefault(TRUST_INDEX, null))
                .build();

        this.addLabelLevelToServiceOffer(participant, serviceOffer, labelLevelVc);
        serviceOffer = this.serviceOfferRepository.save(serviceOffer);

        if (!participant.isOwnDidSolution()) {
            this.signerService.addServiceEndpoint(participant.getId(), vcUrl, this.serviceEndpointConfig.linkDomainType(), vcUrl);
        }

        this.publishService.publishServiceComplianceToMessagingQueue(serviceOffer.getId(), complianceCredential.get(SERVICE_VC));

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

    private Map<String, String> createServiceOfferLabelLevel(Participant participant, CreateServiceOfferingRequest request, String serviceHostUrl) {
        // todo sign label level vc
        Map<String, String> labelLevelVc = new HashMap<>();

        if (request.getCredentialSubject().containsKey(GX_CRITERIA)) {
            LabelLevelRequest labelLevelRequest = new LabelLevelRequest(this.objectMapper.convertValue(request.getCredentialSubject().get(GX_CRITERIA), Map.class), request.getPrivateKey(), request.getParticipantJsonUrl(), request.getVerificationMethod(), request.isStoreVault());
            labelLevelVc = this.labelLevelService.createLabelLevelVc(labelLevelRequest, participant, serviceHostUrl);
            request.getCredentialSubject().remove(GX_CRITERIA);
            if (labelLevelVc != null) {
                request.getCredentialSubject().put(GX_LABEL_LEVEL, labelLevelVc.get("vcUrl"));
            }
        }

        return labelLevelVc;
    }

    private void addLabelLevelToServiceOffer(Participant participant, ServiceOffer serviceOffer, Map<String, String> labelLevelVc) throws JsonProcessingException {
        if (Objects.requireNonNull(labelLevelVc).containsKey(LABEL_LEVEL_VC)) {
            JsonNode descriptionCredential = this.objectMapper.readTree(labelLevelVc.get(LABEL_LEVEL_VC)).path(CREDENTIAL_SUBJECT);
            if (descriptionCredential != null) {
                serviceOffer.setLabelLevel(descriptionCredential.path(GX_LABEL_LEVEL).asText());
            }
        }

        if (!CollectionUtils.isEmpty(labelLevelVc)) {
            this.labelLevelService.saveServiceLabelLevelLink(labelLevelVc.get(LABEL_LEVEL_VC), labelLevelVc.get("vcUrl"), participant, serviceOffer);
        }
    }

    private void addParticipantPrivateKey(String participantId, String did, CreateServiceOfferingRequest request) {
        String privateKeySecret = this.vaultService.getParticipantPrivateKeySecret(participantId);
        if (!StringUtils.hasText(privateKeySecret)) {
            throw new BadDataException("private.key.not.found");
        }

        request.setPrivateKey(privateKeySecret);
        request.setVerificationMethod(did);
    }

    private void generateServiceOfferPolicy(Participant participant, String serviceName, String serviceHostUrl, Map<String, Object> credentialSubject) throws JsonProcessingException {
        String policyId = participant.getId() + "/" + serviceName + "_policy" + JSON_EXTENSION;
        String policyUrl = this.wizardHost + policyId;
        ServiceOfferPolicyDto policy = this.objectMapper.convertValue(credentialSubject.get(GX_POLICY), ServiceOfferPolicyDto.class);
        ODRLPolicyRequest odrlPolicyRequest = new ODRLPolicyRequest(policy.location(), SPATIAL, serviceHostUrl, participant.getDid(), this.wizardHost, serviceName);

        String hostPolicyJson = this.objectMapper.writeValueAsString(this.policyService.createServiceOfferPolicy(odrlPolicyRequest, policyUrl));
        if (StringUtils.hasText(hostPolicyJson)) {
            this.policyService.hostPolicy(hostPolicyJson, policyId);
            if (StringUtils.hasText(policy.customAttribute())) {
                credentialSubject.put(GX_POLICY, List.of(policyUrl, policy.customAttribute()));
            } else {
                credentialSubject.put(GX_POLICY, List.of(policyUrl));
            }
            this.credentialService.createCredential(hostPolicyJson, policyUrl, CredentialTypeEnum.ODRL_POLICY.getCredentialType(), "", participant);
        }
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
        JsonNode verifiableCredential = serviceOffer.get(SELF_DESCRIPTION_CREDENTIAL).get(VERIFIABLE_CREDENTIAL_CAMEL_CASE);

        for (JsonNode credential : verifiableCredential) {
            if (credential.get(CREDENTIAL_SUBJECT).get(TYPE).asText().equals(GX_SERVICE_OFFERING)) {
                return credential.get(CREDENTIAL_SUBJECT);
            }
        }
        return null;
    }

    private String getRandomString() {
        final String possibleCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        StringBuilder randomString = new StringBuilder(5);
        for (int i = 0; i < 4; i++) {
            int randomIndex = this.random.nextInt(possibleCharacters.length());
            char randomChar = possibleCharacters.charAt(randomIndex);
            randomString.append(randomChar);
        }
        return randomString.toString();
    }

    private void createTermsConditionHash(Map<String, Object> credentialSubject) throws IOException {
        if (credentialSubject.containsKey(GX_TERMS_AND_CONDITIONS)) {
            Map<String, Object> termsAndConditions = this.objectMapper.convertValue(credentialSubject.get(GX_TERMS_AND_CONDITIONS), Map.class);
            if (termsAndConditions.containsKey(GX_URL_CAPS)) {
                String content = HashingService.fetchJsonContent(termsAndConditions.get(GX_URL_CAPS).toString());
                termsAndConditions.put(GX_HASH, HashingService.generateSha256Hash(content));
                credentialSubject.put(GX_TERMS_AND_CONDITIONS, termsAndConditions);
            }
        }
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
        this.validateAggregationOf(request);
        this.validateDependsOn(request);
        this.validateDataAccountExport(request);
        this.validateTermsAndConditions(request);
        if (!request.getCredentialSubject().containsKey(GX_POLICY)) {
            throw new BadDataException("invalid.policy");
        }
    }

    private void validateTermsAndConditions(CreateServiceOfferingRequest request) {
        Map<String, Object> credentialSubject = request.getCredentialSubject();
        if (!credentialSubject.containsKey(GX_TERMS_AND_CONDITIONS)) {
            throw new BadDataException("term.condition.not.found");
        }

        Map termsCondition = this.objectMapper.convertValue(credentialSubject.get(GX_TERMS_AND_CONDITIONS), Map.class);

        if (!termsCondition.containsKey("gx:URL")) {
            throw new BadDataException("term.condition.not.found");
        }

        try {
            HashingService.fetchJsonContent(termsCondition.get("gx:URL").toString());
        } catch (Exception e) {
            throw new BadDataException("invalid.tnc.url");
        }
    }

    private void validateCredentialSubject(CreateServiceOfferingRequest request) {
        if (CollectionUtils.isEmpty(request.getCredentialSubject())) {
            throw new BadDataException("invalid.credential");
        }
    }

    private void validateAggregationOf(CreateServiceOfferingRequest request) throws JsonProcessingException {
        if (!request.getCredentialSubject().containsKey(AGGREGATION_OF) || !StringUtils.hasText(request.getCredentialSubject().get(AGGREGATION_OF).toString())) {
            throw new BadDataException("aggregation.of.not.found");
        }
        JsonNode jsonNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(request.getCredentialSubject()));

        JsonNode aggregationOfArray = jsonNode.at("/gx:aggregationOfExists");

        List<String> ids = new ArrayList<>();
        aggregationOfArray.forEach(item -> {
            if (item.has(ID)) {
                String id = item.get(ID).asText();
                ids.add(id);
            }
        });
        this.signerService.validateRequestUrl(ids, new ArrayList<>(ResourceType.getValueSet()), LABEL_AGGREGATION_OF, "aggregation.of.not.found", Collections.singletonList("holderSignature"));
    }

    private void validateDependsOn(CreateServiceOfferingRequest request) throws JsonProcessingException {
        if (request.getCredentialSubject().get(DEPENDS_ON) != null) {
            JsonNode jsonNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(request.getCredentialSubject()));

            JsonNode aggregationOfArray = jsonNode.at("/gx:dependsOn");

            List<String> ids = new ArrayList<>();
            aggregationOfArray.forEach(item -> {
                if (item.has(ID)) {
                    String id = item.get(ID).asText();
                    ids.add(id);
                }
            });
            this.signerService.validateRequestUrl(ids, List.of(GX_SERVICE_OFFERING), LABEL_DEPENDS_ON, "depends.on.not.found", null);
        }

    }

    private void validateDataAccountExport(CreateServiceOfferingRequest request) {
        Map<String, Object> credentialSubject = request.getCredentialSubject();
        if (!credentialSubject.containsKey(GX_DATA_ACCOUNT_EXPORT)) {
            throw new BadDataException("data.account.export.not.found");
        }

        TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
        };
        this.objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        Map<String, Object> export = this.objectMapper.convertValue(credentialSubject.get(GX_DATA_ACCOUNT_EXPORT), typeReference);

        this.validateExportField(export, GX_REQUEST_TYPE, "requestType.of.not.found");
        this.validateExportField(export, GX_ACCESS_TYPE, "accessType.of.not.found");
        this.validateExportField(export, GX_FORMAT_TYPE, "formatType.of.not.found");
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
        List<ServiceFilterResponse> serviceOfferList = this.objectMapper.convertValue(serviceOfferPage.getContent(), new TypeReference<>() {
        });

        return PageResponse.of(serviceOfferList, serviceOfferPage, filterRequest.getSort());
    }

    @SneakyThrows
    public ServiceDetailResponse getServiceOfferingById(UUID serviceOfferId) {
        ServiceOffer serviceOffer = this.serviceOfferRepository.findById(serviceOfferId).orElseThrow(() -> new EntityNotFoundException("service.offer.not.found"));

        ServiceDetailResponse serviceDetailResponse = this.objectMapper.convertValue(serviceOffer, ServiceDetailResponse.class);
        JsonNode veracityData = this.objectMapper.readTree(serviceOffer.getVeracityData());
        serviceDetailResponse.setTrustIndex(veracityData.get(TRUST_INDEX).asDouble());

        String serviceOfferJsonString = InvokeService.executeRequest(serviceOffer.getVcUrl(), HttpMethod.GET);
        JsonNode serviceOfferJson = new ObjectMapper().readTree(serviceOfferJsonString);
        ArrayNode verifiableCredentialList = (ArrayNode) serviceOfferJson.get(SELF_DESCRIPTION_CREDENTIAL).get(VERIFIABLE_CREDENTIAL_CAMEL_CASE);
        JsonNode serviceOfferCredentialSubject = this.getServiceOfferCredentialSubject(verifiableCredentialList);

        if (serviceOfferCredentialSubject != null) {
            serviceDetailResponse.setDataAccountExport(this.getDataAccountExportDto(serviceOfferCredentialSubject));
            serviceDetailResponse.setTnCUrl(serviceOfferCredentialSubject.get(GX_TERMS_AND_CONDITIONS).get(GX_URL_CAPS).asText());

            serviceDetailResponse.setProtectionRegime(this.objectMapper.convertValue(serviceOfferCredentialSubject.get(GX_DATA_PROTECTION_REGIME), new TypeReference<>() {
            }));

            serviceDetailResponse.setLocations(Set.of(this.policyService.getLocationByServiceOfferingId(serviceDetailResponse.getCredential().getVcUrl())));

            if (serviceOfferCredentialSubject.has(AGGREGATION_OF)) {
                serviceDetailResponse.setResources(this.getAggregationOrDependentDtoSet((ArrayNode) serviceOfferCredentialSubject.get(AGGREGATION_OF), false));
            }

            if (serviceOfferCredentialSubject.has(DEPENDS_ON)) {
                serviceDetailResponse.setDependedServices(this.getAggregationOrDependentDtoSet((ArrayNode) serviceOfferCredentialSubject.get(DEPENDS_ON), true));
            }
        }

        return serviceDetailResponse;
    }

    private DataAccountExportDto getDataAccountExportDto(JsonNode credentialSubject) {

        return DataAccountExportDto.builder()
                .requestType(credentialSubject.get(GX_DATA_ACCOUNT_EXPORT).get(GX_REQUEST_TYPE).asText())
                .accessType(credentialSubject.get(GX_DATA_ACCOUNT_EXPORT).get(GX_ACCESS_TYPE).asText())
                .formatType(this.getFormatSet(credentialSubject.get(GX_DATA_ACCOUNT_EXPORT).get(GX_FORMAT_TYPE)))
                .build();
    }

    private Set<String> getFormatSet(JsonNode formatTypeNode) {
        return formatTypeNode.isArray() ? this.objectMapper.convertValue(formatTypeNode, new TypeReference<>() {
        }) : Collections.singleton(formatTypeNode.asText());
    }

    private JsonNode getServiceOfferCredentialSubject(JsonNode credentialSubjectList) {
        for (JsonNode credential : credentialSubjectList) {
            if (credential.get(StringPool.CREDENTIAL_SUBJECT).get(TYPE).asText().equals(GX_SERVICE_OFFERING)) {
                return credential.get(StringPool.CREDENTIAL_SUBJECT);
            }
        }

        return null;
    }

    private JsonNode getResourceCredentialSubject(JsonNode credentialSubjectList) {
        for (JsonNode credential : credentialSubjectList) {
            if (ResourceType.getValueSet().contains(credential.get(StringPool.CREDENTIAL_SUBJECT).get(TYPE).asText())) {
                return credential.get(StringPool.CREDENTIAL_SUBJECT);
            }
        }

        return null;
    }


    private Set<AggregateAndDependantDto> getAggregationOrDependentDtoSet(ArrayNode aggregationOrDependentArrayNode, boolean isService) {
        Set<AggregateAndDependantDto> aggregateAndDependantDtoSet = new HashSet<>();

        StreamSupport.stream(aggregationOrDependentArrayNode.spliterator(), true).forEach(node -> {
            AggregateAndDependantDto aggregateAndDependantDto = new AggregateAndDependantDto();
            aggregateAndDependantDto.setCredentialSubjectId(node.get(ID).asText());

            String serviceOrResourceJsonString = InvokeService.executeRequest(node.get(ID).asText(), HttpMethod.GET);
            JsonNode serviceOrResourceJson;
            try {
                serviceOrResourceJson = new ObjectMapper().readTree(serviceOrResourceJsonString);
                ArrayNode verifiableCredentialList = (ArrayNode) serviceOrResourceJson.get(SELF_DESCRIPTION_CREDENTIAL).get(VERIFIABLE_CREDENTIAL_CAMEL_CASE);

                JsonNode serviceOfferOrResourceCredentialSubject;
                if (isService) {
                    serviceOfferOrResourceCredentialSubject = this.getServiceOfferCredentialSubject(verifiableCredentialList);
                } else {
                    serviceOfferOrResourceCredentialSubject = this.getResourceCredentialSubject(verifiableCredentialList);
                }
                aggregateAndDependantDto.setName(serviceOfferOrResourceCredentialSubject.get(NAME).asText());

            } catch (JsonProcessingException e) {
                log.error("Error while parsing JSON. url: " + node.get(ID).asText());
            }

            aggregateAndDependantDtoSet.add(aggregateAndDependantDto);

        });

        return aggregateAndDependantDtoSet;
    }
}
