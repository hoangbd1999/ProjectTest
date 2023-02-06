package com.elcom.metacen.dto.redis;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 *
 * @author Admin
 */
@Document(collection = "vsat_vessel_type")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class VsatVesselType implements Serializable {

//    @Field("type_id")
//    private Integer typeId;

    @Field("type_code")
    private String typeCode;

    @Field("type_name")
    private String typeName;
}
