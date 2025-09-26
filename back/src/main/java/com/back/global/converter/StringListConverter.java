package com.back.global.converter;

import com.back.standard.util.Ut;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

@Converter
@Component
public class StringListConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        return attribute == null ? null : Ut.json.toString(attribute);
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        return Ut.json.toList(dbData, String.class);
    }
}
