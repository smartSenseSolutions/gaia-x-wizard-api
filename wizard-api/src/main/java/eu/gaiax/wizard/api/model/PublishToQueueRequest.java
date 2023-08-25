package eu.gaiax.wizard.api.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

@Getter
@Setter
public class PublishToQueueRequest {

    private String specversion = "1.0";

    private String type = "eu.gaia-x.credential";

    private String source = "/mycontext";

    private String datacontenttype = "application/json";

    private Date time = new Date();

    private Map<String, Object> data;
}
