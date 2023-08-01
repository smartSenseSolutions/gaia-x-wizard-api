package eu.gaiax.wizard.core.service.data_master;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import com.smartsensesolutions.java.commons.base.service.BaseService;
import com.smartsensesolutions.java.commons.specification.SpecificationUtil;
import eu.gaiax.wizard.dao.entity.data_master.RegistrationTypeMaster;
import eu.gaiax.wizard.dao.repository.data_master.RegistrationTypeMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service(value = "registrationTypeMaster")
@RequiredArgsConstructor
public class RegistrationTypeMasterService extends BaseService<RegistrationTypeMaster, String> {

    private final SpecificationUtil<RegistrationTypeMaster> specificationUtil;

    private final RegistrationTypeMasterRepository registrationTypeMasterRepository;

    @Override
    protected BaseRepository<RegistrationTypeMaster, String> getRepository() {
        return this.registrationTypeMasterRepository;
    }

    @Override
    protected SpecificationUtil<RegistrationTypeMaster> getSpecificationUtil() {
        return this.specificationUtil;
    }
}
