package eu.gaiax.wizard.core.service.data_master;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import com.smartsensesolutions.java.commons.base.service.BaseService;
import com.smartsensesolutions.java.commons.specification.SpecificationUtil;
import eu.gaiax.wizard.dao.entity.data_master.StandardTypeMaster;
import eu.gaiax.wizard.dao.repository.data_master.StandardTypeMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service(value = "standardTypeMasterService")
@RequiredArgsConstructor
public class StandardTypeMasterService extends BaseService<StandardTypeMaster, String> {

    private final SpecificationUtil<StandardTypeMaster> specificationUtil;

    private final StandardTypeMasterRepository standardTypeMasterRepository;

    @Override
    protected BaseRepository<StandardTypeMaster, String> getRepository() {
        return this.standardTypeMasterRepository;
    }

    @Override
    protected SpecificationUtil<StandardTypeMaster> getSpecificationUtil() {
        return this.specificationUtil;
    }
}
