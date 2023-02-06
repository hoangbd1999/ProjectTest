package com.elcom.metacen.mapping.data.controller;

import com.elcom.metacen.mapping.data.constant.Constant;
import com.elcom.metacen.mapping.data.model.MappingAisMetacen;
import com.elcom.metacen.mapping.data.model.dto.*;
import com.elcom.metacen.mapping.data.service.MappingAisMetacenService;
import com.elcom.metacen.mapping.data.validation.MappingAisValidation;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MappingAisMetacenController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingAisMetacenController.class);

    final int PAGE_DEFAULT = 0;
    final int SIZE_DEFAULT = 20;

    @Autowired
    private MappingAisMetacenService mappingAisMetacenService;

    public ResponseMessage insert(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) throws ParseException {
        LOGGER.info("Create MappingAis with request >>> {}", bodyParam);

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
            MappingAisRequestDTO mappingAisRequestDTO = buildMappingAisDTO(bodyParam);
            String validationMsg = new MappingAisValidation().validateMappingAis(mappingAisRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            MappingAisMetacen mappingAisMetacen = mappingAisMetacenService.save(mappingAisRequestDTO, dto.getUserName());
            if (mappingAisMetacen != null) {
                return new ResponseMessage(new MessageContent(mappingAisMetacen));
            } else {
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage update(Map<String, String> headerParam, Map<String, Object> bodyParam, String pathParam, String requestPath) {
        LOGGER.info("Update MappingAis{} with request >>> {}", pathParam, bodyParam);

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
            MappingAisMetacen mappingAisMetacen = mappingAisMetacenService.findByUuid(uuid);
            if (mappingAisMetacen == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
            }
            MappingAisRequestDTO mappingAisRequestDTO = buildMappingAisDTO(bodyParam);
            String validationMsg = new MappingAisValidation().validateMappingAis(mappingAisRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }
            MappingAisMetacen result = mappingAisMetacenService.updateMappingAis(mappingAisMetacen, mappingAisRequestDTO, dto.getUserName());
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
            MappingAisMetacen mappingAisMetacen = mappingAisMetacenService.findByUuid(uuid);
            if (mappingAisMetacen == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
            }
            return new ResponseMessage(new MessageContent(mappingAisMetacen));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage filterMappingAis(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
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
                MappingAisFilterDTO mappingAisFilterDTO = buildMappingAisFilterDTO(bodyParam);
                String validationMsg = new MappingAisValidation().validateFilterMappingAis(mappingAisFilterDTO);
                if (validationMsg != null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
                }

                Page<MappingAisResponseDTO> pagedResult = mappingAisMetacenService.findListMappingAis(mappingAisFilterDTO);
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
            MappingAisMetacen mappingAisMetacen = mappingAisMetacenService.findByUuid(uuid);
            if (mappingAisMetacen == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
            }
            mappingAisMetacenService.delete(mappingAisMetacen);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    private MappingAisFilterDTO buildMappingAisFilterDTO(Map<String, Object> bodyParam) {
        Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
        Integer size = bodyParam.get("size") != null ? (Integer) bodyParam.get("size") : 20;
        String sort = (String) bodyParam.getOrDefault("sort", "");
        String term = (String) bodyParam.getOrDefault("term", "");
        // String aisMmsi = (String) bodyParam.getOrDefault("aisMmsi", "");
        Integer aisMmsi = (bodyParam.get("aisMmsi") != "") && (bodyParam.get("aisMmsi") != null) ? ((Number) bodyParam.get("aisMmsi")).intValue() : null;
        List<String> objectTypes = bodyParam.get("objectTypes") != null ? (List<String>) bodyParam.get("objectTypes") : null;
        String objectId = (String) bodyParam.getOrDefault("objectId", "");

        MappingAisFilterDTO mappingAisFilterDTO = MappingAisFilterDTO.builder()
                .page(page)
                .size(size)
                .term(term)
                .sort(sort)
                .objectId(objectId)
                .aisMmsi(aisMmsi)
                .objectTypes(objectTypes)
                .build();
        return mappingAisFilterDTO;
    }

    private MappingAisRequestDTO buildMappingAisDTO(Map<String, Object> bodyParam) {
        Integer aisMmsi = (bodyParam.get("aisMmsi") != "") && (bodyParam.get("aisMmsi") != null) ? ((Number) bodyParam.get("aisMmsi")).intValue() : null;
        String aisShipName = (String) bodyParam.getOrDefault("aisShipName", "");
        String objectType = (String) bodyParam.getOrDefault("objectType", "");
        String objectId = (String) bodyParam.getOrDefault("objectId", "");
        String objectUuid = (String) bodyParam.getOrDefault("objectUuid", "");
        String objectName = (String) bodyParam.getOrDefault("objectName", "");

        MappingAisRequestDTO mappingAisRequestDTO = MappingAisRequestDTO.builder()
                .aisMmsi(aisMmsi)
                .aisShipName(aisShipName.trim())
                .objectType(objectType)
                .objectId(objectId)
                .objectUuid(objectUuid)
                .objectName(objectName)
                .build();
        return mappingAisRequestDTO;
    }
}
