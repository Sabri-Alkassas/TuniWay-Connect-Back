package com.tuniway.connect.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

// Unit tests for TransportTypeConverter to verify the correctness of the conversion logic between database values and entity attributes. The tests cover both canonical values and various aliases for each transport type, as well as handling of unknown or invalid database values. The assertions ensure that the converter correctly maps input strings to the expected TransportType enum values or returns null when appropriate.
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