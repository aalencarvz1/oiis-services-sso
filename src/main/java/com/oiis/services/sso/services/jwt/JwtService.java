package com.oiis.services.sso.services.jwt;

import com.oiis.libs.java.spring.commons.DefaultDataSwap;
import com.oiis.services.sso.database.entities.oiis.User;
import com.oiis.services.sso.properties.jwt.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);


    private final Key key;
    private final JwtProperties jwtProperties;
    private final JwtParser jwtParsaer;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
        this.jwtParsaer = Jwts.parser().setSigningKey(key).build();
    }

    public String createToken(User user) {
        String result = null;
        if (user != null) {
            result = Jwts.builder()
                    .signWith(key, SignatureAlgorithm.HS256) // usa a mesma key, mas novo builder
                    .setSubject(String.valueOf(user.getId()))
                    .claim("user_id", user.getId())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 dia
                    .compact();
        }
        return result;
    }

    public DefaultDataSwap checkToken(String token){
        DefaultDataSwap result = new DefaultDataSwap();
        try {
            logger.debug("checking token {}",token);
            if (StringUtils.hasText(token)) {
                Claims claims = jwtParsaer.parseClaimsJws(token).getBody();
                String userId = String.valueOf(claims.get("user_id"));
                if (StringUtils.hasText(userId)) {
                    result.data = userId;
                    result.success = true;
                } else {
                    result.message = "invalid token";
                }
            } else {
                result.httpStatus = HttpStatus.UNAUTHORIZED;
                result.message = "missing data";
            }
        } catch (ExpiredJwtException e) {
            result.httpStatus = HttpStatus.UNAUTHORIZED;
            result.message = "token expired";
            result.setException(e);
        } catch (SignatureException e) {
            result.httpStatus = HttpStatus.UNAUTHORIZED;
            result.message = "invalid signature";
            result.setException(e);
        } catch (MalformedJwtException e) {
            result.httpStatus = HttpStatus.BAD_REQUEST;
            result.message = "malformed token";
            result.setException(e);
        } catch (Exception e) {
            result.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            result.message = "unexpected error";
            result.setException(e);
        }
        return result;
    }
}
