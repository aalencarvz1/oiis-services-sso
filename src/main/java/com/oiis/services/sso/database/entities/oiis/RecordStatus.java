package com.oiis.services.sso.database.entities.oiis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "record_status")
@Getter
@Setter
public class RecordStatus extends BaseOiisTableModel {

    @Column(name = "name", nullable = false, length = 127)
    private String name;

    @Column(name = "is_active", nullable = false)
    private Integer isActive = 1;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public void setIsActive(Integer isActive) {
        this.isActive = isActive != null ? isActive : 1;
    }

    // Constantes equivalentes
    public static final long ACTIVE = 1;
    public static final long INACTIVE = 2;
}