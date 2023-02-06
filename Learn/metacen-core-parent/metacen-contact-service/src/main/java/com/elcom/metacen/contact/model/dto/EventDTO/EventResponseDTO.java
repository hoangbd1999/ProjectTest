package com.elcom.metacen.contact.model.dto.EventDTO;

import com.elcom.metacen.contact.model.dto.BaseDTO;
import com.elcom.metacen.contact.model.dto.FileDTO;
import com.elcom.metacen.contact.model.dto.KeywordDTO;
import com.elcom.metacen.contact.model.dto.ObjectRelationshipDeltailDTO;
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
public class EventResponseDTO extends BaseDTO<EventResponseDTO> {

    private String uuid;
    private String name;
    private Date startTime;
    private Date stopTime;
    private String description;
    private String sideId;
    private String sideName;
    private String area;
    private List<FileDTO> imageLst;
    private List<FileDTO> fileAttachmentLst;
    private List<ObjectRelationshipDeltailDTO> relationshipLst;
    private List<String> keywordUuidLst;
    private List<KeywordDTO> keywordLst;
}


