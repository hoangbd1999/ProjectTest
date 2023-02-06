package com.elcom.metacen.vsat.media.process.model.dto;

import com.elcom.metacen.vsat.media.process.model.kafka.VsatMediaMessageFull;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import lombok.Data;

/**
 *
 * @author Admin
 */
@Data
public class ContentMetaDataRequestDTO {

    private List<VsatMediaMessageFull> contentDTOS;

    public String toJsonString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
