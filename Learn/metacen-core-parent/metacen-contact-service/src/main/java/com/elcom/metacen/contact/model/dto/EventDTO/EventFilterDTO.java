package com.elcom.metacen.contact.model.dto.EventDTO;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author hoangbd
 */
@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EventFilterDTO {
    private Integer page;
    private Integer size;
    private String sort;
    private String term;
    private Date startTime;
    private Date stopTime;
    private List<String> sideIds;
    private List<String> keywordIds;
}
