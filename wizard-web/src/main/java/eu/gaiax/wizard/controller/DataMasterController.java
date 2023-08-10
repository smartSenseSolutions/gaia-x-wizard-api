package eu.gaiax.wizard.controller;

import com.smartsensesolutions.java.commons.FilterRequest;
import com.smartsensesolutions.java.commons.base.service.BaseService;
import eu.gaiax.wizard.api.model.CommonResponse;
import eu.gaiax.wizard.api.model.LabelLevelTypeInterface;
import eu.gaiax.wizard.core.service.data_master.LabelLevelService;
import eu.gaiax.wizard.core.service.data_master.MasterDataServiceFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static eu.gaiax.wizard.utils.WizardRestConstant.LABEL_LEVEL_QUESTIONS;
import static eu.gaiax.wizard.utils.WizardRestConstant.MASTER_DATA_FILTER;


@RestController
@RequiredArgsConstructor
@Tag(name = "Master data", description = "APIs to access master data for dropdowns in forms")
public class DataMasterController extends BaseController {

    private final MasterDataServiceFactory masterDataServiceFactory;

    private final LabelLevelService labelLevelService;

    @Operation(
            summary = "Filter API to get master data",
            description = "This endpoint used to fetch master data for participant, service and resource forms."
    )
    @PostMapping(MASTER_DATA_FILTER)
    public CommonResponse<Page> filterTypeMaster(
            @PathVariable(name = "dataType") @Parameter(description = "[access, entity, format, registration, request, standard, subdivision]") String dataType,
            @RequestBody FilterRequest filterRequest) {
        BaseService service = this.masterDataServiceFactory.getInstance(dataType + "TypeMasterService");
        return CommonResponse.of(service.filter(filterRequest));
    }

    @Operation(
            summary = "API to get label level questions",
            description = "This endpoint used to fetch the label level types and questions."
    )
    @GetMapping(LABEL_LEVEL_QUESTIONS)
    public CommonResponse<List<LabelLevelTypeInterface>> getLabelLevelQuestions() {
        return CommonResponse.of(this.labelLevelService.getLabelLevelTypeAndQuestionList());
    }
}
