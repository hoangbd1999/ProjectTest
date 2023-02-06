/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.data.process.model.dto.ObjectGroupConfigDTO;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;

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
public class ObjectGroupConfigRequestDTO implements Serializable {

    private String name;
    private String coordinates;
    private String areaUuid;
    private Integer isActive;
    private Date startTime;
    private Date endTime;
}
