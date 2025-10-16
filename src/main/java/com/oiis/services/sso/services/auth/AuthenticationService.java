package com.oiis.services.sso.services.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oiis.libs.java.spring.commons.DefaultDataSwap;
import com.oiis.services.sso.controllers.rest.auth.PasswordRecoverRequestDTO;
import com.oiis.services.sso.controllers.rest.auth.RefreshTokenRequestDTO;
import com.oiis.services.sso.controllers.rest.auth.TokenRequestDTO;
import com.oiis.services.sso.controllers.rest.auth.UserRequestDTO;
import com.oiis.services.sso.controllers.rest.auth.PasswordChangeRequestDTO;
import com.oiis.services.sso.database.entities.sso.RecordStatus;
import com.oiis.services.sso.database.entities.sso.User;
import com.oiis.services.sso.database.repositories.sso.UsersRepository;
import com.oiis.services.sso.services.jwt.JwtService;
import com.oiis.services.sso.services.mail.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    @Autowired
    JwtService jwtService;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    MailService mailService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${security.password-rules.min-length}")
    private Integer minPassordLength;

    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();

    private DefaultDataSwap getAuthDataResult(
            Optional<User> user,
            Boolean checkPassword, String password,
            String token,
            Boolean returnRefreshToken,
            String refreshToken
    ) throws JsonProcessingException {
        DefaultDataSwap result = new DefaultDataSwap();
        if (user.isPresent()) {
            if (checkPassword) {
                logger.debug("checking password {} {} {} {}",password,encoder.encode(password), user.get().getPassword(), encoder.matches(password, user.get().getPassword()));
            }
            if (!checkPassword || (checkPassword && encoder.matches(password, user.get().getPassword()))) {
                if (user.get().getDeletedAt() == null && Objects.equals(RecordStatus.ACTIVE,user.get().getStatusRegId())) {

                    Map<String,Object> dataObject = new HashMap<>();
                    if (!StringUtils.hasText(token)) {
                        user.get().setLastToken(jwtService.createToken(user.get()));
                        dataObject.put("token", user.get().getLastToken());
                    } else {
                        dataObject.put("token", token);
                    }
                    if (returnRefreshToken) {
                        user.get().setLastRefreshToken(jwtService.createRefreshToken(user.get()));
                        dataObject.put("refreshToken", user.get().getLastRefreshToken());
                    }
                    if (!StringUtils.hasText(token) || returnRefreshToken) {
                        usersRepository.save(user.get());
                    }
                    user.get().setPassword(null);
                    dataObject.put("user", objectMapper.convertValue(user, Map.class));
                    result.data = dataObject;
                    result.httpStatusCode = HttpStatus.OK.value();
                    result.success = true;
                } else {
                    result.httpStatusCode = HttpStatus.UNAUTHORIZED.value();
                    result.message = "user is not active";
                }
            } else {
                result.httpStatusCode = HttpStatus.UNAUTHORIZED.value();
                result.message = "password not match";
            }
        } else {
            result.httpStatusCode = HttpStatus.UNAUTHORIZED.value();
            result.message = "user not found";
        }
        logger.debug("getAuthDataResult return is {}",objectMapper.convertValue(result, Map.class));
        return result;
    }

    private DefaultDataSwap getAuthDataResult(Optional<User> user, String token) throws JsonProcessingException {
        return getAuthDataResult(user,false,null, token, false, null);
    }

    public DefaultDataSwap login(UserRequestDTO userDto){
        DefaultDataSwap result = new DefaultDataSwap();
        try {
            if (StringUtils.hasText(userDto.getEmail()) && StringUtils.hasText(userDto.getPassword())) {
                result = getAuthDataResult(usersRepository.findByEmail(userDto.getEmail().trim().toLowerCase()),true,userDto.getPassword(), null, true, null);
            } else {
                result.httpStatusCode = HttpStatus.EXPECTATION_FAILED.value();
                result.message = "missing data";
            }
        } catch (Exception e) {
            result.setException(e);
        }
        return result;
    }

    public DefaultDataSwap passworRulesCheck(String password) {
        DefaultDataSwap result = new DefaultDataSwap();
        try {
            if (StringUtils.hasText(password)) {
                if (password.length() < minPassordLength) {
                    result.httpStatusCode = HttpStatus.EXPECTATION_FAILED.value();
                    result.message = "password length less than " + minPassordLength + " characters";
                } else {
                    result.success = true;
                }
            } else {
                result.httpStatusCode = HttpStatus.EXPECTATION_FAILED.value();
                result.message = "empty password";
            }
        } catch (Exception e) {
            result.setException(e);
        }
        return result;
    }

    public DefaultDataSwap register(UserRequestDTO userDto){
        DefaultDataSwap result = new DefaultDataSwap();
        try {
            if (StringUtils.hasText(userDto.getEmail()) && StringUtils.hasText(userDto.getPassword())) {
                Optional<User> user = usersRepository.findByEmail(userDto.getEmail().trim().toLowerCase());
                if (user.isEmpty()) {

                    result = passworRulesCheck(userDto.getPassword());
                    if (result.success) {
                        User newUser = new User();
                        newUser.setEmail(userDto.getEmail().trim().toLowerCase());
                        newUser.setPassword(encoder.encode(userDto.getPassword()));
                        usersRepository.save(newUser);
                        result = getAuthDataResult(usersRepository.findByEmail(userDto.getEmail().trim().toLowerCase()), false, null, null, true, null);
                    }
                } else {
                    result.httpStatusCode = HttpStatus.CONFLICT.value();
                    result.message = "user already exists";
                }
            } else {
                result.httpStatusCode = HttpStatus.EXPECTATION_FAILED.value();
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
                result.httpStatusCode = HttpStatus.EXPECTATION_FAILED.value();
                result.message = "missing data";
            }
        } catch (Exception e) {
            result.setException(e);
        }
        return result;
    }

    public DefaultDataSwap refreshTokenFromDto(RefreshTokenRequestDTO refreshTokenRequestDTO) {
        return refreshToken(refreshTokenRequestDTO.getRefreshToken());
    }

    public DefaultDataSwap refreshToken(String refreshToken){
        DefaultDataSwap result = new DefaultDataSwap();
        try {
            if (StringUtils.hasText(refreshToken)) {
                result = jwtService.checkToken(refreshToken);
                if (result.success) {
                    result = getAuthDataResult(usersRepository.findById(Long.valueOf(String.valueOf(result.data))),false,null, null, true, null);
                }
            } else {
                result.httpStatusCode = HttpStatus.EXPECTATION_FAILED.value();
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
                Optional<User> user = usersRepository.findByEmail(passwordRecoverRequestDTO.getEmail().trim().toLowerCase());
                if (user.isPresent()) {
                    user.get().setLastPasswordChangeToken(jwtService.createToken(user.get()));
                    usersRepository.save(user.get());
                    String subject = "Password Recover";
                    String text = "Follow this link to create a new password: " + passwordRecoverRequestDTO.getPasswordChangeInterfacePath() + "/" + user.get().getLastPasswordChangeToken();
                    String html = "Follow this link to create a new password: <br /><a href=\"" + passwordRecoverRequestDTO.getPasswordChangeInterfacePath() + "/" + user.get().getLastPasswordChangeToken() + "\">Change password</a>";

                    mailService.sendEmail(passwordRecoverRequestDTO.getEmail().trim().toLowerCase(), subject, text, html);

                    result.success = true; //sendMail throws exception if error
                } else {
                    result.httpStatusCode = HttpStatus.EXPECTATION_FAILED.value();
                    result.message = "user not found";
                }
            } else {
                result.httpStatusCode = HttpStatus.EXPECTATION_FAILED.value();
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
                    Map<String,Object> userObject = (Map<String, Object>) dataObject.getOrDefault("user",null);
                    Optional<User> user = usersRepository.findById(Long.valueOf(String.valueOf(userObject.getOrDefault("id",null))));
                    if (user.isPresent()) {
                        if (passwordChangeRequestDTO.getToken().equals(user.get().getLastPasswordChangeToken())) {

                            result = passworRulesCheck(passwordChangeRequestDTO.getPassword());
                            if (result.success) {
                                user.get().setPassword(encoder.encode(passwordChangeRequestDTO.getPassword()));
                                usersRepository.save(user.get());
                                result.success = true;
                            }
                        } else {
                            result.httpStatusCode = HttpStatus.EXPECTATION_FAILED.value();
                            result.message = "token not match";
                        }
                    } else {
                        result.httpStatusCode = HttpStatus.EXPECTATION_FAILED.value();
                        result.message = "user not found";
                    }
                }
            } else {
                result.httpStatusCode = HttpStatus.EXPECTATION_FAILED.value();
                result.message = "missing data";
            }
        } catch (Exception e) {
            result.setException(e);
        }
        return result;
    }

}
