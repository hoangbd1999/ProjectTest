package com.elcom.metacen.report.service;

import com.elcom.metacen.report.model.DataAnalyzed;
import com.elcom.metacen.report.model.dto.DataAnalyzedFilterDTO;
import com.elcom.metacen.report.model.dto.DataAnalyzedRequestDTO;
import com.elcom.metacen.report.model.dto.DataAnalyzedRequestReportDTO;

import java.util.List;

/**
 *
 * @author Admin
 */
public interface DataAnalyzedService {

    DataAnalyzed insertLoggingProcess(DataAnalyzedRequestReportDTO dataAnalyzedRequestReportDTO);

    List<DataAnalyzedRequestDTO> filterReport(DataAnalyzedFilterDTO dataAnalyzedFilterDTO);
}
