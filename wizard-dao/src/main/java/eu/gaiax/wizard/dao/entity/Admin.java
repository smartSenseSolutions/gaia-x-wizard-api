/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.dao.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * The type Admin.
 */
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "serial")
    private Long id;

    private String userName;

    private String password;
}
