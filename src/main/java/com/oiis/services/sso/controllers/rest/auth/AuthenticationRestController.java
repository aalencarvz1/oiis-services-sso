package com.oiis.services.sso.controllers.rest.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.oiis.libs.java.spring.commons.DefaultDataSwap;
import com.oiis.services.sso.database.entities.oiis.User;
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
    public ResponseEntity<DefaultDataSwap> login(@RequestBody User reqUser) {
        logger.debug("requested login {}",reqUser.getEmail());
        return authenticationService.login(reqUser).sendHttpResponse();
    }

    @PostMapping("/check_token")
    public ResponseEntity<DefaultDataSwap> checkToken(@RequestBody JsonNode jsonBody) {
        if (jsonBody != null && jsonBody.has("token")) {
            return authenticationService.checkToken(String.valueOf(jsonBody.get("token")).replaceAll("\"", "")).sendHttpResponse();
        } else {
            return (new DefaultDataSwap(false,"missing token")).sendHttpResponse();
        }
    }
}
