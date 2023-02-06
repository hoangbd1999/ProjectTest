package com.elcom.metacen.report.model.dto;

import lombok.*;

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
public class DataAnalyzedCountMediaDTO {
    private String processType;
    private Integer totalSum;  // tổng
    private Integer untreated; // chưa xử lý 0
    private Integer processing; // đang xử lý 1
    private Integer successfulProcessing; // xử lý thành công 2
    private Integer errorHandling; //  xử lý lỗi 3

}
