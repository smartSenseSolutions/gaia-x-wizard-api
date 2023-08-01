package eu.gaiax.wizard.core.service.data_master;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import com.smartsensesolutions.java.commons.base.service.BaseService;
import com.smartsensesolutions.java.commons.specification.SpecificationUtil;
import eu.gaiax.wizard.dao.entity.data_master.RequestTypeMaster;
import eu.gaiax.wizard.dao.repository.data_master.RequestTypeMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service(value = "requestTypeMaster")
@RequiredArgsConstructor
public class RequestTypeMasterService extends BaseService<RequestTypeMaster, String> {

    private final SpecificationUtil<RequestTypeMaster> specificationUtil;

    private final RequestTypeMasterRepository registrationTypeMasterRepository;

    @Override
    protected BaseRepository<RequestTypeMaster, String> getRepository() {
        return this.registrationTypeMasterRepository;
    }

    @Override
    protected SpecificationUtil<RequestTypeMaster> getSpecificationUtil() {
        return this.specificationUtil;
    }
}
