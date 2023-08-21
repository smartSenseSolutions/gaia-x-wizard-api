package eu.gaiax.wizard.core.service.data_master;

import com.smartsensesolutions.java.commons.base.service.BaseService;
import eu.gaiax.wizard.api.exception.BadDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MasterDataServiceFactory {

    private final Map<String, BaseService> indexServiceMap;

    public BaseService getInstance(String indexName) {
        BaseService indexService = this.indexServiceMap.get(indexName + "TypeMasterService");
        if (indexService == null) {
            throw new BadDataException("Master data not found with name '" + indexName + "'");
        }
        return indexService;
    }
}

