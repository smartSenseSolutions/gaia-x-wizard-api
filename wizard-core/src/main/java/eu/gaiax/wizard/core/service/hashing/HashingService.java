package eu.gaiax.wizard.core.service.hashing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HashingService {

    public static final String SHA_256 = "SHA-256";
    public static final String SHA_512 = "SHA-512";

    public static String generateSha256Hash(String content) {
        return generateHash(SHA_256, content);
    }

    public static String generateSha512Hash(String content) {
        return generateHash(SHA_512, content);
    }

    public static String encodeToBase64(String content) {
        return Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodeToBase64(String content) {
        return new String(Base64.getDecoder().decode(content.getBytes(StandardCharsets.UTF_8)));
    }

    @SneakyThrows
    private static String generateHash(String algorithm, String tncContent) {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] hash = digest.digest(tncContent.getBytes(StandardCharsets.UTF_8));
        return Hex.encodeHexString(hash);
    }


    public static String fetchJsonContent(String url) throws IOException {
        URL jsonUrl = new URL(url);
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(jsonUrl.openStream(), StandardCharsets.UTF_8))) {
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                content.append(inputLine);
            }
        }
        return content.toString();
    }

}
