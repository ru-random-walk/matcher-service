package ru.randomwalk.matcherservice.config.converter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UUIDSetConverter implements AttributeConverter<Set<UUID>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Set<UUID> attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Set<UUID> to JSON", e);
        }
    }

    @Override
    public Set<UUID> convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return new HashSet<>();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<Set<UUID>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to Set<UUID>", e);
        }
    }
}