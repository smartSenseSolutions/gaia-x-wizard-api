package eu.gaiax.wizard.core.service.service_offer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsensesolutions.java.commons.FilterRequest;
import com.smartsensesolutions.java.commons.base.repository.BaseRepository;
import com.smartsensesolutions.java.commons.base.service.BaseService;
import com.smartsensesolutions.java.commons.filter.FilterCriteria;
import com.smartsensesolutions.java.commons.operator.Operator;
import com.smartsensesolutions.java.commons.specification.SpecificationUtil;
import eu.gaiax.wizard.api.model.PageResponse;
import eu.gaiax.wizard.api.utils.StringPool;
import eu.gaiax.wizard.dao.entity.service_offer.ServiceOfferView;
import eu.gaiax.wizard.dao.repository.service_offer.ServiceOfferViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceOfferViewService extends BaseService<ServiceOfferView, UUID> {

    private final ServiceOfferViewRepository serviceOfferViewRepository;
    private final SpecificationUtil<ServiceOfferView> specificationUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected BaseRepository<ServiceOfferView, UUID> getRepository() {
        return this.serviceOfferViewRepository;
    }

    @Override
    protected SpecificationUtil<ServiceOfferView> getSpecificationUtil() {
        return this.specificationUtil;
    }

    public PageResponse<ServiceOfferView> filterServiceOfferView(FilterRequest filterRequest, String participantId) {

        if (StringUtils.hasText(participantId)) {
            FilterCriteria participantCriteria = new FilterCriteria(StringPool.PARTICIPANT_ID, Operator.CONTAIN, Collections.singletonList(participantId));
            List<FilterCriteria> filterCriteriaList = filterRequest.getCriteria() != null ? filterRequest.getCriteria() : new ArrayList<>();
            filterCriteriaList.add(participantCriteria);
            filterRequest.setCriteria(filterCriteriaList);
        }

        Page<ServiceOfferView> serviceOfferViewPage = this.filter(filterRequest);

        return PageResponse.of(serviceOfferViewPage, filterRequest.getSort());
    }

}
