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
public class VsatAisFilterListRequestDTO {

    private Integer limit;
    private String fromTime;
    private String toTime;
    private String mmsi;
}
