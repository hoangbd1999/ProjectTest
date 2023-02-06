package com.elcom.metacen.raw.data.model.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AisDataDTO implements Serializable {

//    private String uuidKey;
    private BigInteger mmsi;
    private String name; // lấy qua luồng SHIP-DT
    private String imo;
//    private Integer countryId;
//    private String countryName;
    private Float sog;
    private Float cog;
    private BigDecimal longitude;
    private BigDecimal latitude;
    
    /* Lấy qua luồng parse AIS message ( !AIDVO........... ) */
    /*private String messageType;
    private String shipType;
    private Integer second;
    private String communicationStateSyncState;
    private String specialManeuverIndicator;
    private Integer toStern;
    private Integer toPort;
    private boolean dataTerminalReady;
    private String positionFixingDevice;
    private Integer toStarboard;
    private Integer toBow;
    private Integer rateOfTurn;
    private Integer repeatIndicator;
    private String transponderClass;
    private Integer navigationStatus;
    private boolean raimFlag;
    private boolean positionAccuracy;
    private boolean valid;
    private String eta;
    private String destination;
    private Integer trueHeading;
    private Float draught;
    private Float rot;*/
    /*-------------------------------------*/
    
    private String dataVendor;
    private Timestamp eventTime;
}
