package eu.gaiax.wizard.implementation;

import eu.gaiax.wizard.exception.VaultException;
import eu.gaiax.wizard.model.VaultPathConfiguration;
import eu.gaiax.wizard.vault.Vault;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class HashicorpVaultService implements Vault {
    private final VaultPathConfiguration vaultPathConfiguration;
    private final VaultTemplate template;

    @Override
    public Map<String, Object> put(String secretName, Map<String, Object> kv) {
        try {
            this.template.opsForKeyValue(this.vaultPathConfiguration.secretPath(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_2).put(secretName, kv);
            return kv;
        } catch (Exception ex) {
            throw new VaultException("HashicorpVaultService(put): Issue occur while put the secrets on hashicorp vault.", ex);
        }
    }

    @Override
    public Map<String, Object> get(String secretName) {
        try {
            VaultResponse response = this.template.opsForKeyValue(this.vaultPathConfiguration.secretPath(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_2).get(secretName);
            if (Objects.isNull(response)) {
                return null;
            }
            return response.getData();
        } catch (Exception ex) {
            throw new VaultException("HashicorpVaultService(get): Issue occur while read the secrets from hashicorp vault.", ex);
        }
    }

    @Override
    public boolean patch(String secretName, Map<String, Object> kv) {
        try {
            boolean patch = this.template.opsForKeyValue(this.vaultPathConfiguration.secretPath(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_2).patch(secretName, kv);
            return patch;
        } catch (Exception ex) {
            throw new VaultException("HashicorpVaultService(patch): Issue occur while read the secrets from hashicorp vault.", ex);
        }
    }
}
