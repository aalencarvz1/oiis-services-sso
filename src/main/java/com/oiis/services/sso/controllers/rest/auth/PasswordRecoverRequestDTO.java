package com.oiis.services.sso.controllers.rest.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordRecoverRequestDTO {
    private String email;
    private String passwordChangeInterfacePath;
}