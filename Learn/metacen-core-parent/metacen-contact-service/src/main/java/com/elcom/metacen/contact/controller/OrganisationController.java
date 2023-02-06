package com.elcom.metacen.contact.controller;

import com.elcom.metacen.contact.constant.Constant;
import com.elcom.metacen.contact.model.KeywordData;
import com.elcom.metacen.contact.model.Organisation;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.service.KeywordDataService;
import com.elcom.metacen.contact.service.OrganisationService;
import com.elcom.metacen.contact.validation.OrganisationValidation;
import com.elcom.metacen.enums.ObjectType;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

@Controller
public class OrganisationController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganisationController.class);

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    protected ModelMapper modelMapper;

    @Autowired
    private KeywordDataService keywordDataService;

    public ResponseMessage filterOrganisation(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Filter organisation with request >>> {}", bodyParam);

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

            OrganisationFilterDTO organisationFilterDTO = buildOrganisationFilterRequest(bodyParam);
            String validationMsg = new OrganisationValidation().validateFilterOrganisation(organisationFilterDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            Page<OrganisationDTO> pagedResult = organisationService.findOrganisations(organisationFilterDTO);
            return new ResponseMessage(new MessageContent(pagedResult.getContent(), pagedResult.getTotalElements()));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage findById(Map<String, String> headerParam, String pathParam, String requestPath) {
        LOGGER.info("Find organisation by id >>> {}", pathParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DETAIL", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            String id = pathParam;
            OrganisationDTO organisationDTO = organisationService.findOrganisationByUuid(id);
            if (organisationDTO == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Tổ chức không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Tổ chức không tồn tại", null));
            }

            return new ResponseMessage(new MessageContent(organisationDTO));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage create(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Create organisation with request >>> {}", bodyParam);

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

            OrganisationRequestDTO organisationRequestDTO = buildOrganisationRequestDTO(bodyParam);
            String validationMsg = new OrganisationValidation().validateOrganisationRequest(organisationRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            Organisation organisation = organisationService.save(organisationRequestDTO, dto.getUserName());
            if (organisation != null) {
                Map<String, Object> bodyRelationship = new HashMap<>();
                ObjectDetailDTO objectDetailDTO = new ObjectDetailDTO();
                objectDetailDTO.setObjectId(organisation.getId());
                objectDetailDTO.setObjectMmsi("");
                objectDetailDTO.setObjectUuid(organisation.getUuid());
                objectDetailDTO.setObjectType(ObjectType.ORGANISATION.name());
                objectDetailDTO.setObjectName(organisation.getName());
                ObjectRelationshipDetailDTO objectRelationshipDetailDTO = new ObjectRelationshipDetailDTO();
                objectRelationshipDetailDTO.setObject(objectDetailDTO);
                objectRelationshipDetailDTO.setRelationshipLst(organisationRequestDTO.getRelationshipLst());
                bodyRelationship.put("object", objectRelationshipDetailDTO.getObject());
                bodyRelationship.put("relationshipLst", objectRelationshipDetailDTO.getRelationshipLst());
                callLinkObjectContains(bodyRelationship);
                return new ResponseMessage(new MessageContent(organisation));
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
        LOGGER.info("Update organisation id {} with request >>> {}", pathParam, bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "PUT", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            String id = pathParam;
            Organisation organisation = organisationService.findById(id);
            if (organisation == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Tổ chức không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Tổ chức không tồn tại", null));
            }

            if (bodyParam == null || bodyParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }

            OrganisationRequestDTO organisationRequestDTO = buildOrganisationRequestDTO(bodyParam);
            String validationMsg = new OrganisationValidation().validateOrganisationRequest(organisationRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            Organisation result = organisationService.update(organisation, organisationRequestDTO, dto.getUserName());
            Map<String, Object> bodyChangeName = new HashMap<>();
            bodyChangeName.put("objectUuid", organisation.getUuid());
            bodyChangeName.put("objectName", organisationRequestDTO.getName());
            bodyChangeName.put("objectId", organisation.getId());
            bodyChangeName.put("objectType", ObjectType.ORGANISATION);
            this.ChangeNameVsatMediaDataObjectAnalyzed(bodyChangeName);
            ResponseMessage messageContent = this.ChangeNameMapping(bodyChangeName);
            String message = messageContent.getData().getData().toString();
            if(message != null){
                callLinkObjectUpdateNote(bodyChangeName);
            }
            if (result != null) {
                Map<String, Object> bodyRelationship = new HashMap<>();
                ObjectDetailDTO objectDetailDTO = new ObjectDetailDTO();
                objectDetailDTO.setObjectId(organisation.getId());
                objectDetailDTO.setObjectMmsi("");
                objectDetailDTO.setObjectUuid(organisation.getUuid());
                objectDetailDTO.setObjectType(ObjectType.ORGANISATION.name());
                objectDetailDTO.setObjectName(organisation.getName());
                ObjectRelationshipDetailDTO objectRelationshipDetailDTO = new ObjectRelationshipDetailDTO();
                objectRelationshipDetailDTO.setObject(objectDetailDTO);
                objectRelationshipDetailDTO.setRelationshipLst(organisationRequestDTO.getRelationshipLst());
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

    public ResponseMessage delete(Map<String, String> headerParam, String pathParam, String requestPath) {
        LOGGER.info("Delete organisation by id >>> {}", pathParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DELETE", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            String id = pathParam;
            Organisation organisation = organisationService.findById(id);
            if (organisation == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Tổ chức không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Tổ chức không tồn tại", null));
            }

            Organisation result = organisationService.delete(organisation, dto.getUserName());
            List<KeywordData> keywordData = keywordDataService.findByRefId(pathParam);
            this.keywordDataService.delete(keywordData);
            if (result != null) {
                Map<String, Object> bodyObject = new HashMap<>();
                bodyObject.put("objectUuid", organisation.getUuid());
                callLinkObjectDeleteNode(bodyObject);
                return new ResponseMessage(new MessageContent(null));
            } else {
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    private OrganisationFilterDTO buildOrganisationFilterRequest(Map<String, Object> bodyParam) {
        Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
        Integer size = bodyParam.get("size") != null ? (Integer) bodyParam.get("size") : 20;
        String sort = (String) bodyParam.getOrDefault("sort", "");
        String term = (String) bodyParam.getOrDefault("term", "");
        List<String> organisationTypeLst = bodyParam.get("organisationTypeLst") != null ? (List<String>) bodyParam.get("organisationTypeLst") : null;
        List<Integer> countryIds = bodyParam.get("countryIds") != null ? (List<Integer>) bodyParam.get("countryIds") : null;
        List<String> sideIds = bodyParam.get("sideIds") != null ? (List<String>) bodyParam.get("sideIds") : null;
        List<String> keywordIds = bodyParam.get("keywordIds") != null ? (List<String>) bodyParam.get("keywordIds") : null;

        OrganisationFilterDTO organisationFilterDTO = OrganisationFilterDTO.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .term(term)
                .organisationTypeLst(organisationTypeLst)
                .countryIds(countryIds)
                .sideIds(sideIds)
                .keywordIds(keywordIds)
                .build();
        return organisationFilterDTO;
    }

    private OrganisationRequestDTO buildOrganisationRequestDTO(Map<String, Object> bodyParam) {
        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(df);

        String name = (String) bodyParam.getOrDefault("name", "");
        String organisationType = (String) bodyParam.getOrDefault("organisationType", "");
        Integer countryId = (bodyParam.get("countryId") != "") && (bodyParam.get("countryId") != null) ? ((Number) bodyParam.getOrDefault("countryId", "")).intValue() : 0;
        String headquarters = (String) bodyParam.getOrDefault("headquarters", "");
        String sideId = (String) bodyParam.getOrDefault("sideId", "");
        String description = (String) bodyParam.getOrDefault("description", "");
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

        OrganisationRequestDTO organisationRequestDTO = OrganisationRequestDTO.builder()
                .name(name)
                .organisationType(organisationType)
                .countryId(countryId)
                .headquarters(headquarters)
                .sideId(sideId)
                .description(description)
                .imageLst(imageLst)
                .fileAttachmentLst(fileAttachmentLst)
                .relationshipLst(relationshipLst)
                .keywordLst(keywordLst)
                .build();

        return organisationRequestDTO;
    }

    private OrganisationDTO entityToDto(Organisation organisation) {
        return modelMapper.map(organisation, OrganisationDTO.class);
    }
}
