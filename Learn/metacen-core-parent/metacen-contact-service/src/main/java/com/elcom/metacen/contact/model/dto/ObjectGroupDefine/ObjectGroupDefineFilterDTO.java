package com.elcom.metacen.contact.model.dto.ObjectGroupDefine;

import lombok.*;

import java.util.Date;

/**
 *
 * @author hoangbd
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ObjectGroupDefineFilterDTO {

    private Integer page;
    private Integer size;
    private String term;

}
