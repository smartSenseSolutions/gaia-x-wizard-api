package eu.gaiax.wizard.core.service;

import eu.gaiax.wizard.api.client.SignerClient;
import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.model.ParticipantVerifyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommonService {
    private static final List<String> policies = Arrays.asList(
            "integrityCheck",
            "holderSignature",
            "complianceSignature",
            "complianceCheck"
    );
    private final SignerClient signerClient;

    public String getCurrentFormattedDate() {
        Instant currentInstant = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String time = ZonedDateTime.ofInstant(currentInstant, ZoneOffset.UTC).format(formatter);
        return time;
    }

    public void validateRequestUrl(String url, String message) {
        ParticipantVerifyRequest participantValidatorRequest = new ParticipantVerifyRequest(url, policies);
        ResponseEntity<Map<String, Object>> signerResponse = this.signerClient.verify(participantValidatorRequest);
        if (!signerResponse.getStatusCode().is2xxSuccessful()) {
            throw new BadDataException(message);
        }
    }
}
