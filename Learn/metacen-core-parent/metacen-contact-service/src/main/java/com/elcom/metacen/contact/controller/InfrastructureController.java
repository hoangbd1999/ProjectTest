package com.elcom.metacen.contact.controller;

import com.elcom.metacen.contact.constant.Constant;
import com.elcom.metacen.contact.model.Infrastructure;
import com.elcom.metacen.contact.model.KeywordData;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.service.InfrastructureService;
import com.elcom.metacen.contact.service.KeywordDataService;
import com.elcom.metacen.contact.validation.InfrastructureValidation;
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
public class InfrastructureController extends BaseController {

    @Autowired
    private InfrastructureService infrastructureService;

    @Autowired
    protected ModelMapper modelMapper;

    @Autowired
    private KeywordDataService keywordDataService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupController.class);

    public ResponseMessage insertInfrastructure(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Create infrastructure with request >>> {}", bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "POST", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            InfrastructureRequestDTO infrastructureRequestDTO = buildInfrastructureDTO(bodyParam);
            String validationMsg = new InfrastructureValidation().validateInfrastructure(infrastructureRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }
            Infrastructure infrastructure = infrastructureService.save(infrastructureRequestDTO, dto.getUserName());
            if (infrastructure != null) {
                Map<String, Object> bodyRelationship = new HashMap<>();
                ObjectDetailDTO objectDetailDTO = new ObjectDetailDTO();
                objectDetailDTO.setObjectId(infrastructure.getId());
                objectDetailDTO.setObjectMmsi("");
                objectDetailDTO.setObjectUuid(infrastructure.getUuid());
                objectDetailDTO.setObjectType(ObjectType.INFRASTRUCTURE.name());
                objectDetailDTO.setObjectName(infrastructure.getName());
                ObjectRelationshipDetailDTO objectRelationshipDetailDTO = new ObjectRelationshipDetailDTO();
                objectRelationshipDetailDTO.setObject(objectDetailDTO);
                objectRelationshipDetailDTO.setRelationshipLst(infrastructureRequestDTO.getRelationshipLst());
                bodyRelationship.put("object", objectRelationshipDetailDTO.getObject());
                bodyRelationship.put("relationshipLst", objectRelationshipDetailDTO.getRelationshipLst());
                callLinkObjectContains(bodyRelationship);
                return new ResponseMessage(new MessageContent(infrastructure));
            } else {
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage updateInfrastructure(Map<String, String> headerParam, Map<String, Object> bodyParam, String pathParam, String requestPath) {
        LOGGER.info("Update infrastructure id {} with request >>> {}", pathParam, bodyParam);

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
            Infrastructure infrastructure = infrastructureService.findByUuid(uuid);
            if (infrastructure == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Cơ sở hạ tầng không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Cơ sở hạ tầng không tồn tại", null));
            }
            InfrastructureRequestDTO infrastructureRequestDTO = buildInfrastructureDTO(bodyParam);
            String validationMsg = new InfrastructureValidation().validateInfrastructure(infrastructureRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            Infrastructure result = infrastructureService.updateInfrastructure(infrastructure, infrastructureRequestDTO, dto.getUserName());
            Map<String, Object> bodyChangeName = new HashMap<>();
            bodyChangeName.put("objectUuid", infrastructure.getUuid());
            bodyChangeName.put("objectName", infrastructureRequestDTO.getName());
            bodyChangeName.put("objectId", infrastructure.getId());
            bodyChangeName.put("objectType", ObjectType.INFRASTRUCTURE);
            this.ChangeNameVsatMediaDataObjectAnalyzed(bodyChangeName);
            ResponseMessage messageContent = this.ChangeNameMapping(bodyChangeName);
            String message = messageContent.getData().getData().toString();
            if(message != null){
                callLinkObjectUpdateNote(bodyChangeName);
            }
            if (result != null) {
                Map<String, Object> bodyRelationship = new HashMap<>();
                ObjectDetailDTO objectDetailDTO = new ObjectDetailDTO();
                objectDetailDTO.setObjectId(infrastructure.getId());
                objectDetailDTO.setObjectMmsi("");
                objectDetailDTO.setObjectUuid(infrastructure.getUuid());
                objectDetailDTO.setObjectType(ObjectType.INFRASTRUCTURE.name());
                objectDetailDTO.setObjectName(infrastructure.getName());
                ObjectRelationshipDetailDTO objectRelationshipDetailDTO = new ObjectRelationshipDetailDTO();
                objectRelationshipDetailDTO.setObject(objectDetailDTO);
                objectRelationshipDetailDTO.setRelationshipLst(infrastructureRequestDTO.getRelationshipLst());
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

    public ResponseMessage getInfrastructureById(String requestPath, Map<String, String> headerParam, String pathParam) {
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
            InfrastructureResponseDTO infrastructureResponseDTO = infrastructureService.findInfrastructureByUuid(uuid);
            if (infrastructureResponseDTO == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Cơ sở hạ tầng không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Cơ sở hạ tầng không tồn tại", null));
            }
            return new ResponseMessage(new MessageContent(infrastructureResponseDTO));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage filterInfrastructure(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
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
                InfrastructureFilterDTO infrastructureFilterDTO = buildInfrastructureFilterDTO(bodyParam);
                String validationMsg = new InfrastructureValidation().validateFilterInfrastructure(infrastructureFilterDTO);
                if (validationMsg != null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
                }
                Page<InfrastructureResponseDTO> pagedResult = infrastructureService.findListInfrastructure(infrastructureFilterDTO);
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

    public ResponseMessage deleteInfrastructure(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DELETE", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            Infrastructure infrastructure = infrastructureService.findByUuid(pathParam);
            if (infrastructure == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Cơ sở hạ tầng không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Cơ sở hạ tầng không tồn tại", null));
            }
            infrastructureService.delete(infrastructure, dto.getUserName());
            Map<String, Object> bodyObject = new HashMap<>();
            bodyObject.put("objectUuid", infrastructure.getUuid());
            callLinkObjectDeleteNode(bodyObject);
            List<KeywordData> keywordData = keywordDataService.findByRefId(pathParam);
            this.keywordDataService.delete(keywordData);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    private InfrastructureRequestDTO buildInfrastructureDTO(Map<String, Object> bodyParam) {
        ObjectMapper mapper = new ObjectMapper();
        String name = (String) bodyParam.getOrDefault("name", "");
        String location = (String) bodyParam.getOrDefault("location", "");
        Integer countryId = (bodyParam.get("countryId") != "") && (bodyParam.get("countryId") != null) ? ((Number) bodyParam.getOrDefault("countryId", "")).intValue() : 0;
        Integer infrastructureType = StringUtil.objectToInteger(bodyParam.getOrDefault("infrastructureType", ""));
        String area = (String) bodyParam.getOrDefault("area", "");
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
        if (infrastructureType == null || infrastructureType.equals("")) {
            infrastructureType = 3;
        }
        InfrastructureRequestDTO infrastructureRequestDTO = InfrastructureRequestDTO.builder()
                .name(name)
                .location(location)
                .countryId(countryId)
                .infrastructureType(infrastructureType)
                .area(area)
                .description(description)
                .sideId(sideId)
                .imageLst(imageLst)
                .fileAttachmentLst(fileAttachmentLst)
                .keywordLst(keywordLst)
                .relationshipLst(relationshipLst)
                .build();

        return infrastructureRequestDTO;
    }

    private InfrastructureFilterDTO buildInfrastructureFilterDTO(Map<String, Object> bodyParam) {
        Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
        Integer size = bodyParam.get("size") != null ? (Integer) bodyParam.get("size") : 20;
        String sort = (String) bodyParam.getOrDefault("sort", "");
        String term = (String) bodyParam.getOrDefault("term", "");
        List<Integer> countryIds = bodyParam.get("countryIds") != null ? (List<Integer>) bodyParam.get("countryIds") : null;
        List<String> infrastructureTypeLst = bodyParam.get("infrastructureTypeLst") != null ? (List<String>) bodyParam.get("infrastructureTypeLst") : null;
        List<String> sideIds = bodyParam.get("sideIds") != null ? (List<String>) bodyParam.get("sideIds") : null;
        List<String> keywordIds = bodyParam.get("keywordIds") != null ? (List<String>) bodyParam.get("keywordIds") : null;

        InfrastructureFilterDTO infrastructureFilterDTO = InfrastructureFilterDTO.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .term(term)
                .countryIds(countryIds)
                .infrastructureTypeLst(infrastructureTypeLst)
                .sideIds(sideIds)
                .keywordIds(keywordIds)
                .build();
        return infrastructureFilterDTO;
    }
}
