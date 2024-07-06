package com.nullptr.files.validation;

import java.util.regex.Pattern;

public class InputValidator {

    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(['\";])+|(--)+|((?i)\\b(SELECT|INSERT|UPDATE|DELETE|DROP|TRUNCATE|EXEC|EXECUTE|DECLARE|UNION|FETCH|ALTER|CREATE|RENAME|DESCRIBE)\\b)"
    );

    public static boolean isValid(String input) {
        if (input == null || input.isEmpty()) {
            return true;
        }
        return !SQL_INJECTION_PATTERN.matcher(input).find();
    }
}
