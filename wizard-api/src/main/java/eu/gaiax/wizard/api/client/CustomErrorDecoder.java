package eu.gaiax.wizard.api.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.gaiax.wizard.api.exception.BadDataException;
import eu.gaiax.wizard.api.exception.ConflictException;
import eu.gaiax.wizard.api.exception.RemoteServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class CustomErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        JsonNode responseBody;
        try (InputStream bodyIs = response.body().asInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            responseBody = mapper.readTree(bodyIs);
            System.out.println(responseBody);
        } catch (IOException e) {
            return new Exception(e.getMessage());
        }

        return switch (response.status()) {
            case 400 -> new BadDataException(responseBody.get("error").asText());
            case 409 -> new ConflictException(responseBody.get("error").asText());
            default -> new RemoteServiceException(responseBody.get("error").asText());
        };
    }
}
