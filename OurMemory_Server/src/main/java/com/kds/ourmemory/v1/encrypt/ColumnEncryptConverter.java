package com.kds.ourmemory.v1.encrypt;

import lombok.RequiredArgsConstructor;

import javax.persistence.AttributeConverter;
import java.util.Optional;

@RequiredArgsConstructor
public class ColumnEncryptConverter implements AttributeConverter<String, String> {

    private final ColumnEncryptModule columnEncryptModule;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return Optional.ofNullable(attribute).map(columnEncryptModule::encrypt).orElse(null);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return Optional.ofNullable(dbData).map(columnEncryptModule::decrypt).orElse(null);
    }
}
