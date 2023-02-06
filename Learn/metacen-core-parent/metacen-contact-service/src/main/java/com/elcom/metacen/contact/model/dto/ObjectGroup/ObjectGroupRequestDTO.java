/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.model.dto.ObjectGroup;

import com.elcom.metacen.contact.model.dto.ObjectRelationshipDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
public class ObjectGroupRequestDTO implements Serializable {

    private String name;
    private String note;
    private String configName;
    private String configUuid;
    private Integer configDistanceLevel;
    private Integer configTogetherTime;
   // private List<Date> eventTimes;
    private List<ObjectGroupMappingDTO> objects;
}
