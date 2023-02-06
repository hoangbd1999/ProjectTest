/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elcom.com.neo4j.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.util.Map;

/**
 * @author hanh
 */
@Slf4j
public class HashMapConverter implements AttributeConverter<Map<String, Object>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> objInfo) {

        String objInfoJson = null;
        try {
            objInfoJson = objectMapper.writeValueAsString(objInfo);
        } catch (final JsonProcessingException e) {
            log.error("JSON writing error", e);
        }

        return objInfoJson;
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String objInfoJSON) {

        Map<String, Object> objInfo = null;
        if (objInfoJSON != null) {
            try {
                objInfo = objectMapper.readValue(objInfoJSON, Map.class);
            } catch (final IOException e) {
                log.error("JSON reading error", e);
            }
        }

        return objInfo;
    }

}
