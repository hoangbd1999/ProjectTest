package com.elcom.metacen.contact.model.dto.AreasDTO;

import com.elcom.metacen.contact.model.dto.BaseDTO;
import com.elcom.metacen.contact.model.dto.EventDTO.EventDTO;
import com.elcom.metacen.contact.model.dto.FileDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AreasDTO extends BaseDTO<AreasDTO> {
    private String uuid;
    private String name;
    private String value;
    private String description;
    private String sideId;
    private List<FileDTO> imageLst;
    private List<FileDTO> fileAttachmentLst;
}
