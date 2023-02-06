package com.elcom.metacen.contact.controller;

import com.elcom.metacen.contact.constant.Constant;
import com.elcom.metacen.contact.model.KeywordData;
import com.elcom.metacen.contact.model.People;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.service.KeywordDataService;
import com.elcom.metacen.contact.service.PeopleService;
import com.elcom.metacen.contact.validation.PeopleValidation;
import com.elcom.metacen.enums.DataNoteStatus;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class PeopleController extends BaseController {

    @Autowired
    private PeopleService peopleService;

    @Autowired
    protected ModelMapper modelMapper;

    @Autowired
    private KeywordDataService keywordDataService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupController.class);

    public ResponseMessage insertPeople(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) throws ParseException {
        LOGGER.info("Create people with request >>> {}", bodyParam);

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

            PeopleRequestDTO peopleRequestDTO = buildPeopleDTO(bodyParam);
            String validationMsg = new PeopleValidation().validatePeople(peopleRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            People people = peopleService.save(peopleRequestDTO, dto.getUserName());
            if (people != null) {
                Map<String, Object> bodyRelationship = new HashMap<>();
                ObjectDetailDTO objectDetailDTO = new ObjectDetailDTO();
                objectDetailDTO.setObjectId(people.getId());
                objectDetailDTO.setObjectMmsi("");
                objectDetailDTO.setObjectUuid(people.getUuid());
                objectDetailDTO.setObjectType(ObjectType.PEOPLE.name());
                objectDetailDTO.setObjectName(people.getName());
                ObjectRelationshipDetailDTO objectRelationshipDetailDTO = new ObjectRelationshipDetailDTO();
                objectRelationshipDetailDTO.setObject(objectDetailDTO);
                objectRelationshipDetailDTO.setRelationshipLst(peopleRequestDTO.getRelationshipLst());
                bodyRelationship.put("object", objectRelationshipDetailDTO.getObject());
                bodyRelationship.put("relationshipLst", objectRelationshipDetailDTO.getRelationshipLst());
                callLinkObjectContains(bodyRelationship);
                return new ResponseMessage(new MessageContent(entityToDto(people)));
            } else {

                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage updatePeople(Map<String, String> headerParam, Map<String, Object> bodyParam, String pathParam, String requestPath) throws ParseException {
        LOGGER.info("Update people id {} with request >>> {}", pathParam, bodyParam);

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
            People people = peopleService.findByUuid(uuid);
            if (people == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "People không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "People không tồn tại", null));
            }

            if (bodyParam == null || bodyParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }

            PeopleRequestDTO peopleRequestDTO = buildPeopleDTO(bodyParam);
            String validationMsg = new PeopleValidation().validatePeople(peopleRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }
            People result = peopleService.updatePeople(people, peopleRequestDTO, dto.getUserName());
            Map<String, Object> bodyChangeName = new HashMap<>();
            bodyChangeName.put("objectUuid", people.getUuid());
            bodyChangeName.put("objectName", peopleRequestDTO.getName());
            bodyChangeName.put("objectId", people.getId());
            bodyChangeName.put("objectType", ObjectType.PEOPLE);
            this.ChangeNameVsatMediaDataObjectAnalyzed(bodyChangeName);
            ResponseMessage messageContent = this.ChangeNameMapping(bodyChangeName);
            String message = messageContent.getData().getData().toString();
            if(message != null){
                callLinkObjectUpdateNote(bodyChangeName);
            }
            if (result != null) {
                Map<String, Object> bodyRelationship = new HashMap<>();
                ObjectDetailDTO objectDetailDTO = new ObjectDetailDTO();
                objectDetailDTO.setObjectId(people.getId());
                objectDetailDTO.setObjectMmsi("");
                objectDetailDTO.setObjectUuid(people.getUuid());
                objectDetailDTO.setObjectType(ObjectType.PEOPLE.name());
                objectDetailDTO.setObjectName(people.getName());
                ObjectRelationshipDetailDTO objectRelationshipDetailDTO = new ObjectRelationshipDetailDTO();
                objectRelationshipDetailDTO.setObject(objectDetailDTO);
                objectRelationshipDetailDTO.setRelationshipLst(peopleRequestDTO.getRelationshipLst());
                bodyRelationship.put("object", objectRelationshipDetailDTO.getObject());
                bodyRelationship.put("relationshipLst", objectRelationshipDetailDTO.getRelationshipLst());
                callLinkObjectContains(bodyRelationship);
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

    public ResponseMessage getPeopleById(String requestPath, Map<String, String> headerParam, String pathParam) {
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
            PeopleResponseDTO peopleResponseDTO = peopleService.findPeopleByUuid(uuid);
            if (peopleResponseDTO == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "People không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "People không tồn tại", null));
            }
            return new ResponseMessage(new MessageContent(peopleResponseDTO));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage filterPeople(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
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
                PeopleFilterDTO peopleFilterDTO = buildPeopleFilterDTO(bodyParam);
                String validationMsg = new PeopleValidation().validateFilterPeople(peopleFilterDTO);
                if (validationMsg != null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
                }
                Page<PeopleResponseDTO> pagedResult = peopleService.findListPeople(peopleFilterDTO);
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

    public ResponseMessage deletePeople(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DELETE", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            People people = peopleService.findByUuid(pathParam);
            if (people == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "People không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "People không tồn tại", null));
            }
            this.peopleService.delete(people, dto.getUserName());
            Map<String, Object> bodyObject = new HashMap<>();
            bodyObject.put("objectUuid", people.getUuid());
            callLinkObjectDeleteNode(bodyObject);
            List<KeywordData> keywordData = keywordDataService.findByRefId(pathParam);
            this.keywordDataService.delete(keywordData);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    private PeopleRequestDTO buildPeopleDTO(Map<String, Object> bodyParam) throws ParseException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String name = (String) bodyParam.getOrDefault("name", "");
        String mobileNumber = (String) bodyParam.getOrDefault("mobileNumber", "");
        String email = (String) bodyParam.getOrDefault("email", "");
        Integer countryId = (bodyParam.get("countryId") != "") && (bodyParam.get("countryId") != null) ? ((Number) bodyParam.getOrDefault("countryId", "")).intValue() : 0;
        Date dateOfBirth = (bodyParam.get("dateOfBirth") != "") && (bodyParam.get("dateOfBirth") != null) ? dateFormat.parse((String) bodyParam.get("dateOfBirth")) : null;
        Integer gender = StringUtil.objectToInteger(bodyParam.getOrDefault("gender", ""));
        String address = (String) bodyParam.getOrDefault("address", "");
        String level = (String) bodyParam.getOrDefault("level", "");
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

        PeopleRequestDTO peopleRequestDTO = PeopleRequestDTO.builder()
                .name(name)
                .mobileNumber(mobileNumber)
                .email(email)
                .countryId(countryId)
                .dateOfBirth(dateOfBirth)
                .gender(gender)
                .address(address)
                .level(level)
                .description(description)
                .sideId(sideId)
                .imageLst(imageLst)
                .fileAttachmentLst(fileAttachmentLst)
                .keywordLst(keywordLst)
                .relationshipLst(relationshipLst)
                .build();

        return peopleRequestDTO;
    }

    private PeopleFilterDTO buildPeopleFilterDTO(Map<String, Object> bodyParam) {
        Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
        Integer size = bodyParam.get("size") != null ? (Integer) bodyParam.get("size") : 20;
        String sort = (String) bodyParam.getOrDefault("sort", "");
        String term = (String) bodyParam.getOrDefault("term", "");
        List<Integer> countryIds = bodyParam.get("countryIds") != null ? (List<Integer>) bodyParam.get("countryIds") : null;
        List<Integer> genders = bodyParam.get("genders") != null ? (List<Integer>) bodyParam.get("genders") : null;
        List<String> sideIds = bodyParam.get("sideIds") != null ? (List<String>) bodyParam.get("sideIds") : null;
        List<String> keywordIds = bodyParam.get("keywordIds") != null ? (List<String>) bodyParam.get("keywordIds") : null;

        PeopleFilterDTO peopleFilterDTO = PeopleFilterDTO.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .term(term)
                .countryIds(countryIds)
                .genders(genders)
                .sideIds(sideIds)
                .keywordIds(keywordIds)
                .build();
        return peopleFilterDTO;
    }

    private PeopleDTO entityToDto(People people) {
        return modelMapper.map(people, PeopleDTO.class);
    }
}
