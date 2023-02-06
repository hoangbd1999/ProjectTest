package com.elcom.metacen.contact.model.dto.AreasDTO;

import com.elcom.metacen.contact.model.dto.BaseDTO;
import com.elcom.metacen.contact.model.dto.FileDTO;
import com.elcom.metacen.contact.model.dto.KeywordDTO;
import com.elcom.metacen.contact.model.dto.ObjectRelationshipDeltailDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AreasResponseDTO extends BaseDTO<AreasResponseDTO> {

    private String uuid;
    private String name;
    private String value;
    private String description;
    private String sideId;
    private String sideName;
    private List<FileDTO> imageLst;
    private List<FileDTO> fileAttachmentLst;
    private List<ObjectRelationshipDeltailDTO> relationshipLst;
    private List<String> keywordUuidLst;
    private List<KeywordDTO> keywordLst;
}
