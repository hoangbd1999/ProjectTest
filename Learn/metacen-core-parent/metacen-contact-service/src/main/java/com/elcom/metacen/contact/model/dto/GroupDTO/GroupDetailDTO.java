/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.model.dto.GroupDTO;

import com.elcom.metacen.contact.model.dto.BaseDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

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
public class GroupDetailDTO extends BaseDTO<GroupDetailDTO> {

    private String uuidKey;
    private String name;
    private String note;
    private String sideId;
    private List<GroupObjectMappingDTO> groupObject;
    private Long numberTotal;
}
