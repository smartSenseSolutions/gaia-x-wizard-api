/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.dao.entity;

import jakarta.persistence.*;
import lombok.*;


/**
 * The type Enterprise credential.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnterpriseCredential extends SuperEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "serial")
    private Long id;

    @Column(nullable = false)
    private Long enterpriseId;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String credentials;
}
