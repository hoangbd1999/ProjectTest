package com.elcom.metacen.contact.controller;

import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.validation.ObjectTypeValidation;
import com.elcom.metacen.contact.model.ObjectTypes;
import com.elcom.metacen.contact.service.ObjectTypesService;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hoangbd
 */
@Controller
public class ObjectTypesController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectTypesController.class);

    @Autowired
    ObjectTypesService objectTypesService;

    public ResponseMessage insertObjectType(String requestPath, Map<String, Object> bodyParam, Map<String, String> headerParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "POST", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            ObjectTypesRequestDTO objectTypesRequestDTO = buildObjectTypesRequestDTO(bodyParam);

            String validationMsg = new ObjectTypeValidation().validateObjectType(objectTypesRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }
            ObjectTypes objectTypes = this.objectTypesService
                    .findByTypeNameAndTypeCodeObjectType(objectTypesRequestDTO.getTypeName(), objectTypesRequestDTO.getTypeCode());
            if (objectTypes != null) {
                String msg = "Đã tồn tại loại tàu '" + objectTypesRequestDTO.getTypeName() + "' - mã '" + objectTypesRequestDTO.getTypeCode() + "' trên hệ thống";
                return new ResponseMessage(HttpStatus.OK.value(), msg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), msg, null));
            }

            objectTypes = objectTypesService.save(objectTypesRequestDTO);
            if (objectTypes != null) {
                return new ResponseMessage(new MessageContent(entityToDto(objectTypes)));
            } else {
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage updateObjectType(String requestPath, Map<String, Object> bodyParam, Map<String, String> headerParam, String pathParam) {
        LOGGER.info("Update ObjectType id {} with request >>> {}", pathParam, bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "PUT", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            String typeId = pathParam;
            ObjectTypes objectTypes = objectTypesService.findByTypeId(typeId);
            if (objectTypes == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "ObjectType không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "ObjectType không tồn tại", null));
            }
            ObjectTypesRequestDTO objectTypesRequestDTO = buildObjectTypesRequestDTO(bodyParam);
            String validationMsg = new ObjectTypeValidation().validateObjectType(objectTypesRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }
            ObjectTypes result = objectTypesService.updateObjectType(objectTypes, objectTypesRequestDTO);
            if (result != null) {
                return new ResponseMessage(new MessageContent(entityToDto(result)));
            } else {
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage getObjectTypeById(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DETAIL", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            ObjectTypes objectTypes = objectTypesService.findByTypeId(pathParam);
            if (objectTypes == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "ObjectType không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "ObjectType không tồn tại", null));
            }
            return new ResponseMessage(new MessageContent(objectTypes));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage deleteObjectType(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DELETE", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            ObjectTypes objectTypes = objectTypesService.findByTypeId(pathParam);
            if (objectTypes == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "ObjectType không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "ObjectType không tồn tại", null));
            }
            this.objectTypesService.delete(objectTypes);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    private ObjectTypesRequestDTO buildObjectTypesRequestDTO(Map<String, Object> bodyParam) {
        String typeName = (String) bodyParam.getOrDefault("typeName", "");
        String typeCode = (String) bodyParam.getOrDefault("typeCode", "");
        String typeDesc = (String) bodyParam.getOrDefault("typeDesc", "");
        String typeObject = (String) bodyParam.getOrDefault("typeObject", "VESSEL");
        ObjectTypesRequestDTO objectTypesRequestDTO = ObjectTypesRequestDTO.builder()
                .typeName(typeName)
                .typeCode(typeCode)
                .typeDesc(typeDesc)
                .typeObject(typeObject)
                .build();

        return objectTypesRequestDTO;
    }

    private ObjectTypesDTO entityToDto(ObjectTypes objectTypes) {
        ObjectTypesDTO objectTypesDTO = ObjectTypesDTO.builder()
                .id(objectTypes.getId())
                .typeId(objectTypes.getTypeId())
                .typeName(objectTypes.getTypeName())
                .typeCode(objectTypes.getTypeCode())
                .typeDesc(objectTypes.getTypeDesc())
                .typeObject(objectTypes.getTypeObject())
                .createdBy(objectTypes.getModifiedBy())
                .createdDate(objectTypes.getCreatedDate())
                .modifiedDate(objectTypes.getModifiedDate())
                .modifiedBy(objectTypes.getModifiedBy())
                .build();

        return objectTypesDTO;
    }

}
