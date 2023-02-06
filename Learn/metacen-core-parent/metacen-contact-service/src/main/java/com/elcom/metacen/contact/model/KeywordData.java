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
import java.util.List;

/**
 *
 * @author Admin
 */
@Document(collection = "keyword_data")
@SuperBuilder
@Setter
@Getter
@RequiredArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class KeywordData extends AbstractDocument {

    @Size(max = 36)
    @NotNull
    @Field("uuid")
    private String uuid;

    @Field("type")
    private int type;

    @Size(max = 36)
    @NotNull
    @Field("ref_id")
    private String refId;

    @Size(max = 200)
    @Field("ref_type")
    private String refType;

    @Size(max = 36)
    @Field("keyword_id")
    private String keywordId;

}
