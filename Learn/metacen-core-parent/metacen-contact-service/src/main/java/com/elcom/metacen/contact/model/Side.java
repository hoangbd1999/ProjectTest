/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author hoangbd
 */
@Document(collection = "side")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Side extends AbstractDocument{

    @Size(max = 36)
    @NotNull
    @Field("uuid")
    private String uuidKey;

    @Size(max = 100)
    @NotNull
    @Field("name")
    private String name;

    @Size(max = 500)
    @Field("note")
    private String note;

    @Field("is_deleted")
    private int isDeleted;
}
