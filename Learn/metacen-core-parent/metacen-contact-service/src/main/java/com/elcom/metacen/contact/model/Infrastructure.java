/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 *
 * @author hoangbd
 */
@Document(collection = "infrastructure")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Infrastructure extends AbstractObjectDocument {

    @Transient
    public static final String SEQUENCE_NAME = "infrastructure_sequence";

    @Size(max = 36)
    @NotNull
    @Field("uuid")
    private String uuid;

    @Size(max = 256)
    @NotNull
    @Field("name")
    private String name;

    @Size(max = 512)
    @Field("location")
    private String location;

    @Field("country_id")
    private Integer countryId;

    @Field("infrastructure_type")
    private Integer infrastructureType;

    @Size(max = 64)
    @Field("area")
    private String area;

    @Size(max = 512)
    @Field("description")
    private String description;

    @Size(max = 36)
    @Field("side_id")
    private String sideId;

    @Field("is_deleted")
    private int isDeleted;



}
