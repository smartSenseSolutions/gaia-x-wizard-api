package eu.gaiax.wizard.controller;

import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.model.CommonResponse;
import eu.gaiax.wizard.api.model.ParticipantConfigDTO;
import eu.gaiax.wizard.api.model.RegistrationStatus;
import eu.gaiax.wizard.core.service.domain.DomainService;
import eu.gaiax.wizard.core.service.k8s.K8SService;
import eu.gaiax.wizard.core.service.participant.ParticipantService;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantOnboardRequest;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantValidatorRequest;
import eu.gaiax.wizard.core.service.signer.SignerService;
import eu.gaiax.wizard.core.service.ssl.CertificateService;
import eu.gaiax.wizard.dao.entity.participant.Participant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import static eu.gaiax.wizard.utils.WizardRestConstant.CHECK_REGISTRATION;
import static eu.gaiax.wizard.utils.WizardRestConstant.PARTICIPANT_CONFIG;

@RestController
@RequiredArgsConstructor
public class ParticipantResource extends BaseResource {

    private final ParticipantService participantService;
    private final DomainService domainService;
    private final CertificateService certificateService;
    private final K8SService k8SService;
    private final SignerService signerService;

    @Operation(
            summary = "Onboard Participant",
            description = "This endpoint used to onboard participant."
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Participant onboarded successfully.")})
    @PostMapping(value = "/onboard/participant", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String onboardParticipant(@RequestBody ParticipantOnboardRequest request, Principal principal) {
        this.participantService.onboardParticipant(request, (String) this.requestForClaim("email", principal));
        return "Success";
    }

    @Operation(
            summary = "Validate Participant Json",
            description = "This endpoint used to validate participant json."
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Participant onboarded successfully.")})
    @PostMapping(value = "/validate/participant", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String validateParticipant(@RequestBody ParticipantValidatorRequest request) {
        this.participantService.validateParticipant(request);
        return "Success";
    }

    @Operation(
            summary = "Get .well-known files",
            description = "This endpoint used to fetch well-known files."
    )
    @GetMapping(path = ".well-known/{fileName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getEnterpriseFiles(@PathVariable(name = "fileName") String fileName, @RequestHeader(name = HttpHeaders.HOST) String host) throws IOException {
        return this.participantService.getEnterpriseFiles(host, fileName);
    }

    @Operation(
            summary = "Check if user exists",
            description = "This endpoint used to check if a user exists in the system."
    )
    @GetMapping(value = CHECK_REGISTRATION, produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Map<String, Object>> checkIfParticipantRegistered(@RequestParam(name = "email") String email) {
        return CommonResponse.of(this.participantService.checkIfParticipantRegistered(email));
    }

    @Operation(
            summary = "Get participant json files",
            description = "This endpoint used to fetch participant json details."
    )
    @GetMapping(path = "{participantId}/{fileName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getLegalParticipantJson(@PathVariable(name = "participantId") String participantId, @PathVariable("fileName") String fileName) throws IOException {
        return this.participantService.getLegalParticipantJson(participantId, fileName);
    }

    @Operation(summary = "Resume onboarding process from sub domain creation, role Admin, (only used for manual step in case of failure)")
    @GetMapping(path = "subdomain/{participantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Map<String, String>> createSubDomain(@PathVariable(name = "participantId") String participantId) {
        this.domainService.createSubDomain(UUID.fromString(participantId));
        return CommonResponse.of(Map.of("message", "Subdomain creation started"));
    }

    @Operation(summary = "Resume onboarding process from SLL certificate creation, role = admin, (only used for manual step in case of failure)")
    @GetMapping(path = "certificate/{participantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Participant> createCertificate(@PathVariable(name = "participantId") String participantId) {
        Participant participant = this.participantService.get(UUID.fromString(participantId));
        if (participant.getStatus() != RegistrationStatus.CERTIFICATE_CREATION_FAILED.getStatus()) {
            throw new BadDataException("Status is not certification creation failed");
        }
        participant = this.participantService.changeStatus(UUID.fromString(participantId), RegistrationStatus.CERTIFICATE_CREATION_IN_PROCESS.getStatus());
        this.certificateService.createSSLCertificate(UUID.fromString(participantId), null);
        return CommonResponse.of(participant);
    }


    @Operation(summary = "Resume onboarding process from ingress creation, role = admin, (only used for manual step in case of failure)")
    @GetMapping(path = "ingress/{participantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Map<String, String>> createIngress(@PathVariable(name = "participantId") String participantId) {
        this.k8SService.createIngress(UUID.fromString(participantId));
        return CommonResponse.of(Map.of("message", "Ingress creation started"));
    }

    @Operation(summary = "Resume onboarding process from did creation, role-=admin, (only used for manual step in case of failure)")
    @GetMapping(path = "did/{participantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Map<String, String>> createDid(@PathVariable(name = "participantId") String participantId) {
        this.signerService.createDid(UUID.fromString(participantId));
        return CommonResponse.of(Map.of("message", "did creation started"));
    }

    @Operation(summary = "Resume onboarding process from participant credential creation, role Admin, (only used for manual step in case of failure)")
    @GetMapping(path = "participant/{participantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Map<String, String>> createParticipantJson(@PathVariable(name = "participantId") String participantId) {
        this.signerService.createParticipantJson(UUID.fromString(participantId));
        return CommonResponse.of(Map.of("message", "participant json creation started"));
    }

    @Operation(
            summary = "Participant config",
            description = "This endpoint returns participant's general configuration."
    )
    @GetMapping(PARTICIPANT_CONFIG)
    public CommonResponse<ParticipantConfigDTO> getConfig(@RequestParam(name = "email") String email) {
        return CommonResponse.of(this.participantService.getParticipantConfig(email));
    }

}
