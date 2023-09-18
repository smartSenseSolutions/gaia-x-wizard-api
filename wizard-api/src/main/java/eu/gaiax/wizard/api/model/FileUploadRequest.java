package eu.gaiax.wizard.api.model;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record FileUploadRequest(@NotNull(message = "File can not be blank") MultipartFile file) {
}
