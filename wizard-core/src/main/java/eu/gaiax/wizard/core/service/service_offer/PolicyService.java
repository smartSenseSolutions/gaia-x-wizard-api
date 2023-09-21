package eu.gaiax.wizard.core.service.service_offer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.exception.EntityNotFoundException;
import eu.gaiax.wizard.api.model.policy.Constraint;
import eu.gaiax.wizard.api.model.policy.Policy;
import eu.gaiax.wizard.api.model.policy.Rule;
import eu.gaiax.wizard.api.model.service_offer.ODRLPolicyRequest;
import eu.gaiax.wizard.api.model.service_offer.PolicyEvaluationRequest;
import eu.gaiax.wizard.api.model.setting.ContextConfig;
import eu.gaiax.wizard.api.utils.CommonUtils;
import eu.gaiax.wizard.api.utils.S3Utils;
import eu.gaiax.wizard.api.utils.StringPool;
import eu.gaiax.wizard.api.utils.Validate;
import eu.gaiax.wizard.core.service.participant.InvokeService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static eu.gaiax.wizard.api.utils.StringPool.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyService {

    private final ObjectMapper objectMapper;
    private final S3Utils s3Utils;
    private final ContextConfig contextConfig;

    public Map<String, Object> createServiceOfferPolicy(ODRLPolicyRequest odrlPolicyRequest, String hostUrl) {
        Map<String, Object> policyMap = new HashMap<>();
        policyMap.put("@context", this.contextConfig.ODRLPolicy());
        policyMap.put("type", "policy");
        if (hostUrl == null) {
            hostUrl = odrlPolicyRequest.domain() + odrlPolicyRequest.target() + "/" + odrlPolicyRequest.serviceName() + "_policy.json";
        }
        policyMap.put("id", hostUrl);
        List<Map<String, Object>> permission = getServiceOfferPermissionList(odrlPolicyRequest.rightOperand(), odrlPolicyRequest.target(), odrlPolicyRequest.assigner(), odrlPolicyRequest.leftOperand());
        policyMap.put("permission", permission);
        return policyMap;
    }

    @NotNull
    private static List<Map<String, Object>> getServiceOfferPermissionList(List<String> rightOperand, String target, String assigner, String leftOperand) {
        List<Map<String, Object>> permissionList = new ArrayList<>();
        Map<String, Object> permissionMap = new HashMap<>();
        permissionMap.put(TARGET, target);
        permissionMap.put(ASSIGNER, assigner);
        permissionMap.put(ACTION, "use");
        List<Map<String, Object>> constraint = new ArrayList<>();
        Map<String, Object> constraintMap = new HashMap<>();
        constraintMap.put("name", leftOperand);
        constraintMap.put("operator", "isAnyOf");
        constraintMap.put("rightOperand", rightOperand);
        constraint.add(constraintMap);

        permissionMap.put("constraint", constraint);
        permissionList.add(permissionMap);

        return permissionList;
    }

    public void hostPolicy(String hostPolicyJson, String hostedPath) {
        File file = new File(TEMP_FOLDER + hostedPath + JSON_EXTENSION);
        try {
            FileUtils.writeStringToFile(file, hostPolicyJson, Charset.defaultCharset());
            this.s3Utils.uploadFile(hostedPath + JSON_EXTENSION, file);
        } catch (Exception e) {
            log.error("Error while hosting policy json on path " + hostedPath, e);
        } finally {
            CommonUtils.deleteFile(file);
        }
    }

    public String[] getLocationByServiceOfferingId(String serviceOfferingId) {
        JsonNode serviceOffer = this.getServiceOffering(serviceOfferingId);
        JsonNode policyArray = this.getPolicyArrayFromServiceOffer(serviceOffer);

        if (policyArray != null && policyArray.has(0)) {
            Constraint constraint = this.getLocationConstraintFromPolicy(policyArray);

            if (constraint != null) {
                return constraint.getRightOperand();
            }
        }

        return new String[]{};
    }

    private JsonNode getPolicyArrayFromServiceOffer(JsonNode serviceOffer) {
        JsonNode selfDescriptionCredential = serviceOffer.get("selfDescriptionCredential");
        ObjectReader reader = this.objectMapper.readerFor(new TypeReference<List<JsonNode>>() {
        });

        List<JsonNode> list = new ArrayList<>();
        try {
            list = reader.readValue(selfDescriptionCredential.get("verifiableCredential"));
        } catch (IOException ignored) {
            log.info("Error encountered while parsing service for policy");
        }

        Optional<JsonNode> policyVcOptional = list.stream()
                .filter(vc -> vc.get(StringPool.CREDENTIAL_SUBJECT).get("type").asText().equals("gx:ServiceOffering"))
                .findFirst();
        if (policyVcOptional.isPresent() && policyVcOptional.get().get(StringPool.CREDENTIAL_SUBJECT).has(StringPool.GX_POLICY)) {
            return policyVcOptional.get().get(StringPool.CREDENTIAL_SUBJECT).get(StringPool.GX_POLICY);
        }

        return null;
    }

    @SneakyThrows
    private JsonNode getCatalogueDescription(String catalogueUrl) {
        String catalogue = InvokeService.executeRequest(catalogueUrl, HttpMethod.GET);
        Validate.isNull(catalogue).launch(new BadDataException("Invalid Catalogue URL"));
        return this.objectMapper.readTree(catalogue);
    }

    @SneakyThrows
    private JsonNode getServiceOffering(String serviceOfferingUrl) {
        String serviceOffering = InvokeService.executeRequest(serviceOfferingUrl, HttpMethod.GET);
        Validate.isNull(serviceOffering).launch(new BadDataException("Invalid Service Offering ID"));
        return this.objectMapper.readValue(serviceOffering, JsonNode.class);
    }

    private Policy getPolicyForServiceOffer(String policyUrl) {
        String policy = InvokeService.executeRequest(policyUrl, HttpMethod.GET);
        Validate.isNull(policy).launch(new BadDataException("Invalid Policy URL"));

        try {
            return this.objectMapper.readValue(policy, Policy.class);
        } catch (Exception e) {
            log.info("Error while converting policy from string to object");
            return null;
        }
    }

    private Constraint getLocationConstraintFromPolicy(String policyUrl) {
        Policy accessPolicy = this.getPolicyForServiceOffer(policyUrl);
        if (accessPolicy == null) {
            return null;
        }

        Optional<Rule> rule = accessPolicy.getPermission().stream().filter(permission -> permission.getAction().equalsIgnoreCase("use")).findAny();
        Constraint constraint = null;
        if (rule.isPresent() && !CollectionUtils.isEmpty(rule.get().getConstraint())) {
            constraint = rule.get().getConstraint().stream()
                    .filter(c -> c.getName().equalsIgnoreCase(SPATIAL))
                    .findAny()
                    .orElse(null);
        }
        return constraint;
    }

    private Constraint getLocationConstraintFromPolicy(JsonNode policyArray) {
        List<String> policyUrlList = this.objectMapper.convertValue(policyArray, List.class);
        if (CollectionUtils.isEmpty(policyUrlList)) {
            throw new EntityNotFoundException("Policy not found for the specified entity.");
        }

        Policy accessPolicy;
        Constraint constraint = null;
        for (String policyUrl : policyUrlList) {
            accessPolicy = this.getPolicyForServiceOffer(policyUrl);

            if (accessPolicy != null && accessPolicy.getPermission().stream().anyMatch(rule -> rule.getAction().equalsIgnoreCase("use"))) {
                Optional<Rule> rule = accessPolicy.getPermission().stream().filter(permission -> permission.getAction().equalsIgnoreCase("use")).findAny();
                if (rule.isPresent() && !CollectionUtils.isEmpty(rule.get().getConstraint())) {
                    constraint = rule.get().getConstraint().stream()
                            .filter(c -> c.getName().equalsIgnoreCase(SPATIAL))
                            .findAny()
                            .orElse(null);
                }

                break;
            }
        }

        return constraint;
    }

    private boolean isCountryInPermittedRegion(List<String> countryCode, Constraint constraint) {

        if (constraint == null) {
//            no location constraint found, allow access
            return true;
        } else if (constraint.getOperator().equals("isAnyOf")) {
            ArrayList<String> countryCodeNew = new ArrayList<>(countryCode);
            countryCodeNew.retainAll(Arrays.asList(constraint.getRightOperand()));
            return !CollectionUtils.isEmpty(countryCodeNew);
        }

        return false;
    }

    private List<String> getCountryCodeFromSelfDescription(JsonNode catalogSelfDescription) {

        JsonNode policyArray = this.getPolicyArrayFromServiceOffer(catalogSelfDescription);

        if (policyArray != null && policyArray.has(0)) {

            for (JsonNode policyUrlJsonNode : policyArray) {
                String policyUrl = policyUrlJsonNode.asText();
                if (policyUrl.endsWith(JSON_EXTENSION)) {
                    Constraint constraint = this.getLocationConstraintFromPolicy(policyUrl);

                    if (constraint != null && constraint.getName().equals("spatial")) {
                        return List.of(constraint.getRightOperand());
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    public boolean evaluatePolicy(PolicyEvaluationRequest policyEvaluationRequest) {
        JsonNode catalogueDescription = this.getCatalogueDescription(policyEvaluationRequest.catalogueUrl());
        List<String> countryCode;

        try {
            countryCode = this.getCountryCodeFromSelfDescription(catalogueDescription);
        } catch (Exception e) {
            throw new BadDataException("catalogue.location.invalid");
        }

        if (CollectionUtils.isEmpty(countryCode)) {
            throw new BadDataException("catalogue.location.invalid");
        }

        JsonNode serviceOffer = this.getServiceOffering(policyEvaluationRequest.serviceOfferId());
        JsonNode policyArray = this.getPolicyArrayFromServiceOffer(serviceOffer);

        if (policyArray != null && policyArray.has(0)) {
            for (JsonNode policyUrlJsonNode : policyArray) {
                String policyUrl = policyUrlJsonNode.asText();
                if (policyUrl.endsWith(JSON_EXTENSION)) {
                    Constraint constraint = this.getLocationConstraintFromPolicy(policyUrl);

                    if (!this.isCountryInPermittedRegion(countryCode, constraint)) {
                        return false;
                    }

                }
            }
        }

        return true;
    }

}
