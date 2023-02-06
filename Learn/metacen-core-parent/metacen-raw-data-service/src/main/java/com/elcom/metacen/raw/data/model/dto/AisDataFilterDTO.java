package com.elcom.metacen.raw.data.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class AisDataFilterDTO implements Serializable {

    private String fromTime;
    private String toTime;
    private String term; // Từ khóa tìm kiếm
    private Integer mmsi;
    private List<Integer> countryId; // Quốc gia
    private List<String> dataVendors; // nguồn dữ liệu
    private String imo;
    private Double longitude;
    private Double latitude;
//    private Integer page;
//    private Integer size;
//    private String sort;
    private List<Integer> processStatus; // Trạng thái
    private Integer limit;
}
