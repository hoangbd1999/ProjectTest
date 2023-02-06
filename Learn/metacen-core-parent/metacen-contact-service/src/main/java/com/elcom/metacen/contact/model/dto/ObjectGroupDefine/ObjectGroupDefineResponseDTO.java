/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.model.dto.ObjectGroupDefine;

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
public class ObjectGroupDefineResponseDTO extends BaseDTO<ObjectGroupDefineResponseDTO> {

    private String uuid;
    private String name;
    private String note;
    private Integer countNumber;
    private List<ObjectGroupDefineMappingDTO> objects;

}
