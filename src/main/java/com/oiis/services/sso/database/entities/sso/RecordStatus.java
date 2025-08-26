package com.oiis.services.sso.database.entities.sso;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "record_status")
@Getter
@Setter
public class RecordStatus extends BaseSsoTableModel {

    @Column(name = "name", nullable = false, length = 127)
    private String name;

    @Column(name = "is_active", nullable = false)
    private Integer isActive = 1;

    @Lob
    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    public void setIsActive(Integer isActive) {
        this.isActive = isActive != null ? isActive : 1;
    }

    // Constantes equivalentes
    public static final long ACTIVE = 1;
    public static final long INACTIVE = 2;
}