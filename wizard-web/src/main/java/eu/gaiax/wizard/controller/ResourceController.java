package eu.gaiax.wizard.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smartsensesolutions.java.commons.FilterRequest;
import eu.gaiax.wizard.api.model.CommonResponse;
import eu.gaiax.wizard.api.model.PageResponse;
import eu.gaiax.wizard.api.model.ResourceFilterResponse;
import eu.gaiax.wizard.api.model.StringPool;
import eu.gaiax.wizard.api.model.service_offer.CreateResourceRequest;
import eu.gaiax.wizard.core.service.service_offer.ResourceService;
import eu.gaiax.wizard.dao.entity.resource.Resource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import static eu.gaiax.wizard.utils.WizardRestConstant.PARTICIPANT_RESOURCE_FILTER;
import static eu.gaiax.wizard.utils.WizardRestConstant.RESOURCE_FILTER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
public class ResourceController extends BaseController {

    private final ResourceService resourceService;

    @Tag(name = "Resources")
    @Operation(summary = "Create Resource")
    @PostMapping(path = "/resource", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Resource> createResource(@Valid @RequestBody CreateResourceRequest request, Principal principal) throws JsonProcessingException {
        return CommonResponse.of(this.resourceService.createResource(request, request.email()));
    }

    @Tag(name = "Resources")
    @Operation(summary = "Create Resource")
    @PostMapping(path = "public/resource", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<Resource> createPublicResource(@Valid @RequestBody CreateResourceRequest request) throws JsonProcessingException {
        return CommonResponse.of(this.resourceService.createResource(request, null));
    }

    @Tag(name = "Resources")
    @Operation(summary = "Resource filter API")
    @PostMapping(path = {RESOURCE_FILTER}, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<PageResponse<ResourceFilterResponse>> filterResource(@Valid @RequestBody FilterRequest filterRequest) {
        return CommonResponse.of(this.resourceService.filterResource(filterRequest, null));
    }

    @Tag(name = "Resources")
    @Operation(summary = "Resource filter API")
    @PostMapping(path = {PARTICIPANT_RESOURCE_FILTER}, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CommonResponse<PageResponse<ResourceFilterResponse>> filterResource(@PathVariable(name = StringPool.PARTICIPANT_ID) String participantId, @Valid @RequestBody FilterRequest filterRequest, Principal principal) {
        return CommonResponse.of(this.resourceService.filterResource(filterRequest, participantId));
    }

}
