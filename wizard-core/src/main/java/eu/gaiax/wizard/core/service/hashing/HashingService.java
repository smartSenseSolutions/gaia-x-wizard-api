package eu.gaiax.wizard.core.service.hashing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HashingService {

    public static String generateSha256Hash(String content) {
        //https://registry.gaia-x.eu/v1/api/termsAndConditions
        return content;
    }
}
