package com.elcom.metacen.contact.controller;

import com.elcom.metacen.contact.constant.Constant;
import com.elcom.metacen.contact.model.KeywordData;
import com.elcom.metacen.contact.model.OtherObject;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.service.KeywordDataService;
import com.elcom.metacen.contact.service.OtherObjectService;
import com.elcom.metacen.contact.validation.OtherObjectValidation;
import com.elcom.metacen.enums.ObjectType;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.utils.StringUtil;
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
public class OtherObjectController extends BaseController {

    @Autowired
    private OtherObjectService otherObjectService;

    @Autowired
    protected ModelMapper modelMapper;

    @Autowired
    private KeywordDataService keywordDataService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupController.class);

    public ResponseMessage insertOtherObject(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Create other object with request >>> {}", bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "POST", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            OtherObjectRequestDTO otherObjectRequestDTO = buildOtherObjectDTO(bodyParam);
            String validationMsg = new OtherObjectValidation().validateOtherObject(otherObjectRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }
            OtherObject otherObject = otherObjectService.save(otherObjectRequestDTO, dto.getUserName());
            if (otherObject != null) {
                Map<String, Object> bodyRelationship = new HashMap<>();
                ObjectDetailDTO objectDetailDTO = new ObjectDetailDTO();
                objectDetailDTO.setObjectId(otherObject.getId());
                objectDetailDTO.setObjectMmsi("");
                objectDetailDTO.setObjectUuid(otherObject.getUuid());
                objectDetailDTO.setObjectType(ObjectType.OTHER_OBJECT.name());
                objectDetailDTO.setObjectName(otherObject.getName());
                ObjectRelationshipDetailDTO objectRelationshipDetailDTO = new ObjectRelationshipDetailDTO();
                objectRelationshipDetailDTO.setObject(objectDetailDTO);
                objectRelationshipDetailDTO.setRelationshipLst(otherObjectRequestDTO.getRelationshipLst());
                bodyRelationship.put("object", objectRelationshipDetailDTO.getObject());
                bodyRelationship.put("relationshipLst", objectRelationshipDetailDTO.getRelationshipLst());
                callLinkObjectContains(bodyRelationship);
                return new ResponseMessage(new MessageContent(otherObject));
            } else {
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage updateOtherObject(Map<String, String> headerParam, Map<String, Object> bodyParam, String pathParam, String requestPath) {
        LOGGER.info("Update other object id {} with request >>> {}", pathParam, bodyParam);

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
            OtherObject otherObject = otherObjectService.findByUuid(uuid);
            if (otherObject == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Đối tượng không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Đối tượng không tồn tại", null));
            }
            OtherObjectRequestDTO otherObjectRequestDTO = buildOtherObjectDTO(bodyParam);
            String validationMsg = new OtherObjectValidation().validateOtherObject(otherObjectRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            OtherObject result = otherObjectService.updateOtherObject(otherObject, otherObjectRequestDTO, dto.getUserName());
            Map<String, Object> bodyChangeName = new HashMap<>();
            bodyChangeName.put("objectUuid", otherObject.getUuid());
            bodyChangeName.put("objectName", otherObjectRequestDTO.getName());
            bodyChangeName.put("objectId", otherObject.getId());
            bodyChangeName.put("objectType", ObjectType.OTHER_OBJECT);
            this.ChangeNameVsatMediaDataObjectAnalyzed(bodyChangeName);
            ResponseMessage messageContent = this.ChangeNameMapping(bodyChangeName);
            String message = messageContent.getData().getData().toString();
            if(message != null){
                callLinkObjectUpdateNote(bodyChangeName);
            }
            if (result != null) {
                Map<String, Object> bodyRelationship = new HashMap<>();
                ObjectDetailDTO objectDetailDTO = new ObjectDetailDTO();
                objectDetailDTO.setObjectId(otherObject.getId());
                objectDetailDTO.setObjectMmsi("");
                objectDetailDTO.setObjectUuid(otherObject.getUuid());
                objectDetailDTO.setObjectType(ObjectType.OTHER_OBJECT.name());
                objectDetailDTO.setObjectName(otherObject.getName());
                ObjectRelationshipDetailDTO objectRelationshipDetailDTO = new ObjectRelationshipDetailDTO();
                objectRelationshipDetailDTO.setObject(objectDetailDTO);
                objectRelationshipDetailDTO.setRelationshipLst(otherObjectRequestDTO.getRelationshipLst());
                bodyRelationship.put("object", objectRelationshipDetailDTO.getObject());
                bodyRelationship.put("relationshipLst", objectRelationshipDetailDTO.getRelationshipLst());
                callLinkObjectContains(bodyRelationship);
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

    public ResponseMessage getOtherObjectById(String requestPath, Map<String, String> headerParam, String pathParam) {
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
            OtherObjectResponseDTO otherObjectResponseDTO = otherObjectService.findOtherObjectByUuid(uuid);
            if (otherObjectResponseDTO == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Đối tượng không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Đối tượng không tồn tại", null));
            }
            return new ResponseMessage(new MessageContent(otherObjectResponseDTO));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage filterOtherObject(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
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
                OtherObjectFilterDTO otherObjectFilterDTO = buildOtherObjectFilterDTO(bodyParam);
                String validationMsg = new OtherObjectValidation().validateFilterOtherObject(otherObjectFilterDTO);
                if (validationMsg != null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
                }
                Page<OtherObjectResponseDTO> pagedResult = otherObjectService.findListOtherObject(otherObjectFilterDTO);
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

    public ResponseMessage deleteOtherObject(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DELETE", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            OtherObject otherObject = otherObjectService.findByUuid(pathParam);
            if (otherObject == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Đối tượng không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Đối tượng không tồn tại", null));
            }
            otherObjectService.delete(otherObject, dto.getUserName());
            Map<String, Object> bodyObject = new HashMap<>();
            bodyObject.put("objectUuid", otherObject.getUuid());
            callLinkObjectDeleteNode(bodyObject);
            List<KeywordData> keywordData = keywordDataService.findByRefId(pathParam);
            this.keywordDataService.delete(keywordData);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    private OtherObjectRequestDTO buildOtherObjectDTO(Map<String, Object> bodyParam) {
        ObjectMapper mapper = new ObjectMapper();
        String name = (String) bodyParam.getOrDefault("name", "");
        Integer countryId = (bodyParam.get("countryId") != "") && (bodyParam.get("countryId") != null) ? ((Number) bodyParam.getOrDefault("countryId", "")).intValue() : 0;
        String description = (String) bodyParam.getOrDefault("description", "");
        String sideId = (String) bodyParam.getOrDefault("sideId", "");
        List<String> keywordLst = bodyParam.get("keywordLst") != null ? (List<String>) bodyParam.get("keywordLst") : null;

        List<FileDTO> imageLst = null;
        if (bodyParam.get("imageLst") != null) {
            imageLst = mapper.convertValue(
                    bodyParam.get("imageLst"),
                    new TypeReference<List<FileDTO>>() {
            });
        }

        List<FileDTO> fileAttachmentLst = null;
        if (bodyParam.get("fileAttachmentLst") != null) {
            fileAttachmentLst = mapper.convertValue(
                    bodyParam.get("fileAttachmentLst"),
                    new TypeReference<List<FileDTO>>() {
            });
        }

        List<ObjectRelationshipDTO> relationshipLst = null;
        if (bodyParam.get("relationshi"
                + ""
                + "pLst") != null) {
            relationshipLst = mapper.convertValue(
                    bodyParam.get("relationshipLst"),
                    new TypeReference<List<ObjectRelationshipDTO>>() {
            });
        }

        OtherObjectRequestDTO otherObjectRequestDTO = OtherObjectRequestDTO.builder()
                .name(name)
                .countryId(countryId)
                .description(description)
                .sideId(sideId)
                .imageLst(imageLst)
                .fileAttachmentLst(fileAttachmentLst)
                .keywordLst(keywordLst)
                .relationshipLst(relationshipLst)
                .build();

        return otherObjectRequestDTO;
    }

    private OtherObjectFilterDTO buildOtherObjectFilterDTO(Map<String, Object> bodyParam) {
        Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
        Integer size = bodyParam.get("size") != null ? (Integer) bodyParam.get("size") : 20;
        String sort = (String) bodyParam.getOrDefault("sort", "");
        String term = (String) bodyParam.getOrDefault("term", "");
        List<Integer> countryIds = bodyParam.get("countryIds") != null ? (List<Integer>) bodyParam.get("countryIds") : null;
        List<String> sideIds = bodyParam.get("sideIds") != null ? (List<String>) bodyParam.get("sideIds") : null;
        List<String> keywordIds = bodyParam.get("keywordIds") != null ? (List<String>) bodyParam.get("keywordIds") : null;

        OtherObjectFilterDTO otherObjectFilterDTO = OtherObjectFilterDTO.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .term(term)
                .countryIds(countryIds)
                .sideIds(sideIds)
                .keywordIds(keywordIds)
                .build();
        return otherObjectFilterDTO;
    }
}
