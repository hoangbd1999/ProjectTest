package com.elcom.metacen.report.model.dto;

import lombok.*;
import java.util.Date;

/**
 *
 * @author hoangbd
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataAnalyzedRequestDTO {

    private String uuidKey;
    private String refUuidKey;
    private String processType;
    private Date eventTime;
    private Date timeProcess;
    private Date ingestTime;
    private Integer processStatus;

}
