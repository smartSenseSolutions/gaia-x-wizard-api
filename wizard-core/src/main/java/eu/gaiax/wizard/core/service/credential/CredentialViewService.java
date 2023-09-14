package eu.gaiax.wizard.core.service.credential;

import com.smartsensesolutions.java.commons.FilterRequest;
import com.smartsensesolutions.java.commons.base.service.BaseService;
import com.smartsensesolutions.java.commons.filter.FilterCriteria;
import com.smartsensesolutions.java.commons.operator.Operator;
import com.smartsensesolutions.java.commons.specification.SpecificationUtil;
import eu.gaiax.wizard.api.model.PageResponse;
import eu.gaiax.wizard.api.utils.StringPool;
import eu.gaiax.wizard.dao.entity.credential.CredentialView;
import eu.gaiax.wizard.dao.repository.credential.CredentialViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CredentialViewService extends BaseService<CredentialView, UUID> {

    private final CredentialViewRepository credentialViewRepository;
    private final SpecificationUtil<CredentialView> specificationUtil;

    @Override
    protected CredentialViewRepository getRepository() {
        return this.credentialViewRepository;
    }

    @Override
    protected SpecificationUtil<CredentialView> getSpecificationUtil() {
        return this.specificationUtil;
    }

    public PageResponse<CredentialView> filterCredentialView(String participantId, FilterRequest filterRequest) {
        FilterCriteria participantCriteria = new FilterCriteria(StringPool.PARTICIPANT_ID, Operator.CONTAIN, Collections.singletonList(participantId));
        List<FilterCriteria> filterCriteriaList = filterRequest.getCriteria() != null ? filterRequest.getCriteria() : new ArrayList<>();
        filterCriteriaList.add(participantCriteria);
        filterRequest.setCriteria(filterCriteriaList);

        Page<CredentialView> credentialViewPage = this.filter(filterRequest);
        return PageResponse.of(credentialViewPage, filterRequest.getSort());
    }

}
