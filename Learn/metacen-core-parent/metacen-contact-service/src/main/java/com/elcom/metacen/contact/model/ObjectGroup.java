package com.elcom.metacen.contact.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author hoangbd
 */
@Document(collection = "object_group")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@ToString
public class ObjectGroup extends AbstractDocument {

    @Size(max = 36)
    @NotNull
    @Field("uuid")
    private String uuid;

    @Field("name")
    private String name;

    @Field("note")
    private String note;

    @Field("config_name")
    private String configName;

    @Field("config_uuid")
    private String configUuid;

    @Field("is_confirmed")
    private Integer isConfirmed;

    @Field("config_together_time")
    private Integer configTogetherTime;

    @Field("config_distance_level")
    private Integer configDistanceLevel;

    @Field("is_deleted")
    private Integer isDeleted;

    @Field(name = "confirm_date")
    private Date confirmDate;

    @Field("event_times")
    private List<Date> eventTimes;

    @CreatedBy
    @Field("updated_by")
    private String updatedBy;

    @CreatedDate
    @Field("updated_at")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Field("first_together_time")
    private Date firstTogetherTime;

    @Field("mapping_pair_info")
    private Map<String, Object> mappingPairInfos;

    @Field("last_together_time")
    private Date lastTogetherTime;

}
