/*
 * Copyright (c) 2023 | smartSense
 */

package eu.gaiax.wizard.dao.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.AttributeConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * The type String to set convertor.
 */
public class StringToSetConvertor implements AttributeConverter<Set<String>, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringToSetConvertor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public String convertToDatabaseColumn(Set<String> set) {
        if (set == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(set);
        } catch (JsonProcessingException ex) {
            LOGGER.error("convertToDatabaseColumn: Error", ex);
            return null;
        }
    }

    @Override
    public Set<String> convertToEntityAttribute(String dbData) {
        if (StringUtils.isEmpty(dbData)) {
            return Collections.emptySet();
        }
        try {
            return objectMapper.readValue(dbData, Set.class);
        } catch (IOException ex) {
            LOGGER.error("Unexpected IOEx decoding json from database: {}", dbData, ex);
            return Collections.emptySet();
        }
    }

}