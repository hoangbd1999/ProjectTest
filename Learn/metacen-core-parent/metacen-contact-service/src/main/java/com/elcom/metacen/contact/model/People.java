/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
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
@Document(collection = "people_info")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class People extends AbstractObjectDocument {

    @Transient
    public static final String SEQUENCE_NAME = "people_sequence";

    @Size(max = 36)
    @NotNull
    @Field("uuid")
    private String uuid;

    @Size(max = 200)
    @NotNull
    @Field("name")
    private String name;

    @Size(max = 50)
    @Field("mobile_number")
    private String mobileNumber;

    @Size(max = 100)
    @Field("email")
    private String email;

    @Field("country_id")
    private Integer countryId;

    @Field("date_of_birth")
    private Date dateOfBirth;

    @Field("gender")
    private Integer gender;

    @Size(max = 300)
    @Field("address")
    private String address;

    @Size(max = 100)
    @Field("level")
    private String level;

    @Size(max = 1000)
    @Field("note")
    private String description;

    @Field("is_deleted")
    private int isDeleted;

    @Size(max = 36)
    @Field("side_id")
    private String sideId;

}
