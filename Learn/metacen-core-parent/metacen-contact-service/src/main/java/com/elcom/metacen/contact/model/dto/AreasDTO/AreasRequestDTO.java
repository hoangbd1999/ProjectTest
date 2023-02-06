package com.elcom.metacen.contact.model.dto.AreasDTO;

import com.elcom.metacen.contact.model.dto.FileDTO;
import com.elcom.metacen.contact.model.dto.ObjectRelationshipDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;
import java.util.List;

@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AreasRequestDTO implements Serializable {

    private String name;
    private String value;
    private String description;
    private String sideId;
    private List<FileDTO> imageLst;
    private List<FileDTO> fileAttachmentLst;
    private List<String> keywordLst;
    private List<ObjectRelationshipDTO> relationshipLst;
}
