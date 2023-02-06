package com.elcom.metacen.contact.model.dto;

import lombok.*;

import java.util.List;

/**
 *
 * @author Admin
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectRelationshipDetailDTO {

    private ObjectDetailDTO object;
    private List<ObjectRelationshipDTO> relationshipLst;

}
