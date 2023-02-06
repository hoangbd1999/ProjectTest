package com.elcom.metacen.contact.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author hoangbd
 */
@Document(collection = "areas")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Areas extends AbstractObjectDocument {

    @Transient
    public static final String SEQUENCE_NAME = "areas_sequence";

    @Size(max = 36)
    @NotNull
    @Field("uuid")
    private String uuid;

    @Size(max = 300)
    @Field("name")
    private String name;

    @Field(name = "value")
    private String value;

    @Size(max = 1000)
    @Field(name = "description")
    private String description;

    @Size(max = 36)
    @Field(name = "side_id")
    private String sideId;

    @Field(name = "is_deleted")
    private int isDeleted;

}
