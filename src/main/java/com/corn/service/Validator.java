package com.corn.service;


import com.corn.exception.ValidationException;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
class Validator {

    private static final Pattern PATTERN_EMAIL = Pattern.compile("^[+\\w.-]+@[\\w-.]+[.][a-z]{2,4}$");
    private final StringBuilder errors = new StringBuilder();

    Validator notEmpty(String value, String caption) {
        if (value == null || value.isEmpty()) {
            errors.append("Please, provide ").append(caption).append(". ");
        }
        return this;
    }

    Validator maxLength(String value, int length, String caption) {
        if (value != null && value.length() > length)
            errors.append(caption).append(" length must be less than ").append(length+1).append(" characters. ");
        return this;
    }

    Validator notEmpty(Collection value, String caption) {
        if (value == null || value.isEmpty()) {
            errors.append("Please, provide ").append(caption).append(". ");
        }
        return this;
    }

    public void validate() {
        if (errors.length() > 0)
            throw new ValidationException(errors.toString());
    }

    Validator notNull(Object value, String caption) {
        if (value == null)
            errors.append(caption).append(" is empty! ");
        return this;
    }

    Validator assertTrue(boolean condition, String message) {
        if (!condition)
            errors.append(message).append(". ");
        return this;
    }

    Validator assertFalse(boolean condition, String message) {
        if (condition)
            errors.append(message).append(". ");
        return this;
    }

    Validator email(String email, String caption) {
        Matcher matcher = PATTERN_EMAIL.matcher(email);
        if (!matcher.matches())
            errors.append(caption).append(" not a valid email address.");
        return this;
    }

    Validator assertId(boolean condition, Long id, String caption) {
        if (!condition)
            errors.append(caption).append(" {").append(id).append("} not found. ");
        return this;
    }
}
