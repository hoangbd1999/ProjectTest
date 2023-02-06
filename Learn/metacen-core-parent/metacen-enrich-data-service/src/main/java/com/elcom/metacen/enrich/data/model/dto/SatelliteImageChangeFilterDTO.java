package com.elcom.metacen.enrich.data.model.dto;

import lombok.*;

import java.util.List;

/**
 *
 * @author Admin
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SatelliteImageChangeFilterDTO {

    private Integer page;
    private Integer size;
    private String sort;
    private String fromTime;
    private String toTime;
    private List<String> tileNumberLst;
    private List<String> createdByLst;
    private List<Integer> processStatusLst;
    private String tileNumber;  // Mã vùng
    private String timeFileOrigin; // Mốc thời gian 1
    private String timeFileCompare; // Mốc thời gian 2
    private String timeReceiveResult; // Thời gian nhận kết quả
    private String createdBy; // Người tạo
    private String ingestTime; // Thời gian tạo
    private Integer processStatus; // Trạng thái
    private String term;
}
