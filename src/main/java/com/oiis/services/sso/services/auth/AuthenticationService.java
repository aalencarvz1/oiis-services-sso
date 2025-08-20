package com.oiis.services.sso.services.auth;

import com.oiis.libs.java.spring.commons.DefaultDataSwap;
import com.oiis.services.sso.controllers.rest.auth.PasswordChangeRequestDTO;
import com.oiis.services.sso.controllers.rest.auth.PasswordRecoverRequestDTO;
import com.oiis.services.sso.controllers.rest.auth.TokenRequestDTO;
import com.oiis.services.sso.database.entities.oiis.RecordStatus;
import com.oiis.services.sso.database.entities.oiis.User;
import com.oiis.services.sso.database.repositories.oiis.UsersRepository;
import com.oiis.services.sso.services.jwt.JwtService;
import com.oiis.services.sso.services.mail.MailService;
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

    @Autowired
    MailService mailService;

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
                result.httpStatus = HttpStatus.EXPECTATION_FAILED;
                result.message = "missing data";
            }
        } catch (Exception e) {
            result.setException(e);
        }
        return result;
    }

    public DefaultDataSwap register(User reqUser){
        DefaultDataSwap result = new DefaultDataSwap();
        try {
            if (StringUtils.hasText(reqUser.getEmail()) && StringUtils.hasText(reqUser.getPassword())) {
                Optional<User> user = usersRepository.findByEmail(reqUser.getEmail());
                if (user.isEmpty()) {
                    reqUser.setPassword(encoder.encode(reqUser.getPassword()));
                    Optional.ofNullable(usersRepository.save(reqUser));
                    usersRepository.save(reqUser);
                    result = getAuthDataResult(usersRepository.findByEmail(reqUser.getEmail()), null);
                } else {
                    result.httpStatus = HttpStatus.CONFLICT;
                    result.message = "user already exists";
                }
            } else {
                result.httpStatus = HttpStatus.EXPECTATION_FAILED;
                result.message = "missing data";
            }
        } catch (Exception e) {
            result.setException(e);
        }
        return result;
    }


    public DefaultDataSwap checkTokenFromDto(TokenRequestDTO tokenRequest) {
        return checkToken(tokenRequest.getToken());
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
                result.httpStatus = HttpStatus.EXPECTATION_FAILED;
                result.message = "missing data";
            }
        } catch (Exception e) {
            result.setException(e);
        }
        return result;
    }

    public DefaultDataSwap sendEmailRecoverPasswordFromDto(PasswordRecoverRequestDTO passwordRecoverRequestDTO){
        DefaultDataSwap result = new DefaultDataSwap();
        try {
            if (StringUtils.hasText(passwordRecoverRequestDTO.getEmail())) {
                Optional<User> user = usersRepository.findByEmail(passwordRecoverRequestDTO.getEmail());
                if (user.isPresent()) {
                    String token = jwtService.createToken(user.get());
                    String subject = "Recuperação de senha";
                    String text = "Acesse este link para criar uma nova senha: " + passwordRecoverRequestDTO.getPasswordChangeInterfacePath() + "/" + token;
                    String html = "Acesse este link para criar uma nova senha: <br /><a href=\"" + passwordRecoverRequestDTO.getPasswordChangeInterfacePath() + "/" + token + "\">Alterar senha</a>";

                    mailService.sendEmail(passwordRecoverRequestDTO.getEmail(), subject, text, html);

                    result.success = true; //sendMail throws exception if error
                } else {
                    result.httpStatus = HttpStatus.EXPECTATION_FAILED;
                    result.message = "user not found";
                }
            } else {
                result.httpStatus = HttpStatus.EXPECTATION_FAILED;
                result.message = "missing data";
            }
        } catch (Exception e) {
            result.setException(e);
        }
        return result;
    }

    public DefaultDataSwap passwordChangeFromDto(PasswordChangeRequestDTO passwordChangeRequestDTO){
        DefaultDataSwap result = new DefaultDataSwap();
        try {
            if (StringUtils.hasText(passwordChangeRequestDTO.getToken()) && StringUtils.hasText(passwordChangeRequestDTO.getPassword())) {

                result = checkToken(passwordChangeRequestDTO.getToken());
                if (result.success) {
                    result.success = false;
                    Map<String,Object> dataObject = (Map<String, Object>) result.data;
                    Optional<User> user = usersRepository.findById(Long.valueOf(String.valueOf(dataObject.get("user_id"))));
                    if (user.isPresent()) {
                        user.get().setPassword(encoder.encode(passwordChangeRequestDTO.getPassword()));
                        usersRepository.save(user.get());
                        result.success = true; //sendMail throws exception if error
                    } else {
                        result.httpStatus = HttpStatus.EXPECTATION_FAILED;
                        result.message = "user not found";
                    }
                }
            } else {
                result.httpStatus = HttpStatus.EXPECTATION_FAILED;
                result.message = "missing data";
            }
        } catch (Exception e) {
            result.setException(e);
        }
        return result;
    }

}
