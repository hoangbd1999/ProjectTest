package com.elcom.metacen.enrich.data.model.dto;

import lombok.*;
import java.util.List;

/**
 *
 * @author Admin
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SatelliteImageDataAnalyzedFilterDTO {

    private Integer page;
    private Integer size;
    private String sort;
    private String term;
    private String fromTime;
    private String toTime;
    private List<String> dataVendorLst;
    private List<String> tileNumberLst;
    private List<Integer> processStatusLst;
    private List<Integer> commentLst;
}
