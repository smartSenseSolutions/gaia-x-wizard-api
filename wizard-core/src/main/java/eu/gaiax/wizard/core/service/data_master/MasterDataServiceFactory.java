package eu.gaiax.wizard.core.service.data_master;

import com.smartsensesolutions.java.commons.base.service.BaseService;
import eu.gaiax.wizard.api.exception.BadDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MasterDataServiceFactory {

    private final Map<String, BaseService> indexServiceMap;
    private final MessageSource messageSource;

    public BaseService getInstance(String indexName) {
        BaseService indexService = this.indexServiceMap.get(indexName + "TypeMasterService");
        if (indexService == null) {
            throw new BadDataException(this.messageSource.getMessage("invalid.master.data.type", new String[]{indexName}, LocaleContextHolder.getLocale()));
        }
        return indexService;
    }
}

