package com.elcom.metacen.report.model.dto;

import lombok.*;

import java.util.List;

/**
 *
 * @author hoangbd
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DataAnalyzedFilterDTO {

    private String fromTime;
    private String toTime;
    private List<String> processTypeLst;

}
