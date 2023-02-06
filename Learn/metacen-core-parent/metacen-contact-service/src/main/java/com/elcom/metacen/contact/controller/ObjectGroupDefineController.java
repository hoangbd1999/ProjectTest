package com.elcom.metacen.contact.controller;

import com.elcom.metacen.contact.constant.Constant;
import com.elcom.metacen.contact.model.ObjectGroupDefine;
import com.elcom.metacen.contact.model.dto.ABACResponseDTO;
import com.elcom.metacen.contact.model.dto.AuthorizationResponseDTO;
import com.elcom.metacen.contact.model.dto.ObjectGroupDefine.ObjectGroupDefineFilterDTO;
import com.elcom.metacen.contact.model.dto.ObjectGroupDefine.ObjectGroupDefineMappingDTO;
import com.elcom.metacen.contact.model.dto.ObjectGroupDefine.ObjectGroupDefineRequestDTO;
import com.elcom.metacen.contact.model.dto.ObjectGroupDefine.ObjectGroupDefineResponseDTO;
import com.elcom.metacen.contact.service.ObjectGroupDefineService;
import com.elcom.metacen.contact.service.ObjectGroupMappingService;
import com.elcom.metacen.contact.validation.ObjectGroupDefineValidation;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
public class ObjectGroupDefineController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectGroupDefineController.class);

    @Autowired
    private ObjectGroupDefineService objectGroupDefineService;

    @Autowired
    private ObjectGroupMappingService objectGroupMappingService;

    @Autowired
    protected ModelMapper modelMapper;

    public ResponseMessage filterObjectGroupDefine(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
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
                ObjectGroupDefineFilterDTO objectGroupDefineFilterDTO = buildObjectGroupDefineFilterDTO(bodyParam);
                Page<ObjectGroupDefineResponseDTO> pagedResult = objectGroupDefineService.findListObjectGroupDefine(objectGroupDefineFilterDTO);
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

    public ResponseMessage insert(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Create object group define with request >>> {}", bodyParam);

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
            ObjectGroupDefineRequestDTO requestDTO = buildObjectGroupDefineDTO(bodyParam);
            String validationMsg = new ObjectGroupDefineValidation().validateObjectGroupDefine(requestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }
            ObjectGroupDefine findByName = objectGroupDefineService.findByName(requestDTO.getName());
            if (findByName != null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Tên nhóm bị trùng, vui lòng đặt tên khác",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Tên nhóm bị trùng, vui lòng đặt tên khác", null));
            }
            ObjectGroupDefine objectGroupDefine = objectGroupDefineService.save(requestDTO, dto.getUserName());
            if (objectGroupDefine != null) {
                return new ResponseMessage(new MessageContent(objectGroupDefine));
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
        LOGGER.info("Update object group id {} with request >>> {}", pathParam, bodyParam);

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
            ObjectGroupDefine objectGroupDefine = objectGroupDefineService.findByUuid(uuid);
            if (objectGroupDefine == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
            }
            ObjectGroupDefineRequestDTO objectGroupDefineRequestDTO = buildObjectGroupDefineDTO(bodyParam);
            String validationMsg = new ObjectGroupDefineValidation().validateObjectGroupDefine(objectGroupDefineRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }
            ObjectGroupDefine findByName = objectGroupDefineService.findByName(objectGroupDefineRequestDTO.getName());
            ObjectGroupDefine result = new ObjectGroupDefine();
            if (findByName == null) {
                result = objectGroupDefineService.update(objectGroupDefine, objectGroupDefineRequestDTO, dto.getUserName());
            } else if (findByName != null) {
                if (findByName.getName().equalsIgnoreCase(objectGroupDefine.getName())) {
                    result = objectGroupDefineService.update(objectGroupDefine, objectGroupDefineRequestDTO, dto.getUserName());
                } else {
                    return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Tên nhóm bị trùng, vui lòng đặt tên khác",
                            new MessageContent(HttpStatus.NOT_FOUND.value(), "Tên nhóm bị trùng, vui lòng đặt tên khác", null));
                }
            }
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

    public ResponseMessage statusMainObjectChange(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
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
            Boolean isMainObject = bodyParam.get("isMainObject") != null ? (Boolean) bodyParam.get("isMainObject") : true;
            String objectUuid = (String) bodyParam.getOrDefault("objectUuid", "");

            ObjectGroupDefine objectGroupDefine = objectGroupDefineService.findByUuid(uuid);
            if (objectGroupDefine == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
            }
            objectGroupDefineService.statusChange(objectGroupDefine, isMainObject, objectUuid);
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
            ObjectGroupDefine objectGroupDefine = objectGroupDefineService.findByUuid(uuid);
            if (objectGroupDefine == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
            }
            objectGroupDefineService.delete(objectGroupDefine);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }


    private ObjectGroupDefineFilterDTO buildObjectGroupDefineFilterDTO(Map<String, Object> bodyParam) {
        Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
        Integer size = bodyParam.get("size") != null ? (Integer) bodyParam.get("size") : 20;
        String term = (String) bodyParam.getOrDefault("term", "");

        ObjectGroupDefineFilterDTO objectGroupDefineFilterDTO = ObjectGroupDefineFilterDTO.builder()
                .page(page)
                .size(size)
                .term(term)
                .build();
        return objectGroupDefineFilterDTO;
    }

    private ObjectGroupDefineRequestDTO buildObjectGroupDefineDTO(Map<String, Object> bodyParam) {
        ObjectMapper mapper = new ObjectMapper();
        String name = (String) bodyParam.getOrDefault("name", "");
        String note = (String) bodyParam.getOrDefault("note", "");
        List<ObjectGroupDefineMappingDTO> objects = null;
        if (bodyParam.get("objects") != null) {
            objects = mapper.convertValue(
                    bodyParam.get("objects"),
                    new TypeReference<List<ObjectGroupDefineMappingDTO>>() {
                    });
        }

        ObjectGroupDefineRequestDTO objectGroupDefineRequestDTO = ObjectGroupDefineRequestDTO.builder()
                .name(name)
                .note(note)
                .objects(objects)
                .build();

        return objectGroupDefineRequestDTO;
    }

}
