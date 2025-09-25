package com.oiis.services.sso.database.entities.sso;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseSsoTableModel {

    @Column(name = "email", nullable = false, length = 512, unique = true)
    private String email;

    @JsonIgnore
    @Column(name = "password", length = 1000)
    private String password;

    @Column(name = "last_token", length = 1000)
    private String lastToken;

    @Column(name = "last_refresh_token", length = 1000)
    private String lastRefreshToken;

    @Column(name = "last_password_change_token", length = 1000)
    private String lastPasswordChangeToken;

    @Column(name = "generic_access_profile_id")
    private Long genericAccessProfileId;

}
