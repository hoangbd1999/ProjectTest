/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.model.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author hoangbd
 */
@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PeopleRequestDTO implements Serializable {

    private String name;
    private String mobileNumber;
    private String email;
    private Integer countryId;
    private Date dateOfBirth;
    private Integer gender;
    private String address;
    private String level;
    private String description;
    private String sideId;
    private List<FileDTO> imageLst;
    private List<FileDTO> fileAttachmentLst;
    private List<String> keywordLst;
    private List<ObjectRelationshipDTO> relationshipLst;
}
