package com.match_intel.backend.auth.utils;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class EmailValidator {

    public boolean validate(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String EMAIL_PATTERN = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

        return Pattern.compile(EMAIL_PATTERN).matcher(email).matches();
    }
}
