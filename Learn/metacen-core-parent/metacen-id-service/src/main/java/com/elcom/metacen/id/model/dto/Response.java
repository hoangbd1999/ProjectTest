package com.elcom.metacen.id.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

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

    @JsonProperty("size")
    private Object size;

    @JsonProperty("page")
    private Object page;

    @JsonProperty("total")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long total;

    public Response(int status, String message, Object data){
        this.status = status;
        this.message = message;
        this.data = data;
    }
}

