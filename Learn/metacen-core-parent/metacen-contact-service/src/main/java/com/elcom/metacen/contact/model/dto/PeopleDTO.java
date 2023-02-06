package com.elcom.metacen.contact.model.dto;

import com.elcom.metacen.contact.model.dto.EventDTO.EventDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

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
public class PeopleDTO extends BaseDTO<PeopleDTO>{
    private String uuid;
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
}
