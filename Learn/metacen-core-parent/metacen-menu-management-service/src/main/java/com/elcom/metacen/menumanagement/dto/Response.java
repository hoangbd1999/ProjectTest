/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.menumanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.*;

/**
 *
 * @author Admin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
public class Response implements Serializable {
    
    @JsonProperty("status")
    @NonNull
    private int status;
    
    @JsonProperty("message")
    @NonNull
    private String message;
    
    @JsonProperty("data")
    private Object data;
    
    @JsonProperty("total")
    @JsonInclude(Include.NON_NULL)
    private Long total;
    
    public Response(int status, String message, Object data){
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
