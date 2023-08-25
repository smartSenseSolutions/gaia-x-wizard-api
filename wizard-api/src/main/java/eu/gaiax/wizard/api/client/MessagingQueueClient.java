package eu.gaiax.wizard.api.client;

import eu.gaiax.wizard.api.model.PublishToQueueRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "MessagingQueueClient", url = "${wizard.host.messagingQueue}")
public interface MessagingQueueClient {

    @PostMapping(path = "credentials-events", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> publishServiceCompliance(@RequestBody PublishToQueueRequest publishToQueueRequest);
}
