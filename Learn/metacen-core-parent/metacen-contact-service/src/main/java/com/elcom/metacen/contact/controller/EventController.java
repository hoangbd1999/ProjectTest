package com.elcom.metacen.contact.controller;

import com.elcom.metacen.contact.constant.Constant;
import com.elcom.metacen.contact.model.Event;
import com.elcom.metacen.contact.model.KeywordData;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.model.dto.EventDTO.EventDTO;
import com.elcom.metacen.contact.model.dto.EventDTO.EventFilterDTO;
import com.elcom.metacen.contact.model.dto.EventDTO.EventRequestDTO;
import com.elcom.metacen.contact.model.dto.EventDTO.EventResponseDTO;
import com.elcom.metacen.contact.service.EventService;
import com.elcom.metacen.contact.service.KeywordDataService;
import com.elcom.metacen.contact.validation.EventValidation;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class EventController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventController.class);

    @Autowired
    private EventService eventService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private KeywordDataService keywordDataService;

    public ResponseMessage insertEvent(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
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
                EventRequestDTO eventRequestDTO = buildEventRequestDTO(bodyParam);
                String validationMsg = new EventValidation().validateEvent(eventRequestDTO);
                if (validationMsg != null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
                }
                Event event = eventService.save(eventRequestDTO, dto.getUserName());
                if (event != null) {
                    Map<String, Object> bodyRelationship = new HashMap<>();
                    ObjectDetailDTO objectDetailDTO = new ObjectDetailDTO();
                    objectDetailDTO.setObjectId(event.getId());
                    objectDetailDTO.setObjectMmsi("");
                    objectDetailDTO.setObjectUuid(event.getUuid());
                    objectDetailDTO.setObjectType(ObjectType.EVENT.name());
                    objectDetailDTO.setObjectName(event.getName());
                    ObjectRelationshipDetailDTO objectRelationshipDetailDTO = new ObjectRelationshipDetailDTO();
                    objectRelationshipDetailDTO.setObject(objectDetailDTO);
                    objectRelationshipDetailDTO.setRelationshipLst(eventRequestDTO.getRelationshipLst());
                    bodyRelationship.put("object", objectRelationshipDetailDTO.getObject());
                    bodyRelationship.put("relationshipLst", objectRelationshipDetailDTO.getRelationshipLst());
                    callLinkObjectContains(bodyRelationship);
                    return new ResponseMessage(new MessageContent(entityToDto(event)));
                } else {
                    return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
                }
            } catch (Exception e) {
                LOGGER.error("Insert failed >>> {}", e.toString());
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện tác vụ này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện tác vụ này", null));
        }
    }

    public ResponseMessage updateEvent(Map<String, String> headerParam, Map<String, Object> bodyParam, String pathParam, String requestPath) {
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
                Event event = eventService.findById(uuid);
                if (event == null) {
                    return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Sự kiện không tồn tại",
                            new MessageContent(HttpStatus.NOT_FOUND.value(), "Sự kiện không tồn tại", null));
                }
                EventRequestDTO eventRequestDTO = buildEventRequestDTO(bodyParam);
                String validationMsg = new EventValidation().validateEvent(eventRequestDTO);
                if (validationMsg != null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
                }
                Event result = eventService.updateEvent(event, eventRequestDTO, dto.getUserName());
                Map<String, Object> bodyChangeName = new HashMap<>();
                bodyChangeName.put("objectUuid", event.getUuid());
                bodyChangeName.put("objectName", eventRequestDTO.getName());
                bodyChangeName.put("objectId", event.getId());
                bodyChangeName.put("objectType", ObjectType.EVENT);
                this.ChangeNameVsatMediaDataObjectAnalyzed(bodyChangeName);
                ResponseMessage messageContent = this.ChangeNameMapping(bodyChangeName);
                String message = messageContent.getData().getData().toString();
                if(message != null){
                    callLinkObjectUpdateNote(bodyChangeName);
                }
                if (result != null) {
                    Map<String, Object> bodyRelationship = new HashMap<>();
                    ObjectDetailDTO objectDetailDTO = new ObjectDetailDTO();
                    objectDetailDTO.setObjectId(event.getId());
                    objectDetailDTO.setObjectMmsi("");
                    objectDetailDTO.setObjectUuid(event.getUuid());
                    objectDetailDTO.setObjectType(ObjectType.EVENT.name());
                    objectDetailDTO.setObjectName(event.getName());
                    ObjectRelationshipDetailDTO objectRelationshipDetailDTO = new ObjectRelationshipDetailDTO();
                    objectRelationshipDetailDTO.setObject(objectDetailDTO);
                    objectRelationshipDetailDTO.setRelationshipLst(eventRequestDTO.getRelationshipLst());
                    bodyRelationship.put("object", objectRelationshipDetailDTO.getObject());
                    bodyRelationship.put("relationshipLst", objectRelationshipDetailDTO.getRelationshipLst());
                    callLinkObjectContains(bodyRelationship);
                    return new ResponseMessage(new MessageContent(entityToDto(result)));
                } else {
                    return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
                }
            } catch (Exception e) {
                LOGGER.error("Update failed >>> {}", e.toString());
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện tác vụ này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện tác vụ này", null));
        }
    }

    public ResponseMessage getEventById(Map<String, String> headerParam, String requestPath, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DETAIL", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            try {
                String uuid = pathParam;
                EventResponseDTO eventResponseDTO = eventService.findEventByUuid(uuid);
                if (eventResponseDTO == null) {
                    return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Sự kiện không tồn tại",
                            new MessageContent(HttpStatus.NOT_FOUND.value(), "Sự kiện không tồn tại", null));
                }
                return new ResponseMessage(new MessageContent(eventResponseDTO));
            } catch (Exception e) {
                LOGGER.error("Find by uuid failed >>> {}", e.toString());
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện tác vụ này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện tác vụ này", null));
        }
    }

    public ResponseMessage filterEvent(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
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
                EventFilterDTO eventFilterDTO = buildEventFilterDTO(bodyParam);
                String validationMsg = new EventValidation().validateFilterEvent(eventFilterDTO);
                if (validationMsg != null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
                }
                Page<EventResponseDTO> pagedResult = eventService.findListEvent(eventFilterDTO);
                return new ResponseMessage(new MessageContent(pagedResult.getContent(), pagedResult.getTotalElements()));

            } catch (Exception e) {
                LOGGER.error("Filter failed >>> {}", e.toString());
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage deleteEvent(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DELETE", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            try {
                Event event = eventService.findById(pathParam);
                if (event == null) {
                    return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Sự kiện không tồn tại",
                            new MessageContent(HttpStatus.NOT_FOUND.value(), "Sự kiện không tồn tại", null));
                }
                this.eventService.deleteEvent(event, dto.getUserName());
                Map<String, Object> bodyObject = new HashMap<>();
                bodyObject.put("objectUuid", event.getUuid());
                callLinkObjectDeleteNode(bodyObject);
                List<KeywordData> keywordData = keywordDataService.findByRefId(pathParam);
                this.keywordDataService.delete(keywordData);
                return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
            } catch (Exception e) {
                LOGGER.error("Delete failed >>> {}", e.toString());
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    private EventRequestDTO buildEventRequestDTO(Map<String, Object> bodyParam) throws ParseException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String name = (String) bodyParam.getOrDefault("name", "");
        Date startTime = (bodyParam.get("startTime") != "") && (bodyParam.get("startTime") != null) ? dateFormat.parse((String) bodyParam.get("startTime")) : null;
        Date stopTime = (bodyParam.get("stopTime") != "") && (bodyParam.get("stopTime") != null) ? dateFormat.parse((String) bodyParam.get("stopTime")) : null;
        String description = (String) bodyParam.getOrDefault("description", "");
        String sideId = (String) bodyParam.getOrDefault("sideId", "");
        String area = (String) bodyParam.getOrDefault("area", "");
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
        EventRequestDTO eventRequestDTO = EventRequestDTO.builder()
                .name(name)
                .startTime(startTime)
                .stopTime(stopTime)
                .description(description)
                .sideId(sideId)
                .area(area)
                .imageLst(imageLst)
                .fileAttachmentLst(fileAttachmentLst)
                .keywordLst(keywordLst)
                .relationshipLst(relationshipLst)
                .build();
        return eventRequestDTO;
    }

    private EventFilterDTO buildEventFilterDTO(Map<String, Object> bodyParam) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
        Integer size = bodyParam.get("size") != null ? (Integer) bodyParam.get("size") : 20;
        String sort = (String) bodyParam.getOrDefault("sort", "");
        String term = (String) bodyParam.getOrDefault("term", "");
        Date startTime = (bodyParam.get("startTime") != "") && (bodyParam.get("startTime") != null) ? dateFormat.parse((String) bodyParam.get("startTime")) : null;
        Date stopTime = (bodyParam.get("stopTime") != "") && (bodyParam.get("stopTime") != null) ? dateFormat.parse((String) bodyParam.get("stopTime")) : null;
        List<String> sideIds = bodyParam.get("sideIds") != null ? (List<String>) bodyParam.get("sideIds") : null;
        List<String> keywordIds = bodyParam.get("keywordIds") != null ? (List<String>) bodyParam.get("keywordIds") : null;

        EventFilterDTO eventFilterDTO = EventFilterDTO.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .term(term)
                .startTime(startTime)
                .stopTime(stopTime)
                .sideIds(sideIds)
                .keywordIds(keywordIds)
                .build();
        return eventFilterDTO;
    }

    private EventDTO entityToDto(Event event) {
        return modelMapper.map(event, EventDTO.class);
    }
}
