package com.elcom.metacen.id.model.dto;

import lombok.*;

import java.io.Serializable;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UnitDTO implements Serializable {
    private String uuid;
    private String code;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String description;
    private String lisOfStage;
    private String listOfJob;
    private boolean processStatus = false;
}
