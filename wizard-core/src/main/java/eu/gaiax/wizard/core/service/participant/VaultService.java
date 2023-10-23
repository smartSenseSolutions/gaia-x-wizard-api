package eu.gaiax.wizard.core.service.participant;

import eu.gaiax.wizard.vault.Vault;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaultService {

    private final Vault vault;

    public void uploadCertificatesToVault(String participantId, String domainChain, String csr, String key, String pkcs8Key) {
        Map<String, Object> data = new HashMap<>();
        if (StringUtils.hasText(domainChain)) {
            data.put("x509CertificateChain.pem", domainChain);
        }
        if (StringUtils.hasText(csr)) {
            data.put(participantId + ".csr", csr);
        }
        if (StringUtils.hasText(key)) {
            data.put(participantId + ".key", key);
        }
        if (StringUtils.hasText(pkcs8Key)) {
            data.put("pkcs8.key", pkcs8Key);
        }
        if (Objects.isNull(this.vault.get(participantId))) {
            this.vault.put(participantId, data);
        } else {
            this.vault.patch(participantId, data);
        }
        log.info("CertificateService(uploadCertificatesToVault) -> Certificate has been uploaded on vault.");
    }

    public String getParticipantPrivateKeySecret(String participantId) {
        if (this.vault.get(participantId) != null && this.vault.get(participantId).containsKey("pkcs8.key")) {
            return this.vault.get(participantId).get("pkcs8.key").toString();
        }
        return null;
    }

    public Map<String, Object> getParticipantSecretData(String participantId) {
        if (this.vault.get(participantId) != null) {
            return this.vault.get(participantId);
        }
        return null;
    }
}
