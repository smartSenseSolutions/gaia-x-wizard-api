/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.api.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * The type Session dto.
 */
@Getter
@Setter
@Builder
public class SessionDTO {

    private long enterpriseId;

    private String email;

    private int role;
}
