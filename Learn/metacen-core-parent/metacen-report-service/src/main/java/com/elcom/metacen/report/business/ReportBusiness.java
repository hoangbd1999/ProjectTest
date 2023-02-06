package com.elcom.metacen.report.business;

import com.elcom.metacen.enums.ProcessTypes;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.report.constant.Constant;
import com.elcom.metacen.report.model.dto.*;
import com.elcom.metacen.report.service.DataAnalyzedService;

import com.elcom.metacen.report.validation.ReportValidation;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ReportBusiness extends BaseBusiness {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportBusiness.class);

    @Autowired
    private DataAnalyzedService dataAnalyzedService;

    @Autowired
    ModelMapper modelMapper;

    // Satellite Image
    public ResponseMessage filterReport(Map<String, Object> bodyParam, Map<String, String> headerParam, String requestPath) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        //  Check ABAC
        Map<String, Object> body = new HashMap<>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "LIST", dto.getUuid(), requestPath);
        if (abacStatus == null || !abacStatus.getStatus()) {
            return new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
        if (bodyParam == null || bodyParam.isEmpty()) {
            return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
        }
        try {
            DataAnalyzedFilterDTO dataAnalyzedFilterDTO = buildReportFilterRequest(bodyParam);
            String validationMsg = new ReportValidation().ValidationFilterReport(dataAnalyzedFilterDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.OK.value(), validationMsg, new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            List<DataAnalyzedRequestDTO> results = dataAnalyzedService.filterReport(dataAnalyzedFilterDTO);
            List<DataAnalyzedCountSatelliteDTO> listResults = listData(results);
            if (listResults == null) {
                return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
            }

            return new ResponseMessage(new MessageContent(listResults));
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
            return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), new MessageContent(HttpStatus.OK.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
        }
    }

    private DataAnalyzedFilterDTO buildReportFilterRequest(Map<String, Object> bodyParam) {
        try {
            String fromTime = (String) bodyParam.getOrDefault("fromTime", "");
            String toTime = (String) bodyParam.getOrDefault("toTime", "");
            List<String> processTypeLst = bodyParam.get("processTypeLst") != null ? (List<String>) bodyParam.get("processTypeLst") : null;

            DataAnalyzedFilterDTO dataAnalyzedFilterDTO = DataAnalyzedFilterDTO.builder()
                    .fromTime(fromTime)
                    .toTime(toTime)
                    .processTypeLst(processTypeLst)
                    .build();

            return dataAnalyzedFilterDTO;
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }

        return null;
    }

    private List<DataAnalyzedCountSatelliteDTO> listData(List<DataAnalyzedRequestDTO> results) {
        List<DataAnalyzedCountSatelliteDTO> listData = new ArrayList<>();
        DataAnalyzedCountSatelliteDTO dataAnalyzedCountSatelliteDTO = new DataAnalyzedCountSatelliteDTO();
        DataAnalyzedCountSatelliteDTO dataAnalyzedCountSatelliteCompareDTO = new DataAnalyzedCountSatelliteDTO();
        DataAnalyzedCountMediaDTO dataAnalyzedCountMediaDTO = new DataAnalyzedCountMediaDTO();
        DataAnalyzedCountVoiceToTextDTO dataAnalyzedCountVoiceToTextDTO = new DataAnalyzedCountVoiceToTextDTO();
        DataAnalyzedCountImageToTextDTO dataAnalyzedCountImageToTextDTO = new DataAnalyzedCountImageToTextDTO();
        int totalSumSatellite = 0;
        int untreatedSatellite = 0;
        int processingSatellite = 0;
        int successfulProcessingSatellite = 0;
        int errorHandlingSatellite = 0;

        int totalSumSatelliteCompare = 0;
        int untreatedSatelliteCompare = 0;
        int processingSatelliteCompare = 0;
        int successfulProcessingSatelliteCompare = 0;
        int errorHandlingSatelliteCompare = 0;

        int totalSumMedia = 0;
        int untreatedMedia = 0;
        int processingMedia = 0;
        int successfulProcessingMedia = 0;
        int errorHandlingMedia = 0;

        int totalSumVoiceToText = 0;
        int untreatedVoiceToText = 0;
        int processingVoiceToText = 0;
        int successfulProcessingVoiceToText = 0;
        int errorHandlingVoiceToText = 0;

        int totalSumImageToText = 0;
        int untreatedImageToText = 0;
        int processingImageToText = 0;
        int successfulProcessingImageToText = 0;
        int errorHandlingImageToText = 0;
        for (int i = 0; i < results.size(); i++) {
            if (results.get(i).getProcessType().equals(ProcessTypes.SATELLITE_ANALYTICS_RAW.type())) {
                dataAnalyzedCountSatelliteDTO.setProcessType(ProcessTypes.SATELLITE_ANALYTICS_RAW.type());
                totalSumSatellite++;
                if (results.get(i).getProcessStatus() == 0) {
                    untreatedSatellite++;
                } else if (results.get(i).getProcessStatus() == 1) {
                    processingSatellite++;
                } else if (results.get(i).getProcessStatus() == 2) {
                    successfulProcessingSatellite++;
                } else if (results.get(i).getProcessStatus() == 3) {
                    errorHandlingSatellite++;
                }
            }
            if (results.get(i).getProcessType().equals(ProcessTypes.SATELLITE_ANALYTICS_COMPARE.type())) {
                dataAnalyzedCountSatelliteCompareDTO.setProcessType(ProcessTypes.SATELLITE_ANALYTICS_COMPARE.type());
                totalSumSatelliteCompare++;
                if (results.get(i).getProcessStatus() == 0) {
                    untreatedSatelliteCompare++;
                } else if (results.get(i).getProcessStatus() == 1) {
                    processingSatelliteCompare++;
                } else if (results.get(i).getProcessStatus() == 2) {
                    successfulProcessingSatelliteCompare++;
                } else if (results.get(i).getProcessStatus() == 3) {
                    errorHandlingSatelliteCompare++;
                }
            }
            if (results.get(i).getProcessType().equals(ProcessTypes.VSAT_MEDIA_ANALYTICS.type())) {
                dataAnalyzedCountMediaDTO.setProcessType(ProcessTypes.VSAT_MEDIA_ANALYTICS.type());
                totalSumMedia++;
                if (results.get(i).getProcessStatus() == 0) {
                    untreatedMedia++;
                } else if (results.get(i).getProcessStatus() == 1) {
                    processingMedia++;
                } else if (results.get(i).getProcessStatus() == 2) {
                    successfulProcessingMedia++;
                } else if (results.get(i).getProcessStatus() == 3) {
                    errorHandlingMedia++;
                }
            }
            if (results.get(i).getProcessType().equals(ProcessTypes.VOICE_TO_TEXT.type())) {
                dataAnalyzedCountVoiceToTextDTO.setProcessType(ProcessTypes.VOICE_TO_TEXT.type());
                totalSumVoiceToText++;
                if (results.get(i).getProcessStatus() == 0) {
                    untreatedVoiceToText++;
                } else if (results.get(i).getProcessStatus() == 1) {
                    processingVoiceToText++;
                } else if (results.get(i).getProcessStatus() == 2) {
                    successfulProcessingVoiceToText++;
                } else if (results.get(i).getProcessStatus() == 3) {
                    errorHandlingVoiceToText++;
                }
            }
            if (results.get(i).getProcessType().equals(ProcessTypes.IMAGE_TO_TEXT.type())) {
                dataAnalyzedCountImageToTextDTO.setProcessType(ProcessTypes.IMAGE_TO_TEXT.type());
                totalSumImageToText++;
                if (results.get(i).getProcessStatus() == 0) {
                    untreatedImageToText++;
                } else if (results.get(i).getProcessStatus() == 1) {
                    processingImageToText++;
                } else if (results.get(i).getProcessStatus() == 2) {
                    successfulProcessingImageToText++;
                } else if (results.get(i).getProcessStatus() == 3) {
                    errorHandlingImageToText++;
                }
            }
            dataAnalyzedCountSatelliteDTO.setTotalSum(totalSumSatellite);
            dataAnalyzedCountSatelliteDTO.setUntreated(untreatedSatellite);
            dataAnalyzedCountSatelliteDTO.setProcessing(processingSatellite);
            dataAnalyzedCountSatelliteDTO.setSuccessfulProcessing(successfulProcessingSatellite);
            dataAnalyzedCountSatelliteDTO.setErrorHandling(errorHandlingSatellite);

            dataAnalyzedCountSatelliteCompareDTO.setTotalSum(totalSumSatelliteCompare);
            dataAnalyzedCountSatelliteCompareDTO.setUntreated(untreatedSatelliteCompare);
            dataAnalyzedCountSatelliteCompareDTO.setProcessing(processingSatelliteCompare);
            dataAnalyzedCountSatelliteCompareDTO.setSuccessfulProcessing(successfulProcessingSatelliteCompare);
            dataAnalyzedCountSatelliteCompareDTO.setErrorHandling(errorHandlingSatelliteCompare);

            dataAnalyzedCountMediaDTO.setTotalSum(totalSumMedia);
            dataAnalyzedCountMediaDTO.setUntreated(untreatedMedia);
            dataAnalyzedCountMediaDTO.setProcessing(processingMedia);
            dataAnalyzedCountMediaDTO.setSuccessfulProcessing(successfulProcessingMedia);
            dataAnalyzedCountMediaDTO.setErrorHandling(errorHandlingMedia);

            dataAnalyzedCountVoiceToTextDTO.setTotalSum(totalSumVoiceToText);
            dataAnalyzedCountVoiceToTextDTO.setUntreated(untreatedVoiceToText);
            dataAnalyzedCountVoiceToTextDTO.setProcessing(processingVoiceToText);
            dataAnalyzedCountVoiceToTextDTO.setSuccessfulProcessing(successfulProcessingVoiceToText);
            dataAnalyzedCountVoiceToTextDTO.setErrorHandling(errorHandlingVoiceToText);

            dataAnalyzedCountImageToTextDTO.setTotalSum(totalSumImageToText);
            dataAnalyzedCountImageToTextDTO.setUntreated(untreatedImageToText);
            dataAnalyzedCountImageToTextDTO.setProcessing(processingImageToText);
            dataAnalyzedCountImageToTextDTO.setSuccessfulProcessing(successfulProcessingImageToText);
            dataAnalyzedCountImageToTextDTO.setErrorHandling(errorHandlingImageToText);
        }
        if (dataAnalyzedCountSatelliteDTO.getProcessType() != null) {
            listData.add(dataAnalyzedCountSatelliteDTO);
        }
        if (dataAnalyzedCountSatelliteCompareDTO.getProcessType() != null) {
            listData.add(dataAnalyzedCountSatelliteCompareDTO);
        }
        DataAnalyzedCountSatelliteDTO countMedia = modelMapper.map(dataAnalyzedCountMediaDTO, DataAnalyzedCountSatelliteDTO.class);
        if (countMedia.getProcessType() != null) {
            listData.add(countMedia);
        }
        DataAnalyzedCountSatelliteDTO countVoiceToText = modelMapper.map(dataAnalyzedCountVoiceToTextDTO, DataAnalyzedCountSatelliteDTO.class);
        if (countVoiceToText.getProcessType() != null) {
            listData.add(countVoiceToText);
        }
        DataAnalyzedCountSatelliteDTO countImageToText = modelMapper.map(dataAnalyzedCountImageToTextDTO, DataAnalyzedCountSatelliteDTO.class);
        if (countImageToText.getProcessType() != null) {
            listData.add(countImageToText);
        }

        return listData;
    }

}
