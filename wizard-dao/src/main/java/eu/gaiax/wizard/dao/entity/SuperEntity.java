/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

/**
 * The type Super entity.
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SuperEntity {

    @JsonIgnore
    @CreationTimestamp
    @Column(name = "created_at")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date createdAt;

    @JsonIgnore
    @UpdateTimestamp
    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;
}
