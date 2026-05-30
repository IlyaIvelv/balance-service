package com.balanceservice.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PhoneFormatValidator implements ConstraintValidator<PhoneFormat, String> {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^7\\d{10}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || PHONE_PATTERN.matcher(value).matches();
    }
}