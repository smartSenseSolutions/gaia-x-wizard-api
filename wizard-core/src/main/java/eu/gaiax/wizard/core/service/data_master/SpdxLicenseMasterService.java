package eu.gaiax.wizard.core.service.data_master;

import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import com.smartsensesolutions.java.commons.base.service.BaseService;
import com.smartsensesolutions.java.commons.specification.SpecificationUtil;
import eu.gaiax.wizard.dao.entity.data_master.SpdxLicenseMaster;
import eu.gaiax.wizard.dao.repository.data_master.SpdxLicenseMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service(value = "spdxLicenseTypeMasterService")
@RequiredArgsConstructor
public class SpdxLicenseMasterService extends BaseService<SpdxLicenseMaster, String> {

    private final SpecificationUtil<SpdxLicenseMaster> specificationUtil;

    private final SpdxLicenseMasterRepository spdxLicenseMasterRepository;

    @Override
    protected BaseRepository<SpdxLicenseMaster, String> getRepository() {
        return this.spdxLicenseMasterRepository;
    }

    @Override
    protected SpecificationUtil<SpdxLicenseMaster> getSpecificationUtil() {
        return this.specificationUtil;
    }

}
