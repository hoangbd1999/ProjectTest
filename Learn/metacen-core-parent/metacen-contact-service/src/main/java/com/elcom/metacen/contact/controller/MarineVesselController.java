package com.elcom.metacen.contact.controller;

import com.elcom.metacen.contact.constant.Constant;
import com.elcom.metacen.contact.model.*;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.service.*;
import com.elcom.metacen.contact.validation.MarineVesselValidation;
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

import java.util.*;

/**
 * @author hoangbd
 */
@Controller
public class MarineVesselController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectTypesController.class);

    @Autowired
    MarineVesselInfoService marineVesselInfoService;

    @Autowired
    ObjectFilesService objectFilesService;

    @Autowired
    ObjectTypesService objectTypesService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private KeywordDataService keywordDataService;

    // CRUD Marine Vessel Info
    public ResponseMessage insertMarineVesselInfo(String requestPath, Map<String, Object> bodyParam, Map<String, String> headerParam) {
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
                MarineVesselRequestDTO marineVesselRequestDTO = buildMarineVesselRequestDTO(bodyParam);
                String validationMsg = new MarineVesselValidation().validateMarineVessel(marineVesselRequestDTO);
                if (validationMsg != null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
                }
                MarineVesselInfo marineVesselInfo = this.marineVesselInfoService
                        .findByMmsi(marineVesselRequestDTO.getMmsi());
                if (marineVesselInfo != null) {
                    String msg = "Đã tồn tại mmsi '" + marineVesselRequestDTO.getMmsi() + "' trên hệ thống";
                    return new ResponseMessage(HttpStatus.OK.value(), msg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), msg, null));
                }
                marineVesselInfo = marineVesselInfoService.save(marineVesselRequestDTO, dto.getUserName());
                if (marineVesselInfo != null) {
                    Map<String, Object> bodyRelationship = new HashMap<>();
                    ObjectDetailDTO objectDetailDTO = new ObjectDetailDTO();
                    objectDetailDTO.setObjectId(marineVesselInfo.getId());
                    objectDetailDTO.setObjectMmsi("");
                    objectDetailDTO.setObjectUuid(marineVesselInfo.getUuid());
                    objectDetailDTO.setObjectType(ObjectType.VESSEL.name());
                    objectDetailDTO.setObjectName(marineVesselInfo.getName());
                    ObjectRelationshipDetailDTO objectRelationshipDetailDTO = new ObjectRelationshipDetailDTO();
                    objectRelationshipDetailDTO.setObject(objectDetailDTO);
                    objectRelationshipDetailDTO.setRelationshipLst(marineVesselRequestDTO.getRelationshipLst());
                    bodyRelationship.put("object", objectRelationshipDetailDTO.getObject());
                    bodyRelationship.put("relationshipLst", objectRelationshipDetailDTO.getRelationshipLst());
                    callLinkObjectContains(bodyRelationship);
                    return new ResponseMessage(new MessageContent(entityToDto(marineVesselInfo)));
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

    // CRUD Marine Vessel Info
    public ResponseMessage insertMarineVesselInfoInternal(String requestPath, Map<String, Object> bodyParam, Map<String, String> headerParam) {
        // Check isLogged
            try {
                MarineVesselRequestDTO marineVesselRequestDTO = buildMarineVesselRequestDTO(bodyParam);
                MarineVesselInfo marineVesselInfo = this.marineVesselInfoService
                        .findByMmsi(marineVesselRequestDTO.getMmsi());
                if (marineVesselInfo != null) {
                    String msg = "Đã tồn tại mmsi '" + marineVesselRequestDTO.getMmsi() + "' trên hệ thống";
                    return new ResponseMessage(HttpStatus.OK.value(), msg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), msg, null));
                }
                marineVesselInfo = marineVesselInfoService.save(marineVesselRequestDTO, "Hệ thống");
                if (marineVesselInfo != null) {
                    return new ResponseMessage(new MessageContent(entityToDto(marineVesselInfo)));
                } else {
                    return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
                }
            } catch (Exception e) {
                LOGGER.error("Insert failed >>> {}", e.toString());
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
    }

    public ResponseMessage updateMarineVesselInfo(String requestPath, Map<String, Object> bodyParam, Map<String, String> headerParam, String pathParam) {
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
                MarineVesselInfo marineVesselInfo = marineVesselInfoService.findById(uuid);
                if (marineVesselInfo == null) {
                    return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Phương tiện không tồn tại",
                            new MessageContent(HttpStatus.NOT_FOUND.value(), "Phương tiện không tồn tại", null));
                }
                MarineVesselRequestDTO marineVesselRequestDTO = buildMarineVesselRequestDTO(bodyParam);
                String validationMsg = new MarineVesselValidation().validateMarineVessel(marineVesselRequestDTO);
                if (validationMsg != null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
                }
                try {
                    MarineVesselInfo result = marineVesselInfoService.updateMarineVesselInfo(marineVesselInfo, marineVesselRequestDTO, dto.getUserName());
                    Map<String, Object> bodyChangeName = new HashMap<>();
                    bodyChangeName.put("objectUuid", marineVesselInfo.getUuid());
                    bodyChangeName.put("objectName", marineVesselRequestDTO.getName());
                    bodyChangeName.put("objectId", marineVesselInfo.getId());
                    bodyChangeName.put("objectType", ObjectType.VESSEL);
                    this.ChangeNameVsatMediaDataObjectAnalyzed(bodyChangeName);
                    ResponseMessage messageContent = this.ChangeNameMapping(bodyChangeName);
                    String message = messageContent.getData().getData().toString();
                    if(message != null){
                        callLinkObjectUpdateNote(bodyChangeName);
                    }
                    if (result != null) {
                        Map<String, Object> bodyRelationship = new HashMap<>();
                        ObjectDetailDTO objectDetailDTO = new ObjectDetailDTO();
                        objectDetailDTO.setObjectId(marineVesselInfo.getId());
                        objectDetailDTO.setObjectMmsi("");
                        objectDetailDTO.setObjectUuid(marineVesselInfo.getUuid());
                        objectDetailDTO.setObjectType(ObjectType.VESSEL.name());
                        objectDetailDTO.setObjectName(marineVesselInfo.getName());
                        ObjectRelationshipDetailDTO objectRelationshipDetailDTO = new ObjectRelationshipDetailDTO();
                        objectRelationshipDetailDTO.setObject(objectDetailDTO);
                        objectRelationshipDetailDTO.setRelationshipLst(marineVesselRequestDTO.getRelationshipLst());
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
            } catch (Exception e) {
                LOGGER.error("Error: ", e);
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện tác vụ này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện tác vụ này", null));
        }
    }

    public ResponseMessage getMarineVesselInfo(String requestPath, Map<String, String> headerParam, String pathParam) {
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
                MarineVesselResponseDTO marineVesselResponseDTO = marineVesselInfoService.findMarineVesselByUuid(uuid);
                if (marineVesselResponseDTO == null) {
                    return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Phương tiện không tồn tại",
                            new MessageContent(HttpStatus.NOT_FOUND.value(), "Phương tiện không tồn tại", null));
                }
                return new ResponseMessage(new MessageContent(marineVesselResponseDTO));
            } catch (Exception e) {
                LOGGER.error("Find by uuid failed >>> {}", e.toString());
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện tác vụ này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện tác vụ này ", null));
        }
    }

    public ResponseMessage filterMarineVesselInfo(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
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
                MarineVesselFilterDTO marineVesselFilterDTO = buildMarineVesselFilterDTO(bodyParam);
                String validationMsg = new MarineVesselValidation().validateFilterMarineVessel(marineVesselFilterDTO);
                if (validationMsg != null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
                }
                Page<MarineVesselResponseDTO> pagedResult = marineVesselInfoService.findListMarineVessel(marineVesselFilterDTO);
                return new ResponseMessage(new MessageContent(pagedResult.getContent(), pagedResult.getTotalElements()));
            } catch (Exception e) {
                LOGGER.error("Filter failed >>> {}", e.toString());
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện tác vụ này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện tác vụ này", null));
        }

    }

    public ResponseMessage deleteMarineVesselInfo(String requestPath, Map<String, String> headerParam, String pathParam) {
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
                MarineVesselInfo marineVesselInfo = marineVesselInfoService
                        .findById(pathParam);
                if (marineVesselInfo == null) {
                    return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Phương tiện không tồn tại",
                            new MessageContent(HttpStatus.NOT_FOUND.value(), "Phương tiện không tồn tại", null));
                }
                this.marineVesselInfoService.delete(marineVesselInfo, dto.getUserName());
                Map<String, Object> bodyObject = new HashMap<>();
                bodyObject.put("objectUuid", marineVesselInfo.getUuid());
                callLinkObjectDeleteNode(bodyObject);
                List<KeywordData> keywordData = keywordDataService.findByRefId(pathParam);
                this.keywordDataService.delete(keywordData);
                return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
            } catch (Exception e) {
                String message = String.format("Error: %s", e.getMessage());
                LOGGER.error(message, e);
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện tác vụ này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện tác vụ này", null));
        }
    }

    public ResponseMessage getObjectMappingByMmsiLst(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        List<Integer> mmsiLst = (List<Integer>) bodyParam.getOrDefault("mmsiLst", null);
        if (mmsiLst == null) {
            return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "mmsiLst Không được để trống",
                    new MessageContent(HttpStatus.BAD_REQUEST.value(), "mmsiLst Không được để trống", null));
        }
        return new ResponseMessage(new MessageContent(marineVesselInfoService.getLstMarineVesselId(mmsiLst)));
    }

    private MarineVesselRequestDTO buildMarineVesselRequestDTO(Map<String, Object> bodyParam) {
        ObjectMapper mapper = new ObjectMapper();
        Long mmsi = (bodyParam.get("mmsi") != "") && (bodyParam.get("mmsi") != null) ? ((Number) bodyParam.getOrDefault("mmsi", "")).longValue() : null;
        String name = (String) bodyParam.getOrDefault("name", "");
        String imo = (String) bodyParam.getOrDefault("imo", "");
        Long countryId = (bodyParam.get("countryId") != "") && (bodyParam.get("countryId") != null) ? ((Number) bodyParam.getOrDefault("countryId", "")).longValue() : 0;
        String typeId = (String) bodyParam.getOrDefault("typeId", "");
        Double dimA = bodyParam.get("dimA") != null ? ((Number) bodyParam.getOrDefault("dimA", "")).doubleValue() : null;
        Double dimC = bodyParam.get("dimC") != null ? ((Number) bodyParam.getOrDefault("dimC", "")).doubleValue() : null;
        String payroll = (String) bodyParam.getOrDefault("payroll", "");
        String description = (String) bodyParam.getOrDefault("description", "");
        String equipment = (String) bodyParam.getOrDefault("equipment", "");
        Long draught = (bodyParam.get("draught") != "") && (bodyParam.get("draught") != null) ? ((Number) bodyParam.getOrDefault("draught", "")).longValue() : null;
        Double grossTonnage = (bodyParam.get("grossTonnage") != "") && (bodyParam.get("grossTonnage") != null) ? ((Number) bodyParam.getOrDefault("grossTonnage", "")).doubleValue() : null;
        Double speedMax = (bodyParam.get("speedMax") != "") && (bodyParam.get("speedMax") != null) ? ((Number) bodyParam.getOrDefault("speedMax", "")).doubleValue() : null;
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
        MarineVesselRequestDTO marineVesselRequestDTO = MarineVesselRequestDTO.builder()
                .mmsi(mmsi)
                .name(name)
                .imo(imo)
                .countryId(countryId)
                .typeId(typeId)
                .dimA(dimA)
                .dimC(dimC)
                .payroll(payroll)
                .description(description)
                .equipment(equipment)
                .draught(draught)
                .grossTonnage(grossTonnage)
                .speedMax(speedMax)
                .sideId(sideId)
                .imageLst(imageLst)
                .fileAttachmentLst(fileAttachmentLst)
                .keywordLst(keywordLst)
                .relationshipLst(relationshipLst)
                .build();
        return marineVesselRequestDTO;
    }

    private MarineVesselFilterDTO buildMarineVesselFilterDTO(Map<String, Object> bodyParam) {
        Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
        Integer size = bodyParam.get("size") != null ? (Integer) bodyParam.get("size") : 20;
        String sort = (String) bodyParam.getOrDefault("sort", "");
        String term = (String) bodyParam.getOrDefault("term", "");
        String name = (String) bodyParam.getOrDefault("name", "");
        Long mmsi = (bodyParam.get("mmsi") != "") && (bodyParam.get("mmsi") != null) ? ((Number) bodyParam.getOrDefault("mmsi", "")).longValue() : null;
        List<Integer> countryIds = bodyParam.get("countryIds") != null ? (List<Integer>) bodyParam.get("countryIds") : null;
        List<String> sideIds = bodyParam.get("sideIds") != null ? (List<String>) bodyParam.get("sideIds") : null;
        List<String> keywordIds = bodyParam.get("keywordIds") != null ? (List<String>) bodyParam.get("keywordIds") : null;

        MarineVesselFilterDTO marineVesselFilterDTO = MarineVesselFilterDTO.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .term(term)
                .name(name)
                .mmsi(mmsi)
                .countryIds(countryIds)
                .sideIds(sideIds)
                .keywordIds(keywordIds)
                .build();
        return marineVesselFilterDTO;
    }

    private MarineVesselDTO entityToDto(MarineVesselInfo marineVesselInfo) {
        return modelMapper.map(marineVesselInfo, MarineVesselDTO.class);
    }

}
