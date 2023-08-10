package eu.gaiax.wizard.controller;

import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.model.*;
import eu.gaiax.wizard.api.utils.Validate;
import eu.gaiax.wizard.core.service.domain.DomainService;
import eu.gaiax.wizard.core.service.k8s.K8SService;
import eu.gaiax.wizard.core.service.participant.ParticipantService;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantCreationRequest;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantRegisterRequest;
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

import static eu.gaiax.wizard.utils.WizardRestConstant.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
public class ParticipantController extends BaseController {

    private final ParticipantService participantService;
    private final DomainService domainService;
    private final CertificateService certificateService;
    private final K8SService k8SService;
    private final SignerService signerService;

    @Operation(
            summary = "Check for user existence",
            description = "This endpoint used to check whether user exists or not."
    )
    @GetMapping(value = CHECK_REGISTRATION, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Map<String, Object>> checkIfParticipantRegistered(@RequestParam(name = "email") String email) {
        return CommonResponse.of(this.participantService.checkIfParticipantRegistered(email));
    }

    @Operation(
            summary = "Register Participant",
            description = "This endpoint used to register participants."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Participant registered successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid participant registered request."),
    })
    @PostMapping(value = REGISTER, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Participant> registerParticipant(@RequestBody ParticipantRegisterRequest request) {
        return CommonResponse.of(this.participantService.registerParticipant(request));
    }

    @Operation(
            summary = "Onboard Participant",
            description = "This endpoint used to onboard participants."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Participant registered successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid participant onboarded request."),
    })
    @PostMapping(value = ONBOARD_PARTICIPANT, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Participant> registerParticipant(@PathVariable("participantId") String participantId, @RequestBody ParticipantCreationRequest request) {
        return CommonResponse.of(this.participantService.initiateOnboardParticipantProcess(participantId, request));
    }

    @Operation(
            summary = "Validate Participant Json",
            description = "This endpoint used to validate participant json."
    )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Participant onboarded successfully.")})
    @PostMapping(value = VALIDATE_PARTICIPANT, consumes = APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String validateParticipant(@RequestBody ParticipantValidatorRequest request) {
        this.participantService.validateParticipant(request);
        return "Success";
    }

    @Operation(
            summary = "Get .well-known files",
            description = "This endpoint used to fetch well-known files."
    )
    @GetMapping(path = WELL_KNOWN, produces = APPLICATION_JSON_VALUE)
    public String getWellKnownFiles(@PathVariable(name = "fileName") String fileName, @RequestHeader(name = HttpHeaders.HOST) String host) throws IOException {
        return this.participantService.getWellKnownFiles(host, fileName);
    }

    @Operation(
            summary = "Get participant json files",
            description = "This endpoint used to fetch participant json details."
    )
    @GetMapping(path = PARTICIPANT_JSON, produces = APPLICATION_JSON_VALUE)
    public String getLegalParticipantJson(@PathVariable(name = "participantId") String participantId, @PathVariable("fileName") String fileName) throws IOException {
        return this.participantService.getLegalParticipantJson(participantId, fileName);
    }

    @Operation(summary = "Resume onboarding process from sub domain creation, role Admin, (only used for manual step in case of failure)")
    @GetMapping(path = PARTICIPANT_SUBDOMAIN, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Map<String, String>> createSubDomain(@PathVariable(name = "participantId") String participantId) {
        this.domainService.createSubDomain(UUID.fromString(participantId));
        return CommonResponse.of(Map.of("message", "Subdomain creation started"));
    }

    @Operation(summary = "Resume onboarding process from SLL certificate creation, role = admin, (only used for manual step in case of failure)")
    @GetMapping(path = PARTICIPANT_CERTIFICATE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Participant> createCertificate(@PathVariable(name = "participantId") String participantId) {
        Participant participant = this.participantService.get(UUID.fromString(participantId));
        Validate.isTrue(participant.getStatus() != RegistrationStatus.CERTIFICATE_CREATION_FAILED.getStatus()).launch("Status is not certification creation failed");
        participant = this.participantService.changeStatus(UUID.fromString(participantId), RegistrationStatus.CERTIFICATE_CREATION_IN_PROCESS.getStatus());
        this.certificateService.createSSLCertificate(UUID.fromString(participantId), null);
        return CommonResponse.of(participant);
    }


    @Operation(summary = "Resume onboarding process from ingress creation, role = admin, (only used for manual step in case of failure)")
    @GetMapping(path = PARTICIPANT_INGRESS, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Map<String, String>> createIngress(@PathVariable(name = "participantId") String participantId) {
        this.k8SService.createIngress(UUID.fromString(participantId));
        return CommonResponse.of(Map.of("message", "Ingress creation started"));
    }

    @Operation(summary = "Resume onboarding process from did creation, role-=admin, (only used for manual step in case of failure)")
    @GetMapping(path = PARTICIPANT_DID, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Map<String, String>> createDid(@PathVariable(name = "participantId") String participantId) {
        this.signerService.createDid(UUID.fromString(participantId));
        return CommonResponse.of(Map.of("message", "did creation started"));
    }

    @Operation(summary = "Resume onboarding process from participant credential creation, role Admin, (only used for manual step in case of failure)")
    @GetMapping(path = CREATE_PARTICIPANT, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Map<String, String>> createParticipantJson(@PathVariable(name = "participantId") String participantId) {
        this.signerService.createParticipantJson(UUID.fromString(participantId));
        return CommonResponse.of(Map.of("message", "participant json creation started"));
    }

    @Operation(
            summary = "Participant config",
            description = "This endpoint returns participant's general configuration."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Participant config fetched successfully."),
            @ApiResponse(responseCode = "401", description = "Unauthorized access."),
            @ApiResponse(responseCode = "403", description = "User does not have access to this API."),
            @ApiResponse(responseCode = "404", description = "Participant not found.")
    }
    )
    @GetMapping(PARTICIPANT_CONFIG)
    public CommonResponse<ParticipantConfigDTO> getConfig(Principal principal) {
        String userId = (String) this.requestForClaim(StringPool.ID, principal);
        Validate.isNull(userId).launch(new BadDataException("User ID not present in token"));

        return CommonResponse.of(this.participantService.getParticipantConfig(userId));
    }

    @Operation(
            summary = "Resend registration email",
            description = "This endpoint sends registration email to the user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully."),
            @ApiResponse(responseCode = "400", description = "User not registered.")
    }
    )
    @PostMapping(SEND_REQUIRED_ACTIONS_EMAIL)
    public CommonResponse<Object> sendRequiredActionsEmail(@RequestBody SendRegistrationEmailRequest sendRegistrationEmailRequest) {
        this.participantService.sendRegistrationLink(sendRegistrationEmailRequest.email());
        return CommonResponse.of("Registration email sent successfully");
    }

}