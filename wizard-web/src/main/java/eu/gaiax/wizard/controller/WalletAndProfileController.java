package eu.gaiax.wizard.controller;

import com.smartsensesolutions.java.commons.FilterRequest;
import eu.gaiax.wizard.api.model.CommonResponse;
import eu.gaiax.wizard.api.model.PageResponse;
import eu.gaiax.wizard.api.utils.StringPool;
import eu.gaiax.wizard.core.service.credential.CredentialViewService;
import eu.gaiax.wizard.dao.entity.credential.CredentialView;
import eu.gaiax.wizard.utils.WizardRestConstant;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RestController
public class WalletAndProfileController {

    private final CredentialViewService credentialViewService;

    @PostMapping(path = WizardRestConstant.PARTICIPANT_CREDENTIAL, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<PageResponse<CredentialView>> validateResource(@PathVariable(name = StringPool.PARTICIPANT_ID) String participantId, @Valid @RequestBody FilterRequest filterRequest) {
        return CommonResponse.of(this.credentialViewService.filterCredentialView(participantId, filterRequest));
    }
}
