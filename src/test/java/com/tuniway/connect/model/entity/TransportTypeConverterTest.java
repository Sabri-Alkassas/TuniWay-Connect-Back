package com.tuniway.connect.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class TransportTypeConverterTest {

    private final TransportTypeConverter converter = new TransportTypeConverter();

    @Test
    void convertToEntityAttributeShouldSupportCanonicalValuesAndAliases() {
        assertEquals(TransportType.BUS, converter.convertToEntityAttribute("BUS"));
        assertEquals(TransportType.TRAIN, converter.convertToEntityAttribute("train"));
        assertEquals(TransportType.METRO, converter.convertToEntityAttribute("TRAM"));
        assertEquals(TransportType.METRO, converter.convertToEntityAttribute("light rail"));
        assertEquals(TransportType.METRO, converter.convertToEntityAttribute("Métro léger"));
        assertEquals(TransportType.METRO, converter.convertToEntityAttribute("TGM"));
    }

    @Test
    void convertToEntityAttributeShouldReturnNullForUnknownDatabaseValues() {
        assertNull(converter.convertToEntityAttribute("friend-bus"));
        assertNull(converter.convertToEntityAttribute("   "));
        assertNull(converter.convertToEntityAttribute(null));
    }
}