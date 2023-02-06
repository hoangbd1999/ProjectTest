package com.elcom.metacen.report.model.dto;

import lombok.*;

/**
 *
 * @author hoangbd
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataAnalyzedRequestReportDTO {

    private String uuidKey;
    private String refUuidKey;
    private String processType;
    private Long eventTime;
    private Long processTime;
    private Long ingestTime;
    private Integer processStatus;

}
