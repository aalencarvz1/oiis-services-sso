package com.oiis.services.sso.controllers.mail;

import org.springframework.stereotype.Controller;

@Controller
public class MailController {

    public boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email != null && email.matches(emailRegex);
    }

}
