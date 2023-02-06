package com.elcom.metacen.contact.model.dto.GroupDTO;

import lombok.*;

import java.util.UUID;


/**
 *
 * @author hoangbd
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GroupFilterDTO {
    private Integer page;
    private Integer size;
    private String term;
}
