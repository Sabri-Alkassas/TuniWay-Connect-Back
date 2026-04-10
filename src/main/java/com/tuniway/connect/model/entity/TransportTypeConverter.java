package com.tuniway.connect.model.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

@Converter(autoApply = false)
public class TransportTypeConverter implements AttributeConverter<TransportType, String> {

    @Override
    public String convertToDatabaseColumn(TransportType attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public TransportType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        return TransportType.valueOf(dbData.trim().toUpperCase(Locale.ENGLISH));
    }
}
