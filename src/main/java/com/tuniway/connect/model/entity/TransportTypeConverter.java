package com.tuniway.connect.model.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TransportTypeConverter implements AttributeConverter<TransportType, String> {
    private static final Logger log = LoggerFactory.getLogger(TransportTypeConverter.class);

    @Override
    public String convertToDatabaseColumn(TransportType attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public TransportType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        TransportType transportType = TransportType.fromExternalValue(dbData);
        if (transportType != null) {
            return transportType;
        }

        log.warn("Unknown transport.type value '{}' found in database; treating it as null", dbData);
        return null;
    }
}

