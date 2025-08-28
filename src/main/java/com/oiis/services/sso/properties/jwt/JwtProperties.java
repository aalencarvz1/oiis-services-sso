package com.oiis.services.sso.properties.jwt;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class JwtProperties {

    private static final Logger logger = LoggerFactory.getLogger(JwtProperties.class);

    private final String secretKey;
    private final Long defaultTokenExpiration;
    private final Long defaultRefreshTokenExpiration;

    public JwtProperties(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.default-token-expiration}") Long defaultTokenExpiration,
            @Value("${jwt.default-refresh-token-expiration}") Long defaultRefreshTokenExpiration
    ) {
        this.secretKey = secretKey;
        this.defaultTokenExpiration = defaultTokenExpiration;
        this.defaultRefreshTokenExpiration = defaultRefreshTokenExpiration;
    }

}