package com.elcom.metacen.data.process.controller;

import com.elcom.metacen.data.process.constant.Constant;
import com.elcom.metacen.data.process.model.DataProcessConfig;
import com.elcom.metacen.data.process.model.dto.*;
import com.elcom.metacen.data.process.validation.DataProcessConfigValidation;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.modelmapper.ModelMapper;
import com.elcom.metacen.data.process.service.DataProcessConfigService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class DataProcessConfigController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataProcessConfigController.class);
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static DateTimeFormatter defaultFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private DataProcessConfigService dataProcessConfigService;

    @Autowired
    protected ModelMapper modelMapper;

    Map<String, List<DataProcessConfig>> listDataProcess = new HashMap<>();

    public ResponseMessage insert(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) throws ParseException {
        LOGGER.info("Create data process with request >>> {}", bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "POST", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            if (bodyParam == null || bodyParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }

            DataProcessConfigRequestDTO dataProcessConfigRequestDTO = buildDataProcessConfigDTO(bodyParam);
            String validationMsg = new DataProcessConfigValidation().validateDataProcessConfig(dataProcessConfigRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            DataProcessConfig dataProcessConfig = dataProcessConfigService.save(dataProcessConfigRequestDTO, dto.getUserName());
            if (dataProcessConfig != null) {
                return new ResponseMessage(new MessageContent(dataProcessConfig));
            } else {
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage update(Map<String, String> headerParam, Map<String, Object> bodyParam, String pathParam, String requestPath) throws ParseException {
        LOGGER.info("Update data process id {} with request >>> {}", pathParam, bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "PUT", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            String uuid = pathParam;
            DataProcessConfig dataProcessConfig = dataProcessConfigService.findByUuid(uuid);
            if (dataProcessConfig == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
            }
            DataProcessConfigRequestDTO dataProcessConfigRequestDTO = buildDataProcessConfigDTO(bodyParam);
            String validationMsg = new DataProcessConfigValidation().validateDataProcessConfig(dataProcessConfigRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            DataProcessConfig result = dataProcessConfigService.update(dataProcessConfig, dataProcessConfigRequestDTO, dto.getUserName());
            if (result != null) {
                return new ResponseMessage(new MessageContent(result));
            } else {
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage getById(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DETAIL", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            String uuid = pathParam;
            DataProcessConfig dataProcessConfig = dataProcessConfigService.findByUuid(uuid);
            if (dataProcessConfig == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
            }
            return new ResponseMessage(new MessageContent(dataProcessConfig));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage getListDataProcessConfig(String requestPath, Map<String, String> headerParam) {
        try {
            if (listDataProcess.isEmpty()) {
                List<DataProcessConfig> result = dataProcessConfigService.getList();
                listDataProcess.put("listDataProcess", result);
                return new ResponseMessage(new MessageContent(result));
            } else {
                List<DataProcessConfig> list = new ArrayList<>(listDataProcess.get("listDataProcess"));
                return new ResponseMessage(new MessageContent(list));
            }
        } catch (Exception e) {
            LOGGER.error("get all fail", e);
            return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                    new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
        }
    }

    public ResponseMessage statusChange(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "POST", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            String uuid = (String) bodyParam.getOrDefault("uuid", "");
            Integer status = bodyParam.get("status") != null ? (Integer) bodyParam.get("status") : 1;

            DataProcessConfig dataProcessConfig = dataProcessConfigService.findByUuid(uuid);
            if (dataProcessConfig == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
            }
            dataProcessConfigService.statusChange(dataProcessConfig, status);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Thay đổi trạng thái thành công", null));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage delete(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DELETE", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            String uuid = pathParam;
            DataProcessConfig dataProcessConfig = dataProcessConfigService.findByUuid(uuid);
            if (dataProcessConfig == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
            }
            dataProcessConfigService.delete(dataProcessConfig);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage filterDataProcessConfig(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "LIST", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            if (bodyParam == null || bodyParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }
            try {
                DataProcessConfigFilterDTO dataProcessConfigFilterDTO = buildDataProcessConfigFilterDTO(bodyParam);
                String validationMsg = new DataProcessConfigValidation().validateFilterDataProcessConfig(dataProcessConfigFilterDTO);
                if (validationMsg != null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
                }
                Page<DataProcessConfigResponseDTO> pagedResult = dataProcessConfigService.findListDataProcessConfig(dataProcessConfigFilterDTO);
                return new ResponseMessage(new MessageContent(pagedResult.getContent(), pagedResult.getTotalElements()));
            } catch (Exception e) {
                LOGGER.error("Filter fail", e);
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    @Scheduled(cron = "${cron.time}")
    private void ClearData() {
        try {
            listDataProcess.clear();
        } catch (Exception e) {
            LOGGER.error(e.toString());
        }
    }

    private DataProcessConfigRequestDTO buildDataProcessConfigDTO(Map<String, Object> bodyParam) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String name = (String) bodyParam.getOrDefault("name", "");
        String dataType = (String) bodyParam.getOrDefault("dataType", "");
        String processType = (String) bodyParam.getOrDefault("processType", "");
        String dataVendor = (String) bodyParam.getOrDefault("dataVendor", "");
        Object detailConfig = bodyParam.getOrDefault("detailConfig", "");
        Date startTime = (bodyParam.get("startTime") != "") && (bodyParam.get("startTime") != null) ? dateFormat.parse((String) bodyParam.get("startTime")) : null;
        Date endTime = (bodyParam.get("endTime") != "") && (bodyParam.get("endTime") != null) ? dateFormat.parse((String) bodyParam.get("endTime")) : null;

        DataProcessConfigRequestDTO dataProcessConfigRequestDTO = DataProcessConfigRequestDTO.builder()
                .name(name)
                .dataType(dataType)
                .processType(processType)
                .dataVendor(dataVendor)
                .detailConfig(detailConfig)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        return dataProcessConfigRequestDTO;
    }

    private DataProcessConfigFilterDTO buildDataProcessConfigFilterDTO(Map<String, Object> bodyParam) throws ParseException {
        Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
        Integer size = bodyParam.get("size") != null ? (Integer) bodyParam.get("size") : 20;
        String sort = (String) bodyParam.getOrDefault("sort", "");
        String term = (String) bodyParam.getOrDefault("term", "");
        List<String> dataTypes = bodyParam.get("dataTypes") != null ? (List<String>) bodyParam.get("dataTypes") : null;
        List<String> dataVendors = bodyParam.get("dataVendors") != null ? (List<String>) bodyParam.get("dataVendors") : null;
        List<String> processTypes = bodyParam.get("processTypes") != null ? (List<String>) bodyParam.get("processTypes") : null;
        List<Integer> status = bodyParam.get("status") != null ? (List<Integer>) bodyParam.get("status") : null;
        Date startTime = (bodyParam.get("startTime") != "") && (bodyParam.get("startTime") != null) ? dateFormat.parse((String) bodyParam.get("startTime")) : null;
        Date endTime = (bodyParam.get("endTime") != "") && (bodyParam.get("endTime") != null) ? dateFormat.parse((String) bodyParam.get("endTime")) : null;

        DataProcessConfigFilterDTO dataProcessConfigFilterDTO = DataProcessConfigFilterDTO.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .term(term)
                .dataTypes(dataTypes)
                .dataVendors(dataVendors)
                .processTypes(processTypes)
                .status(status)
                .startTime(startTime)
                .endTime(endTime)
                .build();
        return dataProcessConfigFilterDTO;
    }

}
