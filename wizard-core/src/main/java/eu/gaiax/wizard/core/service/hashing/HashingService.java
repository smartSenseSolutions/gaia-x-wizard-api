package eu.gaiax.wizard.core.service.hashing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.stream.IntStream;

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

    @SneakyThrows
    private static String generateHash(String algorithm, String tncContent) {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        return prepareHash(tncContent, digest);
    }

    @NotNull
    private static String prepareHash(String tncContent, MessageDigest digest) {
        byte[] hash = digest.digest(tncContent.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        IntStream.range(0, hash.length).mapToObj(i -> Integer.toHexString(0xff & hash[i])).forEach(hex -> {
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        });
        return hexString.toString();
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
