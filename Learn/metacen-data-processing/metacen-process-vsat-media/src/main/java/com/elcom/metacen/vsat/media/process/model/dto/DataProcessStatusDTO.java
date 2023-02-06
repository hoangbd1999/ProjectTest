package com.elcom.metacen.vsat.media.process.model.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Admin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataProcessStatusDTO implements Serializable {

    private String recordId;
    private Integer processStatus;
    private Long eventTimeInMs;
    private Long ingestTimeInMs;
}
