package eu.gaiax.wizard.core.service.service_offer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.exception.EntityNotFoundException;
import eu.gaiax.wizard.api.exception.ForbiddenAccessException;
import eu.gaiax.wizard.api.model.StringPool;
import eu.gaiax.wizard.api.model.policy.Constraint;
import eu.gaiax.wizard.api.model.policy.Policy;
import eu.gaiax.wizard.api.model.policy.Rule;
import eu.gaiax.wizard.api.model.service_offer.ODRLPolicyRequest;
import eu.gaiax.wizard.api.model.service_offer.PolicyEvaluationRequest;
import eu.gaiax.wizard.api.model.setting.ContextConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import static eu.gaiax.wizard.api.model.StringPool.POLICY_LOCATION_LEFT_OPERAND;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyService {

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    private final ContextConfig contextConfig;

    public String createPolicy(ODRLPolicyRequest odrlPolicyRequest, String hostUrl) throws IOException {
        Map<String, Object> ODRLPolicy = new HashMap<>();
        ODRLPolicy.put("@context", this.contextConfig.ODRLPolicy());
        ODRLPolicy.put("type", "policy");
        if (hostUrl == null) {
            hostUrl = odrlPolicyRequest.domain() + odrlPolicyRequest.target() + "/" + odrlPolicyRequest.serviceName() + "_policy.json";
        }
        ODRLPolicy.put("id", hostUrl);
        List<Map<String, Object>> permission = getMaps(odrlPolicyRequest.rightOperand(), odrlPolicyRequest.target(), odrlPolicyRequest.assigner(), odrlPolicyRequest.leftOperand());
        ODRLPolicy.put("permission", permission);
        return this.objectMapper.writeValueAsString(ODRLPolicy);
    }

    @NotNull
    private static List<Map<String, Object>> getMaps(List<String> rightOperand, String target, String assigner, String leftOperand) {
        List<Map<String, Object>> permission = new ArrayList<>();
        Map<String, Object> perMap = new HashMap<>();
        perMap.put("target", target);
        perMap.put("assigner", assigner);
        perMap.put("action", "view");
        List<Map<String, Object>> constraint = new ArrayList<>();
        Map<String, Object> constraintMap = new HashMap<>();
        constraintMap.put("leftOperand", leftOperand);
        constraintMap.put("operator", "isAnyOf");
        constraintMap.put("rightOperand", rightOperand);
        constraint.add(constraintMap);
        perMap.put("constraint", constraint);
        permission.add(perMap);
        return permission;
    }

    public String[] getLocationByServiceOfferingId(String serviceOfferingId) {
        JsonNode serviceOffer = this.getServiceOffering(serviceOfferingId);
        String policyUrl = this.getPolicyUrlFromServiceOffer(serviceOffer);

        if (StringUtils.hasText(policyUrl)) {
            Policy accessPolicy = this.getPolicyForServiceOffer(policyUrl);
            if (accessPolicy == null) {
                return new String[]{};
            }

            Optional<Rule> rule = accessPolicy.getPermission().stream().filter(permission -> permission.getAction().equalsIgnoreCase("view")).findAny();
            Constraint constraint;
            if (rule.isPresent() && !CollectionUtils.isEmpty(rule.get().getConstraint())) {
                constraint = rule.get().getConstraint().stream()
                        .filter(c -> c.getLeftOperand().equalsIgnoreCase(POLICY_LOCATION_LEFT_OPERAND))
                        .findAny()
                        .orElse(null);

                if (constraint != null) {
                    return constraint.getRightOperand();
                }
            }

        }

        return new String[]{};
    }

    public JsonNode evaluatePolicy(PolicyEvaluationRequest policyEvaluationRequest) {
        JsonNode catalogueDescription = this.getCatalogueDescription(policyEvaluationRequest.catalogueUrl());
        String countryCode;

        try {
            countryCode = this.getCountryCodeFromSelfDescription(catalogueDescription);
        } catch (Exception e) {
            throw new BadDataException("Legal Address does not have country parameter");
        }

        if (!StringUtils.hasText(countryCode)) {
            throw new BadDataException("Legal Address does not have country parameter");
        }

        JsonNode serviceOffer = this.getServiceOffering(policyEvaluationRequest.serviceOfferId());
        String policyUrl = this.getPolicyUrlFromServiceOffer(serviceOffer);

        if (StringUtils.hasText(policyUrl)) {
            Policy accessPolicy = this.getPolicyForServiceOffer(policyUrl);
            if (accessPolicy == null) {
                throw new EntityNotFoundException("Policy not found for the specified entity.");
            }

            Optional<Rule> rule = accessPolicy.getPermission().stream().filter(permission -> permission.getAction().equalsIgnoreCase("view")).findAny();
            Constraint constraint = null;
            if (rule.isPresent() && !CollectionUtils.isEmpty(rule.get().getConstraint())) {
                constraint = rule.get().getConstraint().stream()
                        .filter(c -> c.getLeftOperand().equalsIgnoreCase(POLICY_LOCATION_LEFT_OPERAND))
                        .findAny()
                        .orElse(null);
            }

            if (!this.isCountryInPermittedRegion(countryCode, constraint)) {
                throw new ForbiddenAccessException("The catalogue does not have permission to view this entity.");
            }
        }

        return serviceOffer;
    }

    private String getPolicyUrlFromServiceOffer(JsonNode serviceOffer) {
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
            return policyVcOptional.get().get(StringPool.CREDENTIAL_SUBJECT).get(StringPool.GX_POLICY).asText();
        }

        return null;
    }

    private JsonNode getCatalogueDescription(String catalogueUrl) {
        ResponseEntity<JsonNode> catalogueResponse = this.restTemplate.getForEntity(URI.create(catalogueUrl), JsonNode.class);
        if (catalogueResponse.getStatusCode().is2xxSuccessful()) {
            return catalogueResponse.getBody();
        }

        throw new BadDataException("Invalid Catalogue URL");
    }

    private JsonNode getServiceOffering(String serviceOfferingUrl) {
        ResponseEntity<JsonNode> serviceOfferingResponse = this.restTemplate.getForEntity(URI.create(serviceOfferingUrl), JsonNode.class);
        if (serviceOfferingResponse.getStatusCode().is2xxSuccessful()) {
            return serviceOfferingResponse.getBody();
        }

        throw new BadDataException("Invalid Service Offering ID");
    }

    private Policy getPolicyForServiceOffer(String policyUrl) {
        ResponseEntity<Policy> policyResponse = this.restTemplate.getForEntity(URI.create(policyUrl), Policy.class);
        if (policyResponse.getStatusCode().is2xxSuccessful()) {
            return policyResponse.getBody();
        }

        throw new BadDataException("Invalid Policy URL");
    }

    private boolean isCountryInPermittedRegion(String countryCode, Constraint constraint) {

        if (constraint == null) {
//            no location constraint found, allow access
            return true;
        } else if (constraint.getOperator().equals("isAnyOf")) {
            return Arrays.stream(constraint.getRightOperand()).anyMatch(policyCountry -> policyCountry.equalsIgnoreCase(countryCode));
        }

        return false;
    }

    private String getCountryCodeFromSelfDescription(JsonNode catalogSelfDescription) {
        JsonNode legalAddress = catalogSelfDescription.get(StringPool.GX_LEGAL_ADDRESS);
        return legalAddress.get(StringPool.GX_COUNTRY_SUBDIVISION).asText();
    }
}
