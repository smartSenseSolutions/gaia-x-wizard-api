package eu.gaiax.wizard.controller;

import com.smartsensesolutions.java.commons.FilterRequest;
import eu.gaiax.wizard.core.service.data_master.AccessTypeMasterService;
import eu.gaiax.wizard.core.service.data_master.EntityTypeMasterService;
import eu.gaiax.wizard.core.service.data_master.FormatTypeMasterService;
import eu.gaiax.wizard.core.service.data_master.RegistrationTypeMasterService;
import eu.gaiax.wizard.core.service.data_master.RequestTypeMasterService;
import eu.gaiax.wizard.core.service.data_master.StandardTypeMasterService;
import eu.gaiax.wizard.dao.entity.data_master.AccessTypeMaster;
import eu.gaiax.wizard.dao.entity.data_master.EntityTypeMaster;
import eu.gaiax.wizard.dao.entity.data_master.FormatTypeMaster;
import eu.gaiax.wizard.dao.entity.data_master.RegistrationTypeMaster;
import eu.gaiax.wizard.dao.entity.data_master.RequestTypeMaster;
import eu.gaiax.wizard.dao.entity.data_master.StandardTypeMaster;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static eu.gaiax.wizard.utils.WizardRestConstant.ACCESS_TYPE_FILTER;
import static eu.gaiax.wizard.utils.WizardRestConstant.ENTITY_TYPE_FILTER;
import static eu.gaiax.wizard.utils.WizardRestConstant.FORMAT_TYPE_FILTER;
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

    @PostMapping(ACCESS_TYPE_FILTER)
    public ResponseEntity<Page<AccessTypeMaster>> filterAccessTypeMaster(@RequestBody FilterRequest filterRequest) {
        return ResponseEntity.ok(accessTypeMasterService.filter(filterRequest));
    }

    @PostMapping(ENTITY_TYPE_FILTER)
    public ResponseEntity<Page<EntityTypeMaster>> filterEntityTypeMaster(@RequestBody FilterRequest filterRequest) {
        return ResponseEntity.ok(entityTypeMasterService.filter(filterRequest));
    }

    @PostMapping(FORMAT_TYPE_FILTER)
    public ResponseEntity<Page<FormatTypeMaster>> filterFormatTypeMaster(@RequestBody FilterRequest filterRequest) {
        return ResponseEntity.ok(formatTypeMasterService.filter(filterRequest));
    }

    @PostMapping(REGISTRATION_TYPE_FILTER)
    public ResponseEntity<Page<RegistrationTypeMaster>> filterRegistrationTypeMaster(@RequestBody FilterRequest filterRequest) {
        return ResponseEntity.ok(registrationTypeMasterService.filter(filterRequest));
    }

    @PostMapping(REQUEST_TYPE_FILTER)
    public ResponseEntity<Page<RequestTypeMaster>> filterRequestTypeMaster(@RequestBody FilterRequest filterRequest) {
        return ResponseEntity.ok(requestTypeMasterService.filter(filterRequest));
    }

    @PostMapping(STANDARD_TYPE_FILTER)
    public ResponseEntity<Page<StandardTypeMaster>> filterStandardTypeMaster(@RequestBody FilterRequest filterRequest) {
        return ResponseEntity.ok(standardTypeMasterService.filter(filterRequest));
    }

}
