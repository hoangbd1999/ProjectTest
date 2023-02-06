package com.elcom.metacen.group.detect.model;


import lombok.AllArgsConstructor;

import lombok.Data;

import lombok.NoArgsConstructor;

import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.Document;

import org.springframework.data.mongodb.core.mapping.Field;


import javax.validation.constraints.NotNull;

import javax.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Document(collection = "object_group")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ObjectGroup extends AbstractDocument{
    @Field("config_distance_level")
    private Integer configDistanceLevel;

    @Field("config_name")
    private String configName;

    @Field("config_together_time")
    private Integer configTogetherTime;

    @Field("config_uuid")
    private String configUuid;

    @Field("confirm_date")
    private LocalDateTime confirmDate;

    @Field("is_confirmed")
    private Integer isConfirmed;

    @Field("is_deleted")
    private Integer isDeleted;

    @Field("name")
    private String name;

    @Field("note")
    private String note;

    @Size(max = 36)
    @NotNull
    @Field("uuid")
    private String uuid;

    @Field("updated_by")
    private String updatedBy;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("first_together_time")
    private LocalDateTime firstTogetherTime;

    @Field("mapping_pair_info")
    private Map<String, MappingPairInfo> mappingPairInfos;

    @Field("last_together_time")
    private LocalDateTime lastTogetherTime;
}
