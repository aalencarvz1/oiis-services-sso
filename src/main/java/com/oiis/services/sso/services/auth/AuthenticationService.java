package com.oiis.services.sso.services.auth;

import com.oiis.libs.java.spring.commons.DefaultDataSwap;
import com.oiis.services.sso.database.entities.oiis.RecordStatus;
import com.oiis.services.sso.database.entities.oiis.User;
import com.oiis.services.sso.database.repositories.oiis.UsersRepository;
import com.oiis.services.sso.services.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class AuthenticationService {

    @Autowired
    JwtService jwtService;

    @Autowired
    UsersRepository usersRepository;

    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();

    private DefaultDataSwap getAuthDataResult(Optional<User> user, String token, Boolean checkPassword, String password) {
        DefaultDataSwap result = new DefaultDataSwap();
        if (user.isPresent()) {
            if (!checkPassword || (checkPassword && encoder.matches(password, user.get().getPassword()))) {
                if (user.get().getDeletedAt() == null && Objects.equals(RecordStatus.ACTIVE,user.get().getStatusRegId())) {
                    result.data = Map.ofEntries(
                            Map.entry("user_id", user.get().getId()),
                            Map.entry("token", StringUtils.hasText(token) ? token : jwtService.createToken(user.get()))
                    );
                    result.success = true;
                } else {
                    result.httpStatus = HttpStatus.UNAUTHORIZED;
                    result.message = "user is not active";
                }
            } else {
                result.httpStatus = HttpStatus.UNAUTHORIZED;
                result.message = "password not match";
            }
        } else {
            result.httpStatus = HttpStatus.UNAUTHORIZED;
            result.message = "user not found";
        }
        return result;
    }

    private DefaultDataSwap getAuthDataResult(Optional<User> user, String token) {
        return getAuthDataResult(user,token,false,null);
    }

    public DefaultDataSwap login(User reqUser){
        DefaultDataSwap result = new DefaultDataSwap();
        try {
            if (StringUtils.hasText(reqUser.getEmail()) && StringUtils.hasText(reqUser.getPassword())) {
                result = getAuthDataResult(usersRepository.findByEmail(reqUser.getEmail()),null,true,reqUser.getPassword());
            } else {
                result.httpStatus = HttpStatus.UNAUTHORIZED;
                result.message = "missing data";
            }
        } catch (Exception e) {
            result.setException(e);
        }
        return result;
    }


    public DefaultDataSwap checkToken(String token){
        DefaultDataSwap result = new DefaultDataSwap();
        try {
            if (StringUtils.hasText(token)) {
                result = jwtService.checkToken(token);
                if (result.success) {
                    result = getAuthDataResult(usersRepository.findById(Long.valueOf(String.valueOf(result.data))),token);
                }
            } else {
                result.httpStatus = HttpStatus.UNAUTHORIZED;
                result.message = "missing data";
            }
        } catch (Exception e) {
            result.setException(e);
        }
        return result;
    }
}
