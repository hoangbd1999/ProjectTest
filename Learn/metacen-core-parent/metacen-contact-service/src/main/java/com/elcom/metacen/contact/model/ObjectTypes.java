/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author hoangbd
 */
@Document(collection = "object_types")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ObjectTypes extends AbstractDocument {

    @Size(max = 36)
    @NotNull
    @Field("type_id")
    private String typeId;

    @Size(max = 200)
    @Field(name = "type_name")
    private String typeName;

    @Size(max = 100)
    @Field(name = "type_code")
    private String typeCode;

    @Size(max = 300)
    @Field(name = "type_desc")
    private String typeDesc;

    @Size(max = 20)
    @Field(name = "type_object")
    private String typeObject;

    @Field(name = "is_deleted")
    private int isDeleted;

}
