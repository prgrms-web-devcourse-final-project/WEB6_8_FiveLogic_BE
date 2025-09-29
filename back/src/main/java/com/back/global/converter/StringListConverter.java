package com.back.global.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Converter
@Component
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON 변환 실패", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            // 이중 인코딩된 경우 처리
            String data = dbData;
            if (data.startsWith("\"") && data.endsWith("\"")) {
                data = objectMapper.readValue(data, String.class);
            }

            return objectMapper.readValue(
                data,
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        } catch (JsonProcessingException e) {
            System.err.println("JSON 파싱 실패. 원본 데이터: " + dbData);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
