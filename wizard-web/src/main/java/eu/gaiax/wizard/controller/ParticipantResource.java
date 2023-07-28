package eu.gaiax.wizard.controller;

import eu.gaiax.wizard.core.service.participant.ParticipantService;
import eu.gaiax.wizard.core.service.participant.model.request.ParticipantOnboardRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class ParticipantResource extends BaseResource {

    private final ParticipantService participantService;

    @PostMapping("/onboard/participant")
    public void onboardParticipant(ParticipantOnboardRequest request, Principal principal) {
        this.participantService.onboardParticipant(request, (String) this.requestForClaim("email", principal));
    }
}
