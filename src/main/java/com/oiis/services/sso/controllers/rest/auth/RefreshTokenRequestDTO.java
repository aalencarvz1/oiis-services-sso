package com.oiis.services.sso.controllers.rest.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequestDTO {
    private String refreshToken;
}