package eu.gaiax.wizard.api.model.service_offer;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record LabelLevelFileUpload(@NotNull(message = "level is required") String level,
                                   @NotNull(message = "type is required") String type,
                                   @NotNull(message = "File can not be blank") MultipartFile file
) {
}
