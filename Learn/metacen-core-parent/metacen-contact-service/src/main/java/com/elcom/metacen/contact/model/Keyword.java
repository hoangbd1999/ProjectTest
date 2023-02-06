/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@Document(collection = "keyword")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Keyword extends AbstractDocument {

    @Size(max = 36)
    @NotNull
    @Field("uuid")
    private String uuid;

    @Size(max = 256)
    @NotNull
    @Field("name")
    private String name;

    @Size(max = 512)
    @Field("description")
    private String description;

    @Field("is_deleted")
    private int isDeleted;
}
