package com.elcom.abac.model;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Unit {
    private String uuid;

    private String code;

    private String name;

    private String address;

    private String phone;

    private String email;

    private String description;

    private String lisOfStage;

    private String listOfJob;
}