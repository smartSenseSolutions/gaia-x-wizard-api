package eu.gaiax.wizard.controller;

import eu.gaiax.wizard.api.model.CommonResponse;
import eu.gaiax.wizard.core.service.participant.ParticipantService;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantOnboardRequest;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantValidatorRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

import static eu.gaiax.wizard.utils.WizardRestConstant.CHECK_REGISTRATION;

@RestController
@RequiredArgsConstructor
public class ParticipantResource extends BaseResource {

    private final ParticipantService participantService;

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
    @GetMapping(path = ".well-known/{fileName}")
    public String getEnterpriseFiles(@PathVariable(name = "fileName") String fileName, @RequestHeader(name = HttpHeaders.HOST) String host) {
        return this.participantService.getParticipantFile(host, fileName);
    }

    @Operation(
            summary = "Check if user exists",
            description = "This endpoint used to check if a user exists in the system."
    )
    @GetMapping(CHECK_REGISTRATION)
    public CommonResponse<Map<String, Object>> checkIfParticipantRegistered(@RequestParam(name = "email") String email) {
        return CommonResponse.of(this.participantService.checkIfParticipantRegistered(email));
    }
}
