package eu.gaiax.wizard.core.service.hashing;

import eu.gaiax.wizard.core.service.InvokeService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class HashingService {

    public static final String SHA_256 = "SHA-256";

    public static String generateSha256Hash(String content) {
        return generateHash(SHA_256, content);
    }

    public static String encodeToBase64(String content) {
        log.debug("HashingService(encodeToBase64) -> Encode the provided content.");
        return Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
    }

    @SneakyThrows
    private static String generateHash(String algorithm, String content) {
        log.debug("HashingService(generateHash) -> Prepare hash of content with algorithm {}", algorithm);
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
        return Hex.encodeHexString(hash);
    }

    public static String fetchJsonContent(String url) {
        return InvokeService.executeRequest(url, HttpMethod.GET);
    }

}
