package com.nullptr.files.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InputValidatorTest {

    @Test
    public void testValidInputs() {
        assertTrue(InputValidator.isValid("SafeString"));
        assertTrue(InputValidator.isValid("1234567890"));
        assertTrue(InputValidator.isValid("Title with spaces"));
        assertTrue(InputValidator.isValid("A description with multiple words."));
        assertTrue(InputValidator.isValid(""));
        assertTrue(InputValidator.isValid(null));
    }

    @Test
    public void testInvalidInputs() {
        assertFalse(InputValidator.isValid("A description with multiple words.'; DROP TABLE users;"));
        assertFalse(InputValidator.isValid("123456'; SELECT * FROM users WHERE name = 'admin' --"));
        assertFalse(InputValidator.isValid("'; DELETE FROM users; --"));
        assertFalse(InputValidator.isValid("Robert'); DROP TABLE students; --"));
        assertFalse(InputValidator.isValid("INSERT INTO users (name, password) VALUES ('admin', 'password')"));
    }
}
