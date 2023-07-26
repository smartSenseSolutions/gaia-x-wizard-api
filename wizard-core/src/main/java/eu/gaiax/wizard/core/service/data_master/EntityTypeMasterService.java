package eu.gaiax.wizard.core.service.data_master;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import com.smartsensesolutions.java.commons.base.service.BaseService;
import com.smartsensesolutions.java.commons.specification.SpecificationUtil;
import eu.gaiax.wizard.dao.entity.data_master.EntityTypeMaster;
import eu.gaiax.wizard.dao.repository.data_master.EntityTypeMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EntityTypeMasterService extends BaseService<EntityTypeMaster, String> {

    private final SpecificationUtil<EntityTypeMaster> specificationUtil;

    private final EntityTypeMasterRepository entityTypeMasterRepository;

    @Override
    protected BaseRepository<EntityTypeMaster, String> getRepository() {
        return entityTypeMasterRepository;
    }

    @Override
    protected SpecificationUtil<EntityTypeMaster> getSpecificationUtil() {
        return specificationUtil;
    }
}
