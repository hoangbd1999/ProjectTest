/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.model.dto.ObjectGroup;

import com.elcom.metacen.contact.model.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
public class ObjectGroupResponseDTO extends BaseDTO<ObjectGroupResponseDTO> {

    private String uuid;
    private String name;
    private String note;
    private String configName;
    private String configUuid;
    private Integer isConfirmed;
    private Integer configTogetherTime;
    private Integer configDistanceLevel;
    private Date confirmDate;
    private List<Date> eventTimes;
    private Map<String, Object> mappingPairInfos;
    private Date firstTogetherTime;
    private Date lastTogetherTime;
    protected String updatedBy;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime updatedAt;
    private Integer countNumber;
    private List<ObjectGroupMappingDTO> objects;

}
