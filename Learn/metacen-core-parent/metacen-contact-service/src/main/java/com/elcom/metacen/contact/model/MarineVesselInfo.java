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
import java.util.List;


@Document(collection = "marine_vessel_info")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MarineVesselInfo extends AbstractObjectDocument {

    @Transient
    public static final String SEQUENCE_NAME = "vehicle_sequence";

    @Size(max = 36)
    @NotNull
    @Field("uuid")
    private String uuid;

    @Field(name = "mmsi")
    private Long mmsi;

    @Size(max = 200)
    @Field(name = "name")
    private String name;

    @Size(max = 10)
    @Field(name = "imo")
    private String imo;

    @Field(name = "country_id")
    private Long countryId;

    @Size(max = 36)
    @Field(name = "type_id")
    private String typeId;

    @Field(name = "dim_a")
    private Double dimA;

    @Field(name = "dim_c")
    private Double dimC;

    @Size(max = 200)
    @Field(name = "payroll")
    private String payroll;

    @Size(max = 2000)
    @Field(name = "description")
    private String description;

    @Size(max = 200)
    @Field(name = "equipment")
    private String equipment;

    @Field(name = "draught")
    private Long draught;

    @Field(name = "gross_tonnage")
    private Double grossTonnage;

    @Field(name = "speed_max")
    private Double speedMax;

    @Field(name = "is_deleted")
    private int isDeleted;

    @Size(max = 36)
    @Field(name = "side_id")
    private String sideId;

}
