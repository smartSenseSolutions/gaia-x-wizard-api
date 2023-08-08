package eu.gaiax.wizard.core.service.service_offer;

import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.exception.EntityNotFoundException;
import eu.gaiax.wizard.api.exception.ForbiddenAccessException;
import eu.gaiax.wizard.api.model.policy_evaluator.Constraint;
import eu.gaiax.wizard.api.model.policy_evaluator.Policy;
import eu.gaiax.wizard.api.model.policy_evaluator.Rule;
import eu.gaiax.wizard.api.model.service_offer.PolicyEvaluationRequest;
import eu.gaiax.wizard.api.model.service_offer.ServiceOfferResponse;
import eu.gaiax.wizard.api.utils.Validate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PolicyEvaluatorService {

    private final RestTemplate restTemplate;

    public String evaluatePolicy(PolicyEvaluationRequest policyEvaluationRequest) {
        Map catalogueDescription = this.getCatalogueDescription(policyEvaluationRequest.catalogueUrl());
        String countryCode = null;

        try {
            countryCode = this.getCountryCodeFromSelfDescription(catalogueDescription);
        } catch (Exception ignored) {

        }

        if (!StringUtils.hasText(countryCode)) {
            throw new BadDataException("Legal Address does not have country parameter");
        }

        ServiceOfferResponse serviceOffer = this.getServiceOffering(policyEvaluationRequest.serviceOfferId());
        String policyUrl = this.getPolicyUrlFromServiceOffer(serviceOffer);
        Validate.isNull(policyUrl).launch(new BadDataException("No policy found for service" + serviceOffer.getName()));

        Policy serviceOfferingPolicy = this.getPolicyForServiceOffer(policyUrl);
        if (serviceOfferingPolicy == null) {
            throw new EntityNotFoundException("Policy not found for the specified service offering.");
        }

        Optional<Rule> rule = serviceOfferingPolicy.getPermission().stream().filter(permission -> permission.getAction().equalsIgnoreCase("view")).findAny();
        Constraint constraint = null;
        if (rule.isPresent() && !CollectionUtils.isEmpty(rule.get().getConstraint())) {
            constraint = rule.get().getConstraint().stream().filter(c -> c.getLeftOperand().equalsIgnoreCase("legalAddress.country")).findAny().orElse(null);
        }
        if (!this.isCountryInPermittedRegion(countryCode, constraint)) {
            throw new ForbiddenAccessException("The catalog does not have permission to view this service.");
        }

        return serviceOffer.getVcUrl();
    }

    private String getPolicyUrlFromServiceOffer(ServiceOfferResponse serviceOffer) {
        List<Map<String, Object>> vcJsonList = serviceOffer.getVcJson();
        Optional<Map<String, Object>> policyVcOptional = vcJsonList.stream()
                .filter(vc -> vc.containsKey("gx:policy")).findFirst();

        return policyVcOptional
                .map(stringObjectMap -> (String) stringObjectMap.get("gx:policy"))
                .orElse(null);
    }

    private Map getCatalogueDescription(String catalogueUrl) {
        ResponseEntity<Map> catalogueResponse = this.restTemplate.getForEntity(URI.create(catalogueUrl), Map.class);
        if (catalogueResponse.getStatusCode().is2xxSuccessful()) {
            return catalogueResponse.getBody();
        }
        throw new BadDataException("Invalid Catalogue URL");
    }

    private ServiceOfferResponse getServiceOffering(String serviceOfferingUrl) {
        ResponseEntity<ServiceOfferResponse> serviceOfferingResponse = this.restTemplate.getForEntity(URI.create(serviceOfferingUrl), ServiceOfferResponse.class);
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
//            no country constraint found, allow access
            return true;
        } else if (constraint.getOperator().equals("isAnyOf")) {
            return Arrays.stream(constraint.getRightOperand()).anyMatch(policyCountry -> policyCountry.equalsIgnoreCase(countryCode));
        }

        return false;
    }

    private String getCountryCodeFromSelfDescription(Map catalogSelfDescription) {
        Map<String, Object> legalAddress = (Map<String, Object>) catalogSelfDescription.get("legalAddress");
        return legalAddress.get("country").toString();
    }
}
