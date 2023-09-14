package eu.gaiax.wizard.core.service.data_master;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import com.smartsensesolutions.java.commons.base.service.BaseService;
import com.smartsensesolutions.java.commons.specification.SpecificationUtil;
import eu.gaiax.wizard.api.model.policy.SubdivisionName;
import eu.gaiax.wizard.dao.entity.data_master.SubdivisionCodeMaster;
import eu.gaiax.wizard.dao.repository.data_master.SubdivisionCodeMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service(value = "subdivisionTypeMasterService")
@RequiredArgsConstructor
public class SubdivisionCodeMasterService extends BaseService<SubdivisionCodeMaster, String> {

    private final SpecificationUtil<SubdivisionCodeMaster> specificationUtil;

    private final SubdivisionCodeMasterRepository subdivisionCodeMasterRepository;

    @Override
    protected BaseRepository<SubdivisionCodeMaster, String> getRepository() {
        return this.subdivisionCodeMasterRepository;
    }

    @Override
    protected SpecificationUtil<SubdivisionCodeMaster> getSpecificationUtil() {
        return this.specificationUtil;
    }

    public List<SubdivisionName> getNameListBySubdivisionCode(String[] subDivisionCodeArray) {
        return this.subdivisionCodeMasterRepository.findAllNameBySubdivisionCodeIn(subDivisionCodeArray);
    }
}
