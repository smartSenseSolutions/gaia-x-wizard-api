/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.models;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * The type Register request.
 *
 * @author Nitin
 * @version 1.0
 */
@Getter
@Setter
public class RegisterRequest {

    @Email
    @NotBlank
    private String email;

    @Size(min = 8, max = 16)
    private String password;


    @Size(min = 3, max = 32)
    private String legalName;

    @Size(min = 3, max = 12)
    @Pattern(regexp = "^[A-Za-z0-9]+$")
    private String subDomainName;

    @NotBlank
    private String legalRegistrationNumber;

    @NotBlank
    private String legalRegistrationType;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z]{2}-(?:[a-zA-Z]{1,3}|[0-9]{1,3})$")
    private String headquarterAddress;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z]{2}-(?:[a-zA-Z]{1,3}|[0-9]{1,3})$")
    private String legalAddress;

    @AssertTrue
    private boolean termsAndConditions;
}
