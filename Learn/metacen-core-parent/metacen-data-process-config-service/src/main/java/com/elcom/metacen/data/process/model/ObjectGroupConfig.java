package com.elcom.metacen.data.process.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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

    @Field("area_uuid")
    private String areaUuid;

    @Field("coordinates")
    private String coordinates;

    @Field("is_active")
    private Integer isActive;

    @Field(name = "start_time")
    private Date startTime;

    @Field(name = "end_time")
    private Date endTime;

}
