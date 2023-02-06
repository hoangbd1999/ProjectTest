/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.data.process.model.dto;

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
public class DataProcessConfigResponseDTO extends BaseDTO<DataProcessConfigResponseDTO> {

    private String uuid;
    private String name;
    private String dataType;
    private String processType;
    private String dataVendor;
    private Object detailConfig;
    private Integer status;
    private Date startTime;
    private Date endTime;

}
