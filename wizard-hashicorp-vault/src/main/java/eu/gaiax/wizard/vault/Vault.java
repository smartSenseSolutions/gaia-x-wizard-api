package eu.gaiax.wizard.vault;

import java.util.Map;

public interface Vault {

    Map<String, Object> put(String secretName, Map<String, Object> kv);

    Map<String, Object> get(String secretName);

    boolean patch(String path, Map<String, Object> patch);

}
