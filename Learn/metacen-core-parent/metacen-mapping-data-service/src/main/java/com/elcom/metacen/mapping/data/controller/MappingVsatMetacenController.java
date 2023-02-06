package com.elcom.metacen.mapping.data.controller;

import com.elcom.metacen.mapping.data.constant.Constant;
import com.elcom.metacen.mapping.data.model.MappingAisMetacen;
import com.elcom.metacen.mapping.data.model.MappingVsatMetacen;
import com.elcom.metacen.mapping.data.model.dto.*;
import com.elcom.metacen.mapping.data.validation.MappingAisValidation;
import com.elcom.metacen.mapping.data.validation.MappingVsatValidation;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.modelmapper.ModelMapper;
import com.elcom.metacen.mapping.data.service.MappingVsatMetacenService;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MappingVsatMetacenController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingVsatMetacenController.class);

    @Autowired
    private MappingVsatMetacenService mappingVsatMetacenService;

    @Autowired
    protected ModelMapper modelMapper;

    public ResponseMessage insert(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) throws ParseException {
        LOGGER.info("Create MappingVsat with request >>> {}", bodyParam);

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

            MappingVsatRequestDTO mappingVsatRequestDTO = buildMappingVsatDTO(bodyParam);
            String validationMsg = new MappingVsatValidation().validateMappingVsat(mappingVsatRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            MappingVsatMetacen mappingVsatMetacen = mappingVsatMetacenService.save(mappingVsatRequestDTO, dto.getUserName());
            if (mappingVsatMetacen != null) {
                return new ResponseMessage(new MessageContent(mappingVsatMetacen));
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
        LOGGER.info("Update MappingVsat id {} with request >>> {}", pathParam, bodyParam);

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
            MappingVsatMetacen mappingVsatMetacen = mappingVsatMetacenService.findByUuid(uuid);
            if (mappingVsatMetacen == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
            }
            MappingVsatRequestDTO mappingVsatRequestDTO = buildMappingVsatDTO(bodyParam);
            String validationMsg = new MappingVsatValidation().validateMappingVsat(mappingVsatRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            MappingVsatMetacen result = mappingVsatMetacenService.updateMappingVsat(mappingVsatMetacen, mappingVsatRequestDTO, dto.getUserName());
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

    public ResponseMessage updateNameObjectInternal(Map<String, Object> bodyParam) {

        String objectUuid = (String) bodyParam.get("objectUuid");
        String objectName = (String) bodyParam.get("objectName");
        List<MappingVsatMetacen> mappingVsatMetacen = mappingVsatMetacenService.findByObjectUuid(objectUuid);
        if (mappingVsatMetacen == null) {
            return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                    new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
        }
        MappingVsatRequestDTO mappingVsatRequestDTO = buildMappingVsatDTO(bodyParam);
        String validationMsg = new MappingVsatValidation().validateMappingVsat(mappingVsatRequestDTO);
        if (validationMsg != null) {
            return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                    new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
        }
        List<MappingVsatMetacen> result = mappingVsatMetacenService.updateNameObjectInternal(mappingVsatMetacen, objectName);
        if (result != null) {
            return new ResponseMessage(new MessageContent(result));
        } else {
            return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                    new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
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
            MappingVsatMetacen mappingVsatMetacen = mappingVsatMetacenService.findByUuid(uuid);
            if (mappingVsatMetacen == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
            }
            return new ResponseMessage(new MessageContent(mappingVsatMetacen));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage filterMappingVsat(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
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
                MappingVsatFilterDTO mappingVsatFilterDTO = buildMappingVsatFilterDTO(bodyParam);
                String validationMsg = new MappingVsatValidation().validateFilterMappingVsat(mappingVsatFilterDTO);
                if (validationMsg != null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
                }
                Page<MappingVsatResponseDTO> pagedResult = mappingVsatMetacenService.findListMappingVsat(mappingVsatFilterDTO);
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

    private MappingVsatRequestDTO buildMappingVsatDTO(Map<String, Object> bodyParam) {
        Integer vsatDataSourceId = (bodyParam.get("vsatDataSourceId") != "") && (bodyParam.get("vsatDataSourceId") != null) ? ((Number) bodyParam.getOrDefault("vsatDataSourceId", "")).intValue() : 0;
        String vsatDataSourceName = (String) bodyParam.getOrDefault("vsatDataSourceName", "");
        String vsatIpAddress = (String) bodyParam.getOrDefault("vsatIpAddress", "");
        String objectType = (String) bodyParam.getOrDefault("objectType", "");
        String objectId = (String) bodyParam.getOrDefault("objectId", "");
        String objectUuid = (String) bodyParam.getOrDefault("objectUuid", "");
        String objectName = (String) bodyParam.getOrDefault("objectName", "");

        MappingVsatRequestDTO mappingVsatRequestDTO = MappingVsatRequestDTO.builder()
                .vsatDataSourceId(vsatDataSourceId)
                .vsatDataSourceName(vsatDataSourceName)
                .vsatIpAddress(vsatIpAddress.trim())
                .objectType(objectType)
                .objectId(objectId)
                .objectUuid(objectUuid)
                .objectName(objectName)
                .build();

        return mappingVsatRequestDTO;
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
            MappingVsatMetacen mappingVsatMetacen = mappingVsatMetacenService.findByUuid(uuid);
            if (mappingVsatMetacen == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
            }
            mappingVsatMetacenService.delete(mappingVsatMetacen);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage isMappingExist(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        //Authenticate
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DETAIL", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            MappingVsatRequestDTO mappingVsatRequestDTO = buildMappingVsatDTO(bodyParam);

            String validationMsg = new MappingVsatValidation().validateMappingVsat(mappingVsatRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            MappingVsatMetacen mappingVsatMetacen = mappingVsatMetacenService.checkExistMapping(mappingVsatRequestDTO);
            String msg = null;
            if (mappingVsatMetacen == null) {
                return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Không tồn tại ánh xạ của Nguồn thu và IP này", null));
            } else {
                return new ResponseMessage(new MessageContent(HttpStatus.NOT_FOUND.value(), "Đã tồn tại ánh xạ của Nguồn thu và IP này", null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    private MappingVsatFilterDTO buildMappingVsatFilterDTO(Map<String, Object> bodyParam) {
        Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
        Integer size = bodyParam.get("size") != null ? (Integer) bodyParam.get("size") : 20;
        String sort = (String) bodyParam.getOrDefault("sort", "");
        String term = (String) bodyParam.getOrDefault("term", "");
        List<Integer> vsatDataSourceIds = bodyParam.get("vsatDataSourceIds") != null ? (List<Integer>) bodyParam.get("vsatDataSourceIds") : null;
        String vsatIpAddress = (String) bodyParam.getOrDefault("vsatIpAddress", "");
        List<String> objectTypes = bodyParam.get("objectTypes") != null ? (List<String>) bodyParam.get("objectTypes") : null;
        String objectId = (String) bodyParam.getOrDefault("objectId", "");

        MappingVsatFilterDTO mappingVsatFilterDTO = MappingVsatFilterDTO.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .term(term)
                .vsatDataSourceIds(vsatDataSourceIds)
                .vsatIpAddress(vsatIpAddress.trim())
                .objectTypes(objectTypes)
                .objectId(objectId)
                .build();
        return mappingVsatFilterDTO;
    }

    public ResponseMessage isMappingExistByObjectUuid(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        MappingVsatRequestDTO mappingVsatRequestDTO = buildMappingVsatDTO(bodyParam);

        String validationMsg = new MappingVsatValidation().validateMappingVsat(mappingVsatRequestDTO);
        if (validationMsg != null) {
            return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                    new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
        }

        MappingVsatMetacen mappingVsatMetacen = mappingVsatMetacenService.checkExistMappingByObjectUuid(mappingVsatRequestDTO);
        String msg = null;
        if (mappingVsatMetacen == null) {
            return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Không tồn tại ánh xạ của Object Uuid này", null);
        } else {
            return new ResponseMessage(HttpStatus.OK.value(), "Tồn tại ánh xạ của Object Uuid này", null);
        }
    }
}
