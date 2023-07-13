/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * The type Login request.
 */
@Getter
@Setter
public class LoginRequest {


    @Min(value = 1, message = "${invalid.type}")
    @Max(value = 2, message = "${invalid.type}")
    private int type;

    @NotBlank(message = "${provide.email}")
    @Email(message = "${invalid.email}")
    private String email;

    @NotBlank(message = "${provide.password}")
    private String password;

}
