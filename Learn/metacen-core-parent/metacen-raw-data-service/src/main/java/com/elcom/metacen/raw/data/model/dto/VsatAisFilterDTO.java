package com.elcom.metacen.raw.data.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

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
public class VsatAisFilterDTO {

    private String fromTime;
    private String toTime;
    private String term; // Từ khóa tìm kiếm
    private Integer mmsi;
    private List<Integer> countryId; // Quốc gia
    private List<String> dataVendors; // nguồn dữ liệu
    
    private String sourceIps; //  ip nguồn
    private String destIps; // ip đích
    private List<Integer> dataSourceId; // Nguồn thu
    private List<Integer> typeId; // Loại tàu
    
    private List<Integer> processStatus; // Trạng thái
    private Integer limit;

}
