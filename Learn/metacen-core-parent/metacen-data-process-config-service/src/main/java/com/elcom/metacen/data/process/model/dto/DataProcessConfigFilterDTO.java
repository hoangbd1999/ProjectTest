package com.elcom.metacen.data.process.model.dto;

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
public class DataProcessConfigFilterDTO {

    private Integer page;
    private Integer size;
    private String sort;
    private List<String> dataTypes;
    private List<String> dataVendors;
    private List<String> processTypes;
    private List<Integer> status;
    private Date startTime;
    private Date endTime;
    private String term;

}
