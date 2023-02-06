package com.elcom.metacen.contact.model.dto.GroupDTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GroupObjectDTO extends GroupObjectMappingDTO {
    private String sideId;
    private String name;
    private String description;
}
