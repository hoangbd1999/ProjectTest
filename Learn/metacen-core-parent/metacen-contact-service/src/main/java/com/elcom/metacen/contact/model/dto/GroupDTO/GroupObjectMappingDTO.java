package com.elcom.metacen.contact.model.dto.GroupDTO;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

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
public class GroupObjectMappingDTO implements Serializable {
    private String objectId;
    private String objectType;
}
