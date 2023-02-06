package com.elcom.metacen.vsat.collector.model.mongodb;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.io.Serializable;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;

@Document(collection = "object_group_mapping")
//@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
//@RequiredArgsConstructor
//@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ObjectGroupMapping implements Serializable {

    @Id
    protected String id;

    @Field(name = "obj_id")
    private String objId;
    
    @Field(name = "group_id")
    private String groupId;

    @Field(name = "is_deleted")
    private Integer isDeleted;

    @Field(name = "taked_to_sync")
    private Integer takedToSync;
    
    @Field("obj_name")
    private String objName;

    @Field("obj_note")
    private String objNote;
    
    @Field("obj_type_id")
    private Integer objTypeId;
    
    @LastModifiedDate
    @Field("updated_time")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    @CreatedDate
    @Field("created_time")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @Transient
    private Long updatedTimeToMs;
}
