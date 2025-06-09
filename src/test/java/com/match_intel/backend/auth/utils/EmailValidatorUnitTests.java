package com.match_intel.backend.auth.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmailValidatorUnitTests {

    private final EmailValidator emailValidator = new EmailValidator();


    @Test
    @DisplayName("Valid email")
    void shouldReturnTrueForValidEmail() {
        assertTrue(emailValidator.validate("test@example.com"));
        assertTrue(emailValidator.validate("user.name@domain.co"));
        assertTrue(emailValidator.validate("user_name@domain.co.uk"));
    }

    @Test
    @DisplayName("Invalid email")
    void shouldReturnFalseForInvalidEmail() {
        assertFalse(emailValidator.validate("plainaddress"));
        assertFalse(emailValidator.validate("missing@domain"));
        assertFalse(emailValidator.validate("@missingusername.com"));
        assertFalse(emailValidator.validate("user@.com"));
        assertFalse(emailValidator.validate("user@domain..com"));
    }

    @Test
    @DisplayName("Special characters in email")
    void shouldReturnFalseForEmailWithSpecialCharacters() {
        assertFalse(emailValidator.validate("user@domain@domain.com"));
        assertFalse(emailValidator.validate("user@domain!com"));
    }

    @Test
    @DisplayName("Email is empty/blank/null")
    void shouldReturnFalseForEmptyOrNullEmail() {
        assertFalse(emailValidator.validate(""));
        assertFalse(emailValidator.validate(" "));
        assertFalse(emailValidator.validate(null));
    }
}
