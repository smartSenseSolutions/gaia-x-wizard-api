package eu.gaiax.wizard.core.service.data_master;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import com.smartsensesolutions.java.commons.base.service.BaseService;
import com.smartsensesolutions.java.commons.specification.SpecificationUtil;
import eu.gaiax.wizard.dao.entity.data_master.FormatTypeMaster;
import eu.gaiax.wizard.dao.repository.data_master.FormatTypeMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FormatTypeMasterService extends BaseService<FormatTypeMaster, String> {

    private final SpecificationUtil<FormatTypeMaster> specificationUtil;

    private final FormatTypeMasterRepository formatTypeMasterRepository;

    @Override
    protected BaseRepository<FormatTypeMaster, String> getRepository() {
        return formatTypeMasterRepository;
    }

    @Override
    protected SpecificationUtil<FormatTypeMaster> getSpecificationUtil() {
        return specificationUtil;
    }
}
