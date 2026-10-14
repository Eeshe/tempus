package me.eeshe.tempus.common.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import me.eeshe.tempus.common.validation.annotation.StartTimeBeforeOrEqualToEndTime;
import me.eeshe.tempus.dto.CreateTimeEntryRequestDTO;

public class StartTimeBeforeOrEqualToEndTimeValidator
        implements ConstraintValidator<StartTimeBeforeOrEqualToEndTime, CreateTimeEntryRequestDTO> {

    @Override
    public boolean isValid(CreateTimeEntryRequestDTO value, ConstraintValidatorContext context) {
        if (value == null || value.startTime() == null || value.endTime() == null) {
            return true;
        }
        return !value.endTime().isBefore(value.startTime());
    }
}
