/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.client;

import eu.gaiax.wizard.api.model.CreateDidRequest;
import eu.gaiax.wizard.api.model.CreateVCRequest;
import eu.gaiax.wizard.api.model.CreateVPRequest;
import eu.gaiax.wizard.api.model.ServiceOffer.SignerServiceRequest;
import eu.gaiax.wizard.api.model.VerifyRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(value = "Signerapi", url = "${wizard.signer.host}")
public interface SignerClient {

    @PostMapping(path = "createWebDID", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, Object>> createDid(@RequestBody CreateDidRequest createDidRequest);

    @PostMapping(path = "/v1/LegalParticipantOnGaiaX", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, Object>> createVc(@RequestBody CreateVCRequest request);


    @PostMapping(path = "createVP", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, Object>> createVP(@RequestBody CreateVPRequest request);

    @PostMapping(path = "verify", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, Object>> verify(@RequestBody VerifyRequest request);

    @PostMapping(path = "createServiceOfferVc", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, Object>> createServiceOfferVc(@RequestBody SignerServiceRequest request);


}
