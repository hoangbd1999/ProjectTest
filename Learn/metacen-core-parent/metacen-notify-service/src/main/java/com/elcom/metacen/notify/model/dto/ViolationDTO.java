/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.notify.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 * @author hanh
 */
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ViolationDTO implements Serializable {

    protected String id;
    protected String siteName;
    protected String siteAddress;
    protected String sourceId;
    protected String sourceName;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    protected Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    protected Date endTime;

    protected String regPlate;
    protected String plate;
    protected String regObjectType;
    protected String objectType;
    protected String objectTypeName; // phuong tien

    protected int eventType; // id su kien
    protected String eventIdString; // Id generated of event
    protected String eventState;
    protected String eventTypeString; // ma su kien
    protected String eventTypeName; // ten su kien ~ ten ha`nh vi

    protected String imageUrl;
    protected String videoUrl;
    protected String imageCtxtUrl;
    protected float longitude;
    protected float latitude;
    protected BBox bbox;
    protected int laneid;
    protected String plateStatus;
    protected Float speed;
    protected Float speedLimit;
    protected String notes;

    protected String processStatus;
    protected String processStatusName;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    protected Date processTime;

    protected String violationConfirm;
    protected String violationConfirmation;

    @Builder.Default
    protected Boolean adjustObjectType = false;

    @Builder.Default
    protected Boolean adjustPlate = false;

    protected String parentId;
    protected String minutesNumber;

    private Short version;

    private boolean pushed;

    private Float speedOfVehicle;
}
