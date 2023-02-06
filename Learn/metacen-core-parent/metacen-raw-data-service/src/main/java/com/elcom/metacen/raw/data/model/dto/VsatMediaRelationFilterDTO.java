package com.elcom.metacen.raw.data.model.dto;

import java.math.BigInteger;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class VsatMediaRelationFilterDTO {

    private Integer page;
    private Integer size;
    private String sort;
    private String term;
    private String fromTime;
    private String toTime;
    private List<Integer> dataSourceIds;
    private List<Integer> mediaTypeIds;
    private List<Integer> processStatusLst;
    private List<AdvanceFilterDTO> filter;
    private String dataVendor; // Nguồn dữ liệu
    private String dataSourceName; // Nguồn thu
    private String sourceIp; // IP nguồn
    private String sourceName; // Tên nguồn
    private String destIp; // IP đích
    private String destName; // Tên đích
    private String mediaTypeName; // Loại dữ liệu
    private String fileType; // Định dạng
    private BigInteger fileSize; // Dung lượng
    private Integer direction; // Chiều dữ liệu
}
