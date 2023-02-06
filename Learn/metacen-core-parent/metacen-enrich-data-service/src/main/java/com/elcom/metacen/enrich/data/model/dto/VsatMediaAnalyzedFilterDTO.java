package com.elcom.metacen.enrich.data.model.dto;

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
public class VsatMediaAnalyzedFilterDTO {

    private Integer page;
    private Integer size;
    private String sort;
    private String term;
    private String fromTime;
    private String toTime;
    private List<String> dataVendorLst;
    private List<Integer> dataSourceIds;
    private List<Integer> mediaTypeIds;
    private List<Integer> processStatusLst;
    private List<AdvanceFilterDTO> filter;
    private String uuid;
    private String dataVendor; // Nguồn dữ liệu
    private String dataSourceName; // Nguồn thu
    private String sourceIp; // IP nguồn
    private Long sourcePort; // Port nguồn
    private Long sourceId; // ID nguồn
    private String sourceName; // Tên nguồn
    private String destIp; // IP đích
    private Long destPort; // Port đích
    private Long destId; // ID đích
    private String destName; // Tên đích
    private String mediaTypeName; // Loại dữ liệu
    private String fileType; // Định dạng
    private String eventTime; // Thời gian
    private String processTime; // Thời gian xử lý
}
