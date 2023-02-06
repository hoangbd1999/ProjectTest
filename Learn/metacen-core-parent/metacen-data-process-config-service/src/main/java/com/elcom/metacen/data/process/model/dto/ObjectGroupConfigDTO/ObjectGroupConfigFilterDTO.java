package com.elcom.metacen.data.process.model.dto.ObjectGroupConfigDTO;

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
public class ObjectGroupConfigFilterDTO {

    private Integer page;
    private Integer size;
    private String sort;
    private String coordinates;
    private String name;
    private List<Integer> isActive;
    private Date startTime;
    private Date endTime;
    private String term;

}
