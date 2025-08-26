package com.oiis.services.sso.controllers.rest.auth;

import com.oiis.libs.java.spring.commons.DefaultDataSwap;
import com.oiis.services.sso.services.auth.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationRestController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationRestController.class);

    @Autowired
    AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<DefaultDataSwap> login(@RequestBody(required = false) UserRequestDTO userDto) {
        logger.debug("requested login {}",userDto.getEmail());
        return authenticationService.login(userDto).sendHttpResponse();
    }

    @PostMapping("/register")
    public ResponseEntity<DefaultDataSwap> register(@RequestBody(required = false) UserRequestDTO userDto) {
        logger.debug("requested register {}",userDto.getEmail());
        return authenticationService.register(userDto).sendHttpResponse();
    }

    @PostMapping("/check_token")
    public ResponseEntity<DefaultDataSwap> checkToken(@RequestBody(required = false) TokenRequestDTO tokenDto) {
        logger.debug("requested check_token {}",tokenDto.getToken());
        return authenticationService.checkTokenFromDto(tokenDto).sendHttpResponse();
    }

    @PostMapping("/refresh_token")
    public ResponseEntity<DefaultDataSwap> refreshToken(@RequestBody(required = false) RefreshTokenRequestDTO refreshTokenDto) {
        logger.debug("requested refresh_token {}",refreshTokenDto.getRefreshToken());
        return authenticationService.refreshTokenFromDto(refreshTokenDto).sendHttpResponse();
    }

    @PostMapping("/send_email_recover_password")
    public ResponseEntity<DefaultDataSwap> sendEmailRecoverPassword(@RequestBody(required = false) PasswordRecoverRequestDTO passwordRecoverRequestDTO) {
        logger.debug("requested send_email_recover_password {} {}",passwordRecoverRequestDTO.getEmail(), passwordRecoverRequestDTO.getPasswordChangeInterfacePath());
        return authenticationService.sendEmailRecoverPasswordFromDto(passwordRecoverRequestDTO).sendHttpResponse();
    }

    @PostMapping("/password_change")
    public ResponseEntity<DefaultDataSwap> passwordChange(@RequestBody(required = false) PasswordChangeRequestDTO passwordChangeRequestDTO) {
        logger.debug("requested password_change {}",passwordChangeRequestDTO.getToken());
        return authenticationService.passwordChangeFromDto(passwordChangeRequestDTO).sendHttpResponse();
    }
}
