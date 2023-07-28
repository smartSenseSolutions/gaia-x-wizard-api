package eu.gaiax.wizard.api.client;

import eu.gaiax.wizard.api.model.GaiaXTermsConditionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "GaiaXRegistryClient", url = "${wizard.gaia-x.registryService}")
public interface GaiaXRegistryClient {
    @PostMapping(path = "/api/termsAndConditions", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<GaiaXTermsConditionDto> getGaiaXTermsAndConditions();
}
