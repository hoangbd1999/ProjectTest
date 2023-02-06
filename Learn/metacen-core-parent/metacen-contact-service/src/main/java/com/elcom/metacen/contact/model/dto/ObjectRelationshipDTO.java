/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.model.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 *
 * @author Admin
 */
@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ObjectRelationshipDTO implements Serializable {

    private String fromTime;
    private String toTime;
    private String sourceObjectType;
    private String sourceObjectId;
    private String destObjectType;
    private String destObjectId;
    private String name;
    private String id;
    private Integer relationshipType;
    private String note;
}
