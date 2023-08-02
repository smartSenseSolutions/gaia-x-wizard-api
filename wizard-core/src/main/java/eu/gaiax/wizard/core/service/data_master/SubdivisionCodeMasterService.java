package eu.gaiax.wizard.core.service.data_master;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import com.smartsensesolutions.java.commons.base.service.BaseService;
import com.smartsensesolutions.java.commons.specification.SpecificationUtil;
import eu.gaiax.wizard.dao.entity.data_master.SubdivisionCodeMaster;
import eu.gaiax.wizard.dao.repository.data_master.SubdivisionCodeMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service(value = "subdivisionTypeMasterService")
@RequiredArgsConstructor
public class SubdivisionCodeMasterService extends BaseService<SubdivisionCodeMaster, String> {

    private final SpecificationUtil<SubdivisionCodeMaster> specificationUtil;

    private final SubdivisionCodeMasterRepository standardTypeMasterRepository;

    @Override
    protected BaseRepository<SubdivisionCodeMaster, String> getRepository() {
        return this.standardTypeMasterRepository;
    }

    @Override
    protected SpecificationUtil<SubdivisionCodeMaster> getSpecificationUtil() {
        return this.specificationUtil;
    }
}
