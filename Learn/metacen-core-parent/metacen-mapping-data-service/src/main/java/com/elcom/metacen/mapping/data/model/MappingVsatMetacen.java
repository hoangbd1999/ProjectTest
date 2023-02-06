/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.mapping.data.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author Admin
 */
@Document(collection = "mapping_vsat_metacen")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MappingVsatMetacen extends AbstractDocument {

    @Size(max = 36)
    @NotNull
    @Field("uuid")
    private String uuid;

    @Field("vsat_data_source_id")
    private Integer vsatDataSourceId;

    @Field("vsat_data_source_name")
    private String vsatDataSourceName;

    @Field("vsat_ip_address")
    private String vsatIpAddress;

    @Field("object_type")
    private String objectType;

    @Field("object_id")
    private String objectId;

    @Field("object_uuid")
    private String objectUuid;

    @Field("object_name")
    private String objectName;

}
