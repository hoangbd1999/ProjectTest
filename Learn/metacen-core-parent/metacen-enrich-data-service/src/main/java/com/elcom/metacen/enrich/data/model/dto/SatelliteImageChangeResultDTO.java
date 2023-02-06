package com.elcom.metacen.enrich.data.model.dto;

import lombok.*;
import org.hibernate.type.TimestampType;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author Admin
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SatelliteImageChangeResultDTO {

    private String uuidKey;
    private String satelliteImageChangesUuidKey;
    private BigDecimal originLatitude;
    private BigDecimal originLongitude;
    private BigDecimal cornerLatitude;
    private BigDecimal cornerLongitude;
    private Long width;
    private Long height;
    private String imageFilePathOrigin;
    private String imageFilePathCompare;
    private Timestamp ingestTime;

}
