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

/**
 *
 * @author hoangbd
 */
@Document(collection = "other_object")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OtherObject extends AbstractObjectDocument {

    @Transient
    public static final String SEQUENCE_NAME = "other_object_sequence";

    @Size(max = 36)
    @NotNull
    @Field("uuid")
    private String uuid;

    @Size(max = 256)
    @NotNull
    @Field("name")
    private String name;

    @Field("country_id")
    private Integer countryId;

    @Size(max = 512)
    @Field(name = "description")
    private String description;

    @Size(max = 36)
    @Field(name = "side_id")
    private String sideId;

    @Field(name = "is_deleted")
    private int isDeleted;

}
