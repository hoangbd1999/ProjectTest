/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 *
 * @author Admin
 */
@Document(collection = "object_relationship")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ObjectRelationship extends AbstractDocument {

    @Field("from_time")
    @JsonProperty
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fromTime;

    @Field("to_time")
    @JsonProperty
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime toTime;

    @Size(max = 36)
    @NotNull
    @Field("source_object_id")
    private String sourceObjectId;

    @Size(max = 20)
    @NotNull
    @Field("source_object_type")
    private String sourceObjectType;

    @Size(max = 36)
    @NotNull
    @Field("dest_object_id")
    private String destObjectId;

    @Size(max = 20)
    @NotNull
    @Field("dest_object_type")
    private String destObjectType;

    @Field("relationship_type")
    @NotNull
    private int relationshipType;

    @Size(max = 512)
    @Field("note")
    private String note;

    @Field("no")
    private Integer no;

    @Field("is_deleted")
    private int isDeleted;
}
