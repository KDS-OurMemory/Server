package com.kds.ourmemory.v1.encrypt;

import com.kds.ourmemory.v1.advice.common.exception.ColumnEncryptException;
import lombok.RequiredArgsConstructor;

import javax.persistence.AttributeConverter;

@RequiredArgsConstructor
public class ColumnEncryptConverter implements AttributeConverter<String, String> {

    private final ColumnEncryptModule columnEncryptModule;

    @Override
    public String convertToDatabaseColumn(String attribute) {

        try {
            return columnEncryptModule.encrypt(attribute);
        } catch (Exception e) {
            throw new ColumnEncryptException(e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            return columnEncryptModule.decrypt(dbData);
        } catch (Exception e) {
            throw new ColumnEncryptException(e);
        }
    }
}
