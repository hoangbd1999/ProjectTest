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
@Document(collection = "other_vehicle")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OtherVehicle extends AbstractObjectDocument {

    @Transient
    public static final String SEQUENCE_NAME = "vehicle_sequence";

    @Size(max = 36)
    @NotNull
    @Field("uuid")
    private String uuid;

    @Size(max = 256)
    @NotNull
    @Field("name")
    private String name;

    @Field(name = "dim_length")
    private Double dimLength;

    @Field(name = "dim_width")
    private Double dimWidth;

    @Field(name = "dim_height")
    private Double dimHeight;

    @Field("country_id")
    private Integer countryId;

    @Size(max = 512)
    @Field(name = "description")
    private String description;

    @Field(name = "tonnage")
    private Double tonnage;

    @Field(name = "payroll")
    private String payroll;

    @Size(max = 36)
    @Field(name = "side_id")
    private String sideId;

    @Size(max = 200)
    @Field(name = "equipment")
    private String equipment;

    @Field(name = "speed_max")
    private Double speedMax;

    @Field(name = "is_deleted")
    private int isDeleted;

}
