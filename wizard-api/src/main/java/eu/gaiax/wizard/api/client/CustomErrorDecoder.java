package eu.gaiax.wizard.api.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.exception.ConflictException;
import eu.gaiax.wizard.api.exception.RemoteServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

import static eu.gaiax.wizard.api.utils.StringPool.ERROR;

@Component
@Slf4j
public class CustomErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        JsonNode responseBody;
        String errorMessage;
        try (InputStream bodyIs = response.body().asInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            responseBody = mapper.readTree(bodyIs);
            errorMessage = responseBody.get(ERROR).asText();

            log.error("Exception received from Signer Tool. Status: {}, error message: {}", response.status(), errorMessage);
        } catch (IOException e) {
            return new Exception(e.getMessage());
        }

        return switch (response.status()) {
            case 400 -> new BadDataException(errorMessage);
            case 409 -> new ConflictException(errorMessage);
            default -> new RemoteServiceException(errorMessage);
        };
    }
}
