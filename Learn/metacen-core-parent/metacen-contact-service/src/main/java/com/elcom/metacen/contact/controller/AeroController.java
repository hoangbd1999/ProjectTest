package com.elcom.metacen.contact.controller;

import com.elcom.metacen.contact.constant.Constant;
import com.elcom.metacen.contact.model.AeroAirplaneInfo;
import com.elcom.metacen.contact.model.KeywordData;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.model.dto.AeroDTO.AeroFilterDTO;
import com.elcom.metacen.contact.model.dto.AeroDTO.AeroInsertRequestDTO;
import com.elcom.metacen.contact.model.dto.AeroDTO.AeroResponseDTO;
import com.elcom.metacen.contact.repository.ObjectFilesRepository;
import com.elcom.metacen.contact.service.AeroService;
import com.elcom.metacen.contact.service.KeywordDataService;
import com.elcom.metacen.contact.service.impl.ObjectTypesServiceImpl;
import com.elcom.metacen.contact.validation.AeroValidation;
import com.elcom.metacen.enums.ObjectType;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class AeroController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AeroController.class);

    @Autowired
    AeroService aeroService;

    @Autowired
    ObjectTypesServiceImpl objectTypesService;

    @Autowired
    ObjectFilesRepository objectFilesRepository;

    @Autowired
    private KeywordDataService keywordDataService;

    public ResponseMessage insertAero(String requestPath, Map<String, Object> bodyParam, Map<String, String> headerParam) {
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
                AeroInsertRequestDTO aeroInsertRequestDTO = buildInsertRequestDTO(bodyParam);
                String validationMsg = new AeroValidation().validateAero(aeroInsertRequestDTO);
                if (validationMsg != null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
                }
                AeroAirplaneInfo aeroAirplaneInfo = aeroService.insert(aeroInsertRequestDTO, dto.getUserName());
                if (aeroAirplaneInfo != null) {
                    Map<String, Object> bodyRelationship = new HashMap<>();
                    ObjectDetailDTO objectDetailDTO = new ObjectDetailDTO();
                    objectDetailDTO.setObjectId(aeroAirplaneInfo.getId());
                    objectDetailDTO.setObjectMmsi("");
                    objectDetailDTO.setObjectUuid(aeroAirplaneInfo.getUuid());
                    objectDetailDTO.setObjectType(ObjectType.AIRPLANE.name());
                    objectDetailDTO.setObjectName(aeroAirplaneInfo.getName());
                    ObjectRelationshipDetailDTO objectRelationshipDetailDTO = new ObjectRelationshipDetailDTO();
                    objectRelationshipDetailDTO.setObject(objectDetailDTO);
                    objectRelationshipDetailDTO.setRelationshipLst(aeroInsertRequestDTO.getRelationshipLst());
                    bodyRelationship.put("object", objectRelationshipDetailDTO.getObject());
                    bodyRelationship.put("relationshipLst", objectRelationshipDetailDTO.getRelationshipLst());
                    callLinkObjectContains(bodyRelationship);
                    return new ResponseMessage(new MessageContent(aeroAirplaneInfo));
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

    public ResponseMessage updateAero(String requestPath, Map<String, Object> bodyParam, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "PUT", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            if (bodyParam == null || bodyParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }
            try {
                AeroAirplaneInfo aeroAirplaneInfo = aeroService.findByUuid(pathParam);
                if (aeroAirplaneInfo == null) {
                    return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Máy bay không tồn tại",
                            new MessageContent(HttpStatus.NOT_FOUND.value(), "Máy bay không tồn tại", null));
                }
                AeroInsertRequestDTO aeroInsertRequestDTO = buildInsertRequestDTO(bodyParam);
                String validationMsg = new AeroValidation().validateAero(aeroInsertRequestDTO);
                if (validationMsg != null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
                }
                AeroAirplaneInfo airplaneInfo = aeroService.update(aeroAirplaneInfo, aeroInsertRequestDTO, dto.getUserName());
                Map<String, Object> bodyChangeName = new HashMap<>();
                bodyChangeName.put("objectUuid", aeroAirplaneInfo.getUuid());
                bodyChangeName.put("objectName", aeroInsertRequestDTO.getName());
                bodyChangeName.put("objectId", aeroAirplaneInfo.getId());
                bodyChangeName.put("objectType", ObjectType.AIRPLANE);
                this.ChangeNameVsatMediaDataObjectAnalyzed(bodyChangeName);
                ResponseMessage messageContent = this.ChangeNameMapping(bodyChangeName);
                String message = messageContent.getData().getData().toString();
                if(message != null){
                    callLinkObjectUpdateNote(bodyChangeName);
                }
                if (airplaneInfo != null) {
                    Map<String, Object> bodyRelationship = new HashMap<>();
                    ObjectDetailDTO objectDetailDTO = new ObjectDetailDTO();
                    objectDetailDTO.setObjectId(aeroAirplaneInfo.getId());
                    objectDetailDTO.setObjectMmsi("");
                    objectDetailDTO.setObjectUuid(aeroAirplaneInfo.getUuid());
                    objectDetailDTO.setObjectType(ObjectType.AIRPLANE.name());
                    objectDetailDTO.setObjectName(aeroAirplaneInfo.getName());
                    ObjectRelationshipDetailDTO objectRelationshipDetailDTO = new ObjectRelationshipDetailDTO();
                    objectRelationshipDetailDTO.setObject(objectDetailDTO);
                    objectRelationshipDetailDTO.setRelationshipLst(aeroInsertRequestDTO.getRelationshipLst());
                    bodyRelationship.put("object", objectRelationshipDetailDTO.getObject());
                    bodyRelationship.put("relationshipLst", objectRelationshipDetailDTO.getRelationshipLst());
                    callLinkObjectContains(bodyRelationship);
                    return new ResponseMessage(new MessageContent(airplaneInfo));
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

    public ResponseMessage filterAero(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
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
                AeroFilterDTO aeroFilterDTO = buildAeroFilterDTO(bodyParam);
                String validationMsg = new AeroValidation().validateFilterAero(aeroFilterDTO);
                if (validationMsg != null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
                }
                Page<AeroResponseDTO> pagedResult = aeroService.findListAero(aeroFilterDTO);
                return new ResponseMessage(new MessageContent(pagedResult.getContent(), pagedResult.getTotalElements()));
            } catch (Exception e) {
                LOGGER.error("Filter fail", e);
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện tác vụ này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện tác vụ này", null));
        }
    }

    public ResponseMessage getAeroById(String requestPath, Map<String, String> headerParam, String pathParam) {
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
                AeroResponseDTO aeroResponseDTO = aeroService.findAeroByUuid(uuid);
                if (aeroResponseDTO == null) {
                    return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Máy bay không tồn tại",
                            new MessageContent(HttpStatus.NOT_FOUND.value(), "Máy bay không tồn tại", null));
                }
                return new ResponseMessage(new MessageContent(aeroResponseDTO));
            } catch (Exception e) {
                LOGGER.error("Find by uuid fail", e);
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện tác vụ này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện tác vụ này", null));
        }
    }

    public ResponseMessage deleteAero(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DELETE", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            try {
                AeroAirplaneInfo aeroAirplaneInfo = aeroService.findByUuid(pathParam);
                if (aeroAirplaneInfo == null) {
                    return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Máy bay không tồn tại",
                            new MessageContent(HttpStatus.NOT_FOUND.value(), "Máy bay không tồn tại", null));
                }
                this.aeroService.delete(aeroAirplaneInfo, dto.getUserName());
                Map<String, Object> bodyObject = new HashMap<>();
                bodyObject.put("objectUuid", aeroAirplaneInfo.getUuid());
                callLinkObjectDeleteNode(bodyObject);
                List<KeywordData> keywordData = keywordDataService.findByRefId(pathParam);
                this.keywordDataService.delete(keywordData);
                return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
            } catch (Exception e) {
                LOGGER.error("Delete fail", e);
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện tác vụ này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện tác vụ này", null));
        }
    }

    private AeroInsertRequestDTO buildInsertRequestDTO(Map<String, Object> bodyParam) {
        ObjectMapper mapper = new ObjectMapper();
        String name = (String) bodyParam.getOrDefault("name", "");
        String model = (String) bodyParam.getOrDefault("model", "");
        Integer countryId = (bodyParam.get("countryId") != "") && (bodyParam.get("countryId") != null)  ? ((Number) bodyParam.getOrDefault("countryId", "")).intValue() : 0;
        Double dimLength = bodyParam.get("dimLength") != null ? ((Number) bodyParam.getOrDefault("dimLength", "")).doubleValue() : null;
        Double dimWidth = bodyParam.get("dimWidth") != null ? ((Number) bodyParam.getOrDefault("dimWidth", "")).doubleValue() : null;
        Double dimHeight = bodyParam.get("dimHeight") != null ? ((Number) bodyParam.getOrDefault("dimHeight", "")).doubleValue() : null;
        Double speedMax = bodyParam.get("speedMax") != null ? ((Number) bodyParam.getOrDefault("speedMax", "")).doubleValue() : null;
        Double grossTonnage = bodyParam.get("grossTonnage") != null ? ((Number) bodyParam.getOrDefault("grossTonnage", "")).doubleValue() : null;
        String payrollTime = (String) bodyParam.getOrDefault("payrollTime", "");
        String equipment = (String) bodyParam.getOrDefault("equipment", "");
        String permanentBase = (String) bodyParam.getOrDefault("permanentBase", "");
        String description = (String) bodyParam.getOrDefault("description", "");
        String sideId = (String) bodyParam.getOrDefault("sideId", "");
        String typeId = (String) bodyParam.getOrDefault("typeId", "");
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

        AeroInsertRequestDTO aeroInsertRequestDTO = AeroInsertRequestDTO.builder()
                .name(name)
                .model(model)
                .countryId(countryId)
                .dimLength(dimLength)
                .dimWidth(dimWidth)
                .dimHeight(dimHeight)
                .speedMax(speedMax)
                .grossTonnage(grossTonnage)
                .payrollTime(payrollTime)
                .equipment(equipment)
                .permanentBase(permanentBase)
                .description(description)
                .sideId(sideId)
                .typeId(typeId)
                .imageLst(imageLst)
                .fileAttachmentLst(fileAttachmentLst)
                .keywordLst(keywordLst)
                .relationshipLst(relationshipLst)
                .build();
        return aeroInsertRequestDTO;
    }

    private AeroFilterDTO buildAeroFilterDTO(Map<String, Object> bodyParam) {
        Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
        Integer size = bodyParam.get("size") != null ? (Integer) bodyParam.get("size") : 20;
        String sort = (String) bodyParam.getOrDefault("sort", "");
        String term = (String) bodyParam.getOrDefault("term", "");
        List<Integer> countryIds = bodyParam.get("countryIds") != null ? (List<Integer>) bodyParam.get("countryIds") : null;
        List<String> sideIds = bodyParam.get("sideIds") != null ? (List<String>) bodyParam.get("sideIds") : null;
        List<String> keywordIds = bodyParam.get("keywordIds") != null ? (List<String>) bodyParam.get("keywordIds") : null;

        AeroFilterDTO aeroFilterDTO = AeroFilterDTO.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .term(term)
                .countryIds(countryIds)
                .sideIds(sideIds)
                .keywordIds(keywordIds)
                .build();
        return aeroFilterDTO;
    }
}
