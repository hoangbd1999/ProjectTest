package com.elcom.metacen.group.detect.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Date;

/**
 *
 * @author hoangbd
 */
@Document(collection = "object_group_config")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@ToString
public class ObjectGroupConfig extends AbstractDocument {

    @Size(max = 36)
    @NotNull
    @Field("uuid")
    private String uuid;

    @Field("name")
    private String name;

    @Field("coordinates")
    private String coordinates;

    @Field("distance")
    private Integer distance;

    @Field("is_active")
    private Integer isActive;

    @Field(name = "start_time")
    private LocalDateTime startTime;

    @Field(name = "end_time")
    private LocalDateTime endTime;

}
