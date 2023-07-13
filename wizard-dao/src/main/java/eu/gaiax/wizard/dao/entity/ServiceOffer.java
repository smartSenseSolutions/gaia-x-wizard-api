/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Map;

/**
 * The type Service offer.
 */
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceOffer extends SuperEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "serial")
    private Long id;

    private Long enterpriseId;

    private Long credentialId;

    private String subjectDid;

    private String name;

    private String label;

    private String producedBy;

    private String copyrightOwnedBy;

    private String description;

    private String policy;

    private String accessType;

    private String requestType;

    private String formatType;

    private String terms;

    private String termsHash;

    @JsonIgnore
    @Convert(converter = StringToMapConvertor.class)
    private Map<String, Object> meta;
}
