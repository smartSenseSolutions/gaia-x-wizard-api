package eu.gaiax.wizard.core.service.participant;

import eu.gaiax.wizard.vault.Vault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class VaultServiceUnitTest {
    
    @Mock
    private Vault vault;
    private VaultService vaultService;
    private final String randomUUID = UUID.randomUUID().toString();
    
    @BeforeEach
    void setUp() {
        this.vaultService = new VaultService(this.vault);
    }
    
    @AfterEach
    void tearDown() {
        this.vaultService = null;
    }
    
    @Test
    void testUploadCertificatesToVault() {
        doReturn(new HashMap<String, Object>()).when(this.vault).get(anyString());
        assertDoesNotThrow(() -> this.vaultService.uploadCertificatesToVault(this.randomUUID, this.randomUUID, this.randomUUID, this.randomUUID, this.randomUUID));
    }
    
    @Test
    void testGetParticipantPrivateKeySecret_notNull() {
        doReturn(Map.of("pkcs8.key", this.randomUUID)).when(this.vault).get(anyString());
        String participantPrivateKeySecret = this.vaultService.getParticipantPrivateKeySecret(this.randomUUID);
        assertThat(participantPrivateKeySecret).isEqualTo(this.randomUUID);
    }
    
    @Test
    void testGetParticipantPrivateKeySecret_null() {
        doReturn(null).when(this.vault).get(anyString());
        String participantPrivateKeySecret = this.vaultService.getParticipantPrivateKeySecret(this.randomUUID);
        assertThat(participantPrivateKeySecret).isNull();
    }
    
    @Test
    void testGetParticipantSecretData_notNull() {
        final String secretKey = "x509CertificateChain.pem"; // pragma: allowlist secret
        doReturn(Map.of(secretKey, this.randomUUID)).when(this.vault).get(anyString());
        Map<String, Object> participantSecretDataMap = this.vaultService.getParticipantSecretData(this.randomUUID);
        assertThat(participantSecretDataMap).containsEntry(secretKey, this.randomUUID);
    }
    
    @Test
    void testGetParticipantSecretData_null() {
        doReturn(null).when(this.vault).get(anyString());
        Map<String, Object> participantSecretDataMap = this.vaultService.getParticipantSecretData(this.randomUUID);
        assertThat(participantSecretDataMap).isNull();
    }
    
    
}