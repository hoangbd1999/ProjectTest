/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.model;


import com.elcom.metacen.contact.model.dto.GroupDTO.GroupObjectMappingDTO;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author hoangbd
 */
@Document(collection = "group")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Group extends AbstractDocument {

    @Size(max = 36)
    @NotNull
    @Field("uuid")
    private String uuidKey;

    @Size(max = 200)
    @NotNull
    @Field("name")
    private String name;

    @Size(max = 1000)
    @Field("note")
    private String note;

    @Size(max = 2000)
    @NotNull
    @Field("group_object")
    private List<GroupObjectMappingDTO> groupObject;

    @Field("is_deleted")
    private int isDeleted;

    @Size(max = 36)
    @Field("side_id")
    private String sideId;

}
