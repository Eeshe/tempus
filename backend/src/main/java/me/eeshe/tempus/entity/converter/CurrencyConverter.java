package me.eeshe.tempus.entity.converter;

import java.util.Currency;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import me.eeshe.tempus.entity.UserSettings;

@Converter
public class CurrencyConverter implements AttributeConverter<Currency, String> {

    @Override
    public String convertToDatabaseColumn(Currency currency) {
        return (currency == null ? UserSettings.DEFAULT_CURRENCY : currency).getCurrencyCode();
    }

    @Override
    public Currency convertToEntityAttribute(String currencyCode) {
        if (currencyCode == null) {
            return UserSettings.DEFAULT_CURRENCY;
        }
        return Currency.getInstance(currencyCode);
    }
}
