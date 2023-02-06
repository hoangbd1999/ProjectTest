package com.elcom.metacen.report.model.dto;

import lombok.*;

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
public class DataAnalyzedCountVoiceToTextDTO {
    private String processType;
    private Integer totalSum;  // tổng
    private Integer untreated; // chưa xử lý
    private Integer processing; // đang xử lý
    private Integer successfulProcessing; // xử lý thành công
    private Integer errorHandling; //  xử lý lỗi

}
