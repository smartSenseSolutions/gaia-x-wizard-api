/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.client;

import com.fasterxml.jackson.databind.JsonNode;
import eu.gaiax.wizard.api.model.CreateVCRequest;
import eu.gaiax.wizard.api.model.ParticipantVerifyRequest;
import eu.gaiax.wizard.api.model.did.CreateDidRequest;
import eu.gaiax.wizard.api.model.did.ValidateDidRequest;
import eu.gaiax.wizard.api.model.service_offer.SignerServiceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(value = "Signerapi", url = "${wizard.host.signer}")
public interface SignerClient {

    @PostMapping(path = "/v1/create-web-did", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, Object>> createDid(@RequestBody CreateDidRequest createDidRequest);

    @PostMapping(path = "/v1/gaia-x/legal-participant", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, Object>> createVc(@RequestBody CreateVCRequest request);

    @PostMapping(path = "/v1/gaia-x/verify", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<JsonNode> verify(@RequestBody ParticipantVerifyRequest request);

    @PostMapping(path = "/v1/gaia-x/service-offering", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, Object>> createServiceOfferVc(@RequestBody SignerServiceRequest request);

    @PostMapping(path = "/v1/gaia-x/resource", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, Object>> signResource(@RequestBody Map<String, Object> request);

    @PostMapping(path = "/v1/verify-web-did", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, Object>> validateDid(@RequestBody ValidateDidRequest request);

    @PostMapping(path = "/v1/gaia-x/label-level", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, Object>> signLabelLevel(@RequestBody Map<String, Object> request);

    @PostMapping(path = "/v1/gaia-x/validate-registration-number", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, Object>> validateRegistrationNumber(@RequestBody Map<String, Object> request);
}
