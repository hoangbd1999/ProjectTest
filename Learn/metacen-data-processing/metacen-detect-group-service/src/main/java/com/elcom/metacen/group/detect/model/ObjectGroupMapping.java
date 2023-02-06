package com.elcom.metacen.group.detect.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.time.LocalDateTime;

@Document(collection = "object_group_mapping")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ObjectGroupMapping implements Serializable {
    @Id
    private String id;

    @Field("created_time")
    private LocalDateTime createdTime;

    @Field("group_id")
    private String groupId;

    @Field("is_deleted")
    private Integer isDeleted;

    @Field("obj_id")
    private String objId;

    @Field("obj_name")
    private String objName;

    @Field("obj_note")
    private String objNote;

    @Field("obj_type_id")
    private Integer objTypeId;

    @Field("taked_to_sync")
    private Integer takedToSync;

    @Field("updated_time")
    private LocalDateTime updatedTime;

//    @Field("time_join")
//    private LocalDateTime timeJoin;

//    @Field("time_separate")
//    private LocalDateTime timeSeparate;
}
