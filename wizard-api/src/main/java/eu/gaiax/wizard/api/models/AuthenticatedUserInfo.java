/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * The type Authenticated user info.
 */
@Getter
@Setter
public class AuthenticatedUserInfo {

    @JsonProperty("sub")
    private String sub;
    @JsonProperty("email_verified")
    private boolean emailVerified;

    @JsonAlias({"fullName"})
    @JsonProperty("name")
    private String name;
    @JsonProperty("preferred_username")
    private String preferredUserName;

    @JsonAlias({"given_name", "firstName"})
    @JsonProperty("given_name")
    private String firstName;

    @JsonAlias({"family_name", "lastName"})
    @JsonProperty("family_name")
    private String lastName;

    @JsonProperty("email")
    @JsonAlias({"mail"})
    private String email;
}
