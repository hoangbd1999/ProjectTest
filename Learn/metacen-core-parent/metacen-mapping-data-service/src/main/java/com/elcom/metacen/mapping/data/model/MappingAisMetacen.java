package com.elcom.metacen.mapping.data.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Document(collection = "mapping_ais_metacen")
@SuperBuilder
@Data
@RequiredArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MappingAisMetacen extends AbstractDocument implements Serializable {

    private static final long serialVersionUID = 1L;

    @Size(max = 36)
    @NotNull
    @Field("uuid")
    private String uuid;

    @Field("ais_mmsi")
    private Integer aisMmsi;

    @Field("ais_ship_name")
    private String aisShipName;

    @Field("object_type")
    private String objectType;

    @Field("object_id")
    private String objectId;

    @Field("object_uuid")
    private String objectUuid;

    @Field("object_name")
    private String objectName;
}
