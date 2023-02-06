package com.elcom.metacen.raw.data.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class SatelliteDataFilterDTO implements Serializable {

    private Integer page;
    private Integer size;
    private String sort;
    private String fromTime;
    private String toTime;
    private Integer mmsi;
    private String longitude;
    private String latitude;
    private List<String> dataVendors;
    private List<Integer> processStatus; // Trạng thái
    private String dataVendor;
    private String satelliteName;
    private String imageFilePath;
    private String coordinates;
    private Integer status;
    private String eventTime;
}
