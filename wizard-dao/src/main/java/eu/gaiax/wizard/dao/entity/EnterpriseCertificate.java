/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.dao.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * The type Enterprise certificate.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnterpriseCertificate extends SuperEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "serial")
    private Long id;

    @Column(nullable = false, unique = true)
    private Long enterpriseId;

    @Column(nullable = false, unique = true)
    private String privateKey;

    @Column(nullable = false, unique = true)
    private String certificateChain;

    @Column(nullable = false, unique = true)
    private String csr;

}
