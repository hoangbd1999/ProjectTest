package com.elcom.metacen.report.service.impl;

import com.elcom.metacen.report.model.DataAnalyzed;
import com.elcom.metacen.report.model.dto.DataAnalyzedFilterDTO;
import com.elcom.metacen.report.model.dto.DataAnalyzedRequestDTO;
import com.elcom.metacen.report.model.dto.DataAnalyzedRequestReportDTO;
import com.elcom.metacen.report.repository.DataAnalyzedRepository;
import com.elcom.metacen.report.service.DataAnalyzedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author Admin
 */
@Service
public class DataAnalyzedServiceImpl implements DataAnalyzedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataAnalyzedServiceImpl.class);

    @Autowired
    DataAnalyzedRepository dataAnalyzedRepository;

    @Override
    public DataAnalyzed insertLoggingProcess(DataAnalyzedRequestReportDTO dataAnalyzedRequestReportDTO) {
        return dataAnalyzedRepository.insertLoggingProcess(dataAnalyzedRequestReportDTO);
    }

    @Override
    public List<DataAnalyzedRequestDTO> filterReport(DataAnalyzedFilterDTO dataAnalyzedFilterDTO) {
        try {
            return dataAnalyzedRepository.filterReport(dataAnalyzedFilterDTO);
        } catch (Exception e) {
            LOGGER.error("filter failed >>> {}", e.toString());
            return null;
        }
    }
}
