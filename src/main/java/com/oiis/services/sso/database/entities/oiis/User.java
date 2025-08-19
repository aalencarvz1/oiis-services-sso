package com.oiis.services.sso.database.entities.oiis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseOiisTableModel{

    @Column(name = "email", nullable = false, length = 512, unique = true)
    private String email;

    @Column(name = "password", nullable = false, length = 1000)
    private String password;


}
