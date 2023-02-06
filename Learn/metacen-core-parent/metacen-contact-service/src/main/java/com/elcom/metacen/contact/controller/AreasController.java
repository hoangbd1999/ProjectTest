package com.elcom.metacen.contact.controller;

import com.elcom.metacen.contact.constant.Constant;
import com.elcom.metacen.contact.model.Areas;
import com.elcom.metacen.contact.model.KeywordData;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.model.dto.AreasDTO.AreasDTO;
import com.elcom.metacen.contact.model.dto.AreasDTO.AreasRequestDTO;
import com.elcom.metacen.contact.model.dto.AreasDTO.AreasFilterDTO;
import com.elcom.metacen.contact.model.dto.AreasDTO.AreasResponseDTO;
import com.elcom.metacen.contact.service.AreasService;
import com.elcom.metacen.contact.service.KeywordDataService;
import com.elcom.metacen.contact.validation.AreasValidation;
import com.elcom.metacen.enums.ObjectType;
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
public class AreasController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AreasController.class);

    @Autowired
    AreasService areasService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private KeywordDataService keywordDataService;


    public ResponseMessage insertAreas(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "POST", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            if (bodyParam == null || bodyParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }
            try {
                AreasRequestDTO areasRequestDTO = buildAreasRequestDTO(bodyParam);
                String validationMsg = new AreasValidation().validateAreas(areasRequestDTO);
                if (validationMsg != null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
                }

                Areas areas = areasService.save(areasRequestDTO, dto.getUserName());
                if (areas != null) {
                    Map<String, Object> bodyRelationship = new HashMap<>();
                    ObjectDetailDTO objectDetailDTO = new ObjectDetailDTO();
                    objectDetailDTO.setObjectId(areas.getId());
                    objectDetailDTO.setObjectMmsi("");
                    objectDetailDTO.setObjectUuid(areas.getUuid());
                    objectDetailDTO.setObjectType(ObjectType.AREA.name());
                    objectDetailDTO.setObjectName(areas.getName());
                    ObjectRelationshipDetailDTO objectRelationshipDetailDTO = new ObjectRelationshipDetailDTO();
                    objectRelationshipDetailDTO.setObject(objectDetailDTO);
                    objectRelationshipDetailDTO.setRelationshipLst(areasRequestDTO.getRelationshipLst());
                    bodyRelationship.put("object", objectRelationshipDetailDTO.getObject());
                    bodyRelationship.put("relationshipLst", objectRelationshipDetailDTO.getRelationshipLst());
                    callLinkObjectContains(bodyRelationship);
                    return new ResponseMessage(new MessageContent(entityToDto(areas)));
                } else {
                    return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
                }
            } catch (Exception e) {
                String message = String.format("Error: %s", e.getMessage());
                LOGGER.error(message, e);
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage updateAreas(Map<String, String> headerParam, Map<String, Object> bodyParam, String pathParam, String requestPath) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "PUT", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            if (bodyParam == null || bodyParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }
            try {
                String uuid = pathParam;
                Areas areas = areasService.findById(uuid);
                if (areas == null) {
                    return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Khu vực không tồn tại",
                            new MessageContent(HttpStatus.NOT_FOUND.value(), "Khu vực không tồn tại", null));
                }

                AreasRequestDTO areasRequestDTO = buildAreasRequestDTO(bodyParam);
                String validationMsg = new AreasValidation().validateAreas(areasRequestDTO);
                if (validationMsg != null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
                }
                Areas result = areasService.updateAreas(areas, areasRequestDTO, dto.getUserName());
                Map<String, Object> bodyChangeName = new HashMap<>();
                bodyChangeName.put("objectUuid", areas.getUuid());
                bodyChangeName.put("objectName", areasRequestDTO.getName());
                bodyChangeName.put("objectId", areas.getId());
                bodyChangeName.put("objectType", ObjectType.AREA);
                this.ChangeNameVsatMediaDataObjectAnalyzed(bodyChangeName);
                ResponseMessage messageContent = this.ChangeNameMapping(bodyChangeName);
                String message = messageContent.getData().getData().toString();
                if(message != null){
                    callLinkObjectUpdateNote(bodyChangeName);
                }
                if (result != null) {
                    Map<String, Object> bodyRelationship = new HashMap<>();
                    ObjectDetailDTO objectDetailDTO = new ObjectDetailDTO();
                    objectDetailDTO.setObjectId(areas.getId());
                    objectDetailDTO.setObjectMmsi("");
                    objectDetailDTO.setObjectUuid(areas.getUuid());
                    objectDetailDTO.setObjectType(ObjectType.AREA.name());
                    objectDetailDTO.setObjectName(areas.getName());
                    ObjectRelationshipDetailDTO objectRelationshipDetailDTO = new ObjectRelationshipDetailDTO();
                    objectRelationshipDetailDTO.setObject(objectDetailDTO);
                    objectRelationshipDetailDTO.setRelationshipLst(areasRequestDTO.getRelationshipLst());
                    bodyRelationship.put("object", objectRelationshipDetailDTO.getObject());
                    bodyRelationship.put("relationshipLst", objectRelationshipDetailDTO.getRelationshipLst());
                    callLinkObjectContains(bodyRelationship);
                    return new ResponseMessage(new MessageContent(entityToDto(result)));
                } else {
                    return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
                }
            } catch (Exception e) {
                String message = String.format("Error: %s", e.getMessage());
                LOGGER.error(message, e);
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage getAreasById(Map<String, String> headerParam, String pathParam, String requestPath) {
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
            AreasResponseDTO areasResponseDTO = areasService.findAreasByUuid(uuid);
            if (areasResponseDTO == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Khu vực không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Khu vực không tồn tại", null));
            }
            return new ResponseMessage(new MessageContent(areasResponseDTO));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage filterAreas(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
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
                AreasFilterDTO areasFilterDTO = buildAreasFilterDTO(bodyParam);
                String validationMsg = new AreasValidation().validateFilterAreas(areasFilterDTO);
                if (validationMsg != null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
                }
                Page<AreasResponseDTO> pagedResult = areasService.findListAreas(areasFilterDTO);
                return new ResponseMessage(new MessageContent(pagedResult.getContent(), pagedResult.getTotalElements()));
            } catch (Exception e) {
                String message = String.format("Error: %s", e.getMessage());
                LOGGER.error(message, e);
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage deleteAreas(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "GET", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            String uuid = pathParam;
            Areas areas = areasService.findById(uuid);
            if (areas == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Khu vực không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Khu vực không tồn tại", null));
            }
            this.areasService.deleteAreas(areas, dto.getUserName());
            Map<String, Object> bodyObject = new HashMap<>();
            bodyObject.put("objectUuid", areas.getUuid());
            callLinkObjectDeleteNode(bodyObject);
            List<KeywordData> keywordData = keywordDataService.findByRefId(pathParam);
            this.keywordDataService.delete(keywordData);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    private AreasFilterDTO buildAreasFilterDTO(Map<String, Object> bodyParam) {

        Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
        Integer size = bodyParam.get("size") != null ? (Integer) bodyParam.get("size") : 20;
        String sort = (String) bodyParam.getOrDefault("sort", "");
        String term = (String) bodyParam.getOrDefault("term", "");
        List<String> sideIds = bodyParam.get("sideIds") != null ? (List<String>) bodyParam.get("sideIds") : null;
        List<String> keywordIds = bodyParam.get("keywordIds") != null ? (List<String>) bodyParam.get("keywordIds") : null;

        AreasFilterDTO areasFilterDTO = AreasFilterDTO.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .term(term)
                .sideIds(sideIds)
                .keywordIds(keywordIds)
                .build();
        return areasFilterDTO;
    }

    private AreasRequestDTO buildAreasRequestDTO(Map<String, Object> bodyParam) {
        ObjectMapper mapper = new ObjectMapper();
        String name = (String) bodyParam.getOrDefault("name", "");
        String value = (String) bodyParam.getOrDefault("value", "");
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
        if (bodyParam.get("relationshipLst") != null) {
            relationshipLst = mapper.convertValue(
                    bodyParam.get("relationshipLst"),
                    new TypeReference<List<ObjectRelationshipDTO>>() {
            });
        }

        AreasRequestDTO areasRequestDTO = AreasRequestDTO.builder()
                .name(name)
                .value(value)
                .description(description)
                .sideId(sideId)
                .imageLst(imageLst)
                .fileAttachmentLst(fileAttachmentLst)
                .keywordLst(keywordLst)
                .relationshipLst(relationshipLst)
                .build();
        return areasRequestDTO;
    }

    private AreasDTO entityToDto(Areas areas) {
        return modelMapper.map(areas, AreasDTO.class);
    }
}
