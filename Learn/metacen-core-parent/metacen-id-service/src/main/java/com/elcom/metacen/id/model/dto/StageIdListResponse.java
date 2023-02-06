/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 *
 * @author Admin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
public class StageIdListResponse implements Serializable {
    @JsonProperty("status")
    @NonNull
    private Integer status;

    @JsonProperty("message")
    @NonNull
    private String message;

    @JsonProperty("data")
    private List<String> data;

    @JsonProperty("size")
    private Object size;

    @JsonProperty("page")
    private Object page;

    @JsonProperty("total")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long total;
}
