/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.mapping.data.model.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;

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
public class MappingVsatRequestDTO implements Serializable {

    private Integer vsatDataSourceId;
    private String vsatDataSourceName;
    private String vsatIpAddress;
    private String objectType;
    private String objectId;
    private String objectUuid;
    private String objectName;

}
