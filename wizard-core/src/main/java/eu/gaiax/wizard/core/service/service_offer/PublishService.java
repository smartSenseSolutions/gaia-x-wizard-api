package eu.gaiax.wizard.core.service.service_offer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.gaiax.wizard.api.client.MessagingQueueClient;
import eu.gaiax.wizard.api.model.PublishToQueueRequest;
import eu.gaiax.wizard.dao.repository.service_offer.ServiceOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublishService {

    private final ObjectMapper objectMapper;
    private final MessagingQueueClient messagingQueueClient;
    private final ServiceOfferRepository serviceOfferRepository;
    @Value("${wizard.host.wizard}")
    private String wizardHost;

    public void publishServiceComplianceToMessagingQueue(UUID serviceOfferId, String complianceCredential) throws JsonProcessingException {
        PublishToQueueRequest publishToQueueRequest = new PublishToQueueRequest();
        publishToQueueRequest.setSource(this.wizardHost);
        publishToQueueRequest.setData((Map<String, Object>) this.objectMapper.readValue(complianceCredential, Map.class).get("complianceCredential"));

        try {
            ResponseEntity<Object> publishServiceComplianceResponse = this.messagingQueueClient.publishServiceCompliance(publishToQueueRequest);
            if (publishServiceComplianceResponse.getStatusCode().equals(HttpStatus.CREATED)) {
                if (publishServiceComplianceResponse.getHeaders().containsKey("location")) {
                    String rawMessageId = publishServiceComplianceResponse.getHeaders().get("location").get(0);
                    String messageReferenceId = rawMessageId.substring(rawMessageId.lastIndexOf("/") + 1);

                    this.serviceOfferRepository.updateMessageReferenceId(serviceOfferId, messageReferenceId);
                    log.info("Service offer published to messaging queue. Message Reference ID: {}", messageReferenceId);
                } else {
                    log.info("Location header not found for service offer ID: {}", serviceOfferId);
                }
            } else {
                log.info("Error while publishing service offer with ID {} to messaging queue. Response status: {}", serviceOfferId, publishServiceComplianceResponse.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error encountered while publishing service to message queue", e);
        }
    }
}
