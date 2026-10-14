package me.eeshe.tempus.common.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import me.eeshe.tempus.common.validation.validator.StartTimeBeforeOrEqualToEndTimeValidator;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StartTimeBeforeOrEqualToEndTimeValidator.class)
public @interface StartTimeBeforeOrEqualToEndTime {
    String message() default "Time entry start time can't be after end time";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
