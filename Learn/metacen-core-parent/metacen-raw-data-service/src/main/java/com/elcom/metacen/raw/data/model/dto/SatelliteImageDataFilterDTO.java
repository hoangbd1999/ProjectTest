package com.elcom.metacen.raw.data.model.dto;

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
public class SatelliteImageDataFilterDTO {

    private Integer page;
    private Integer size;
    private String sort;
    private String term;
    private String fromTime;
    private String toTime;
    private List<String> dataVendorLst;
    private List<String> tileNumberLst;
    private String dataVendor;
    private String satelliteName;
    private String coordinates;
    private String captureTime;
}
