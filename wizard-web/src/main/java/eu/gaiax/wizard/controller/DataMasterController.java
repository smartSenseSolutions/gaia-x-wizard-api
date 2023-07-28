package eu.gaiax.wizard.controller;

import com.smartsensesolutions.java.commons.FilterRequest;
import eu.gaiax.wizard.api.model.CommonResponse;
import eu.gaiax.wizard.api.model.LabelLevelTypeInterface;
import eu.gaiax.wizard.core.service.data_master.AccessTypeMasterService;
import eu.gaiax.wizard.core.service.data_master.EntityTypeMasterService;
import eu.gaiax.wizard.core.service.data_master.FormatTypeMasterService;
import eu.gaiax.wizard.core.service.data_master.LabelLevelService;
import eu.gaiax.wizard.core.service.data_master.RegistrationTypeMasterService;
import eu.gaiax.wizard.core.service.data_master.RequestTypeMasterService;
import eu.gaiax.wizard.core.service.data_master.StandardTypeMasterService;
import eu.gaiax.wizard.core.service.data_master.SubdivisionCodeMasterService;
import eu.gaiax.wizard.dao.entity.data_master.AccessTypeMaster;
import eu.gaiax.wizard.dao.entity.data_master.EntityTypeMaster;
import eu.gaiax.wizard.dao.entity.data_master.FormatTypeMaster;
import eu.gaiax.wizard.dao.entity.data_master.RegistrationTypeMaster;
import eu.gaiax.wizard.dao.entity.data_master.RequestTypeMaster;
import eu.gaiax.wizard.dao.entity.data_master.StandardTypeMaster;
import eu.gaiax.wizard.dao.entity.data_master.SubdivisionCodeMaster;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static eu.gaiax.wizard.utils.WizardRestConstant.ACCESS_TYPE_FILTER;
import static eu.gaiax.wizard.utils.WizardRestConstant.ENTITY_TYPE_FILTER;
import static eu.gaiax.wizard.utils.WizardRestConstant.FORMAT_TYPE_FILTER;
import static eu.gaiax.wizard.utils.WizardRestConstant.LABEL_LEVEL_QUESTIONS;
import static eu.gaiax.wizard.utils.WizardRestConstant.LOCATION_FILTER;
import static eu.gaiax.wizard.utils.WizardRestConstant.REGISTRATION_TYPE_FILTER;
import static eu.gaiax.wizard.utils.WizardRestConstant.REQUEST_TYPE_FILTER;
import static eu.gaiax.wizard.utils.WizardRestConstant.STANDARD_TYPE_FILTER;


@RestController
@RequiredArgsConstructor
public class DataMasterController extends BaseResource {

    private final AccessTypeMasterService accessTypeMasterService;

    private final EntityTypeMasterService entityTypeMasterService;

    private final FormatTypeMasterService formatTypeMasterService;

    private final RegistrationTypeMasterService registrationTypeMasterService;

    private final RequestTypeMasterService requestTypeMasterService;

    private final StandardTypeMasterService standardTypeMasterService;

    private final LabelLevelService labelLevelService;

    private final SubdivisionCodeMasterService subdivisionCodeMasterService;

    @PostMapping(ACCESS_TYPE_FILTER)
    public CommonResponse<Page<AccessTypeMaster>> filterAccessTypeMaster(@RequestBody FilterRequest filterRequest) {
        return CommonResponse.of(this.accessTypeMasterService.filter(filterRequest));
    }

    @PostMapping(ENTITY_TYPE_FILTER)
    public CommonResponse<Page<EntityTypeMaster>> filterEntityTypeMaster(@RequestBody FilterRequest filterRequest) {
        return CommonResponse.of(this.entityTypeMasterService.filter(filterRequest));
    }

    @PostMapping(FORMAT_TYPE_FILTER)
    public CommonResponse<Page<FormatTypeMaster>> filterFormatTypeMaster(@RequestBody FilterRequest filterRequest) {
        return CommonResponse.of(this.formatTypeMasterService.filter(filterRequest));
    }

    @PostMapping(REGISTRATION_TYPE_FILTER)
    public CommonResponse<Page<RegistrationTypeMaster>> filterRegistrationTypeMaster(@RequestBody FilterRequest filterRequest) {
        return CommonResponse.of(this.registrationTypeMasterService.filter(filterRequest));
    }

    @PostMapping(REQUEST_TYPE_FILTER)
    public CommonResponse<Page<RequestTypeMaster>> filterRequestTypeMaster(@RequestBody FilterRequest filterRequest) {
        return CommonResponse.of(this.requestTypeMasterService.filter(filterRequest));
    }

    @PostMapping(STANDARD_TYPE_FILTER)
    public CommonResponse<Page<StandardTypeMaster>> filterStandardTypeMaster(@RequestBody FilterRequest filterRequest) {
        return CommonResponse.of(this.standardTypeMasterService.filter(filterRequest));
    }

    @GetMapping(LABEL_LEVEL_QUESTIONS)
    public CommonResponse<List<LabelLevelTypeInterface>> getLabelLevelQuestions() {
        return CommonResponse.of(this.labelLevelService.getLabelLevelTypeAndQuestionList());
    }

    @PostMapping(LOCATION_FILTER)
    public CommonResponse<Page<SubdivisionCodeMaster>> filterSubdivisionCodeMaster(@RequestBody FilterRequest filterRequest) {
        return CommonResponse.of(this.subdivisionCodeMasterService.filter(filterRequest));
    }

}
