package eu.gaiax.wizard.core.service.hashing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HashingServiceUnitTest {
    private final String uuid = "aa7bfbf6-3f37-4394-a95e-1b01fc5e59cb";
    
    @Test
    void testGenerateSha256Hash() {
        String hashedString = HashingService.generateSha256Hash(this.uuid);
        assertThat(hashedString).isEqualTo("0f0d4b41599f9ee5f1bce597f6c03a94d71106bbeab6e06f8d97c77a01ed7aab"); // pragma: allowlist secret
    }
    
    @Test
    void testEncodeToBase64() {
        String encodedString = HashingService.encodeToBase64(this.uuid);
        assertThat(encodedString).isEqualTo("YWE3YmZiZjYtM2YzNy00Mzk0LWE5NWUtMWIwMWZjNWU1OWNi");
    }
}