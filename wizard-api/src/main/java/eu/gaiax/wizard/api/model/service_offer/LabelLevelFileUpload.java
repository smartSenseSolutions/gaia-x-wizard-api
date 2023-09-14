package eu.gaiax.wizard.api.model.service_offer;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record LabelLevelFileUpload(@NotNull(message = "File can not be blank") MultipartFile file,
                                   @NotNull(message = "FileType can not be blank") String fileType) {
}
