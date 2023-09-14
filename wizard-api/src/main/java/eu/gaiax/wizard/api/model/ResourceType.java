package eu.gaiax.wizard.api.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public enum ResourceType {
    PHYSICAL_RESOURCE("gx:PhysicalResource", "Physical"),
    VIRTUAL_DATA_RESOURCE("gx:VirtualDataResource", "Virtual (Data)"),
    VIRTUAL_SOFTWARE_RESOURCE("gx:VirtualSoftwareResource", "Virtual (Software)");

    private final String value;
    private final String label;
    private static final Map<String, ResourceType> resourceTypeMap = new HashMap<>();

    static {
        for (ResourceType resourceType : ResourceType.values()) {
            resourceTypeMap.put(resourceType.getValue(), resourceType);
        }
    }

    public static ResourceType getByValue(String value) {
        return resourceTypeMap.getOrDefault(value, null);
    }

    public static Set<String> getValueSet() {
        return resourceTypeMap.keySet();
    }
}
