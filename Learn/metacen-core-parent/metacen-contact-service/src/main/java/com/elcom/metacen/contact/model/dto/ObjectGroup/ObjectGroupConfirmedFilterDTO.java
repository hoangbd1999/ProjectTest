package com.elcom.metacen.contact.model.dto.ObjectGroup;

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
public class ObjectGroupConfirmedFilterDTO {

    private Integer page;
    private Integer size;
    private Date fromTime;
    private Date toTime;
    private String name;
    private String configName;
    private String term;

}
