package com.elcom.metacen.contact.model.dto.ObjectGroup;

import lombok.*;

import java.util.Date;
import java.util.List;

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
public class ObjectGroupUnconfirmedFilterDTO {

    private Integer page;
    private Integer size;
    private Date fromTime;
    private Date toTime;
    private String term;
    private String termObject;
    private String configName;

}
