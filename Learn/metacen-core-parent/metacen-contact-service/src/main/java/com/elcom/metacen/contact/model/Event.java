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
import java.util.List;

/**
 *
 * @author hoangbd
 */
@Document(collection = "event")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Event extends AbstractObjectDocument {
    @Transient
    public static final String SEQUENCE_NAME = "event_sequence";

    @Size(max = 36)
    @NotNull
    @Field("uuid")
    private String uuid;

    @Size(max = 200)
    @NotNull
    @Field("name")
    private String name;

    @Field(name = "start_time")
    private Date startTime;

    @Field(name = "stop_time")
    private Date stopTime;

    @Size(max = 2000)
    @Field(name = "description")
    private String description;

    @Size(max = 36)
    @Field(name = "side_id")
    private String sideId;

    @Field(name = "is_deleted")
    private int isDeleted;

    @Size(max = 1000)
    @Field(name = "area")
    private String area;

}
