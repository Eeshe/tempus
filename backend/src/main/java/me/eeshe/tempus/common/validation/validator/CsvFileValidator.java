package me.eeshe.tempus.common.validation.validator;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import me.eeshe.tempus.common.validation.annotation.CsvFile;

public class CsvFileValidator implements ConstraintValidator<CsvFile, MultipartFile> {

    @Override
    public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        final String filename = value.getOriginalFilename();

        return filename != null && filename.toLowerCase().endsWith(".csv");
    }
}
