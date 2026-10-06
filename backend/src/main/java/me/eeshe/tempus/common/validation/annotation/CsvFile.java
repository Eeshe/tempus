package me.eeshe.tempus.common.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import me.eeshe.tempus.common.validation.validator.CsvFileValidator;

@Target({ ElementType.TYPE_USE, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CsvFileValidator.class)
public @interface CsvFile {
    String message() default "must be a CSV file";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
