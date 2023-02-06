package com.elcom.metacen.contact.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Document(collection = "aero_airplane_info")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AeroAirplaneInfo extends AbstractObjectDocument {

    @Transient
    public static final String SEQUENCE_NAME = "vehicle_sequence";

    @Size(max = 36)
    @NotNull
    @Field(name = "uuid")
    private String uuid;

    @Size(max = 256)
    @NotNull
    @Field(name = "name")
    private String name;

    @Field(name = "model")
    private String model;

    @Field(name = "country_id")
    private Integer countryId;

    // Kích thước: chiều dài
    @Field(name = "dim_length")
    private Double dimLength;

    // Kích thước: chiều rộng
    @Field(name = "dim_width")
    private Double dimWidth;

    // Kích thước: sải cánh
    @Field(name = "dim_height")
    private Double dimHeight;

    // Vận tốc tối đa
    @Field(name = "speed_max")
    private Double speedMax;

    // Trọng tải
    @Field(name = "gross_tonnage")
    private Double grossTonnage;

    // Thời gian biên chế
    @Size(max = 50)
    @Field(name = "payroll_time")
    private String payrollTime;

    // Trang bị trên máy bay
    @Size(max = 200)
    @Field(name = "equipment")
    private String equipment;

    // Căn cứ thường trú
    @Size(max = 200)
    @Field(name = "permanent_base")
    private String permanentBase;

    @Size(max = 512)
    @Field("description")
    private String description;

    @Size(max = 36)
    @Field(name = "side_id")
    private String sideId;

    @Size(max = 36)
    @Field(name = "type_id")
    private String typeId;

    @Field(name = "is_deleted")
    private int isDeleted;
}
