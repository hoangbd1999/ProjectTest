package com.elcom.metacen.contact.controller;

import com.elcom.metacen.contact.constant.Constant;
import com.elcom.metacen.contact.model.KeywordData;
import com.elcom.metacen.contact.model.OtherVehicle;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.service.KeywordDataService;
import com.elcom.metacen.contact.service.OtherVehicleService;
import com.elcom.metacen.contact.validation.OtherVehicleValidation;
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
public class OtherVehicleController extends BaseController {

    @Autowired
    private OtherVehicleService otherVehicleService;

    @Autowired
    protected ModelMapper modelMapper;

    @Autowired
    private KeywordDataService keywordDataService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupController.class);

    public ResponseMessage insertOtherVehicle(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Create other vehicle with request >>> {}", bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "POST", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            OtherVehicleRequestDTO otherVehicleRequestDTO = buildOtherVehicleDTO(bodyParam);
            String validationMsg = new OtherVehicleValidation().validateOtherVehicle(otherVehicleRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }
            OtherVehicle otherVehicle = otherVehicleService.save(otherVehicleRequestDTO, dto.getUserName());
            if (otherVehicle != null) {
                Map<String, Object> bodyRelationship = new HashMap<>();
                ObjectDetailDTO objectDetailDTO = new ObjectDetailDTO();
                objectDetailDTO.setObjectId(otherVehicle.getId());
                objectDetailDTO.setObjectMmsi("");
                objectDetailDTO.setObjectUuid(otherVehicle.getUuid());
                objectDetailDTO.setObjectType(ObjectType.OTHER_VEHICLE.name());
                objectDetailDTO.setObjectName(otherVehicle.getName());
                ObjectRelationshipDetailDTO objectRelationshipDetailDTO = new ObjectRelationshipDetailDTO();
                objectRelationshipDetailDTO.setObject(objectDetailDTO);
                objectRelationshipDetailDTO.setRelationshipLst(otherVehicleRequestDTO.getRelationshipLst());
                bodyRelationship.put("object", objectRelationshipDetailDTO.getObject());
                bodyRelationship.put("relationshipLst", objectRelationshipDetailDTO.getRelationshipLst());
                callLinkObjectContains(bodyRelationship);
                return new ResponseMessage(new MessageContent(otherVehicle));
            } else {
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage updateOtherVehicle(Map<String, String> headerParam, Map<String, Object> bodyParam, String pathParam, String requestPath) {
        LOGGER.info("Update other vehicle id {} with request >>> {}", pathParam, bodyParam);

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
            OtherVehicle otherVehicle = otherVehicleService.findByUuid(uuid);
            if (otherVehicle == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Phương tiện không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Phương tiện không tồn tại", null));
            }
            OtherVehicleRequestDTO otherVehicleRequestDTO = buildOtherVehicleDTO(bodyParam);
            String validationMsg = new OtherVehicleValidation().validateOtherVehicle(otherVehicleRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            OtherVehicle result = otherVehicleService.updateOtherVehicle(otherVehicle, otherVehicleRequestDTO, dto.getUserName());
            Map<String, Object> bodyChangeName = new HashMap<>();
            bodyChangeName.put("objectUuid", otherVehicle.getUuid());
            bodyChangeName.put("objectName", otherVehicleRequestDTO.getName());
            bodyChangeName.put("objectId", otherVehicle.getId());
            bodyChangeName.put("objectType", ObjectType.OTHER_VEHICLE);
            this.ChangeNameVsatMediaDataObjectAnalyzed(bodyChangeName);
            ResponseMessage messageContent = this.ChangeNameMapping(bodyChangeName);
            String message = messageContent.getData().getData().toString();
            if(message != null){
                callLinkObjectUpdateNote(bodyChangeName);
            }
            if (result != null) {
                Map<String, Object> bodyRelationship = new HashMap<>();
                ObjectDetailDTO objectDetailDTO = new ObjectDetailDTO();
                objectDetailDTO.setObjectId(otherVehicle.getId());
                objectDetailDTO.setObjectMmsi("");
                objectDetailDTO.setObjectUuid(otherVehicle.getUuid());
                objectDetailDTO.setObjectType(ObjectType.OTHER_VEHICLE.name());
                objectDetailDTO.setObjectName(otherVehicle.getName());
                ObjectRelationshipDetailDTO objectRelationshipDetailDTO = new ObjectRelationshipDetailDTO();
                objectRelationshipDetailDTO.setObject(objectDetailDTO);
                objectRelationshipDetailDTO.setRelationshipLst(otherVehicleRequestDTO.getRelationshipLst());
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

    public ResponseMessage getOtherVehicleById(String requestPath, Map<String, String> headerParam, String pathParam) {
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
            OtherVehicleResponseDTO otherVehicleResponseDTO = otherVehicleService.findOtherVehicleByUuid(uuid);
            if (otherVehicleResponseDTO == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Phương tiện không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Phương tiện không tồn tại", null));
            }
            return new ResponseMessage(new MessageContent(otherVehicleResponseDTO));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage filterOtherVehicle(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
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
                OtherVehicleFilterDTO otherVehicleFilterDTO = buildOtherVehicleFilterDTO(bodyParam);
                String validationMsg = new OtherVehicleValidation().validateFilterOtherVehicle(otherVehicleFilterDTO);
                if (validationMsg != null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
                }
                Page<OtherVehicleResponseDTO> pagedResult = otherVehicleService.findListOtherVehicle(otherVehicleFilterDTO);
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

    public ResponseMessage deleteOtherVehicle(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DELETE", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            OtherVehicle otherVehicle = otherVehicleService.findByUuid(pathParam);
            if (otherVehicle == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Phương tiện không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Phương tiện không tồn tại", null));
            }
            otherVehicleService.delete(otherVehicle, dto.getUserName());
            Map<String, Object> bodyObject = new HashMap<>();
            bodyObject.put("objectUuid", otherVehicle.getUuid());
            callLinkObjectDeleteNode(bodyObject);
            List<KeywordData> keywordData = keywordDataService.findByRefId(pathParam);
            this.keywordDataService.delete(keywordData);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    private OtherVehicleRequestDTO buildOtherVehicleDTO(Map<String, Object> bodyParam) {
        ObjectMapper mapper = new ObjectMapper();
        String name = (String) bodyParam.getOrDefault("name", "");
        Double dimLength = bodyParam.get("dimLength") != null ? ((Number) bodyParam.getOrDefault("dimLength", "")).doubleValue() : null;
        Double dimWidth = bodyParam.get("dimWidth") != null ? ((Number) bodyParam.getOrDefault("dimWidth", "")).doubleValue() : null;
        Double dimHeight = bodyParam.get("dimHeight") != null ? ((Number) bodyParam.getOrDefault("dimHeight", "")).doubleValue() : null;
        Integer countryId = (bodyParam.get("countryId") != "") && (bodyParam.get("countryId") != null) ? ((Number) bodyParam.getOrDefault("countryId", "")).intValue() : 0;
        String description = (String) bodyParam.getOrDefault("description", "");
        Double tonnage = (bodyParam.get("tonnage") != "") && (bodyParam.get("tonnage") != null) ? ((Number) bodyParam.getOrDefault("tonnage", "")).doubleValue() : null;
        String payroll = (String) bodyParam.getOrDefault("payroll", "");
        String sideId = (String) bodyParam.getOrDefault("sideId", "");
        String equipment = (String) bodyParam.getOrDefault("equipment", "");
        Double speedMax = bodyParam.get("speedMax") != null ? ((Number) bodyParam.getOrDefault("speedMax", "")).doubleValue() : null;
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

        OtherVehicleRequestDTO otherVehicleRequestDTO = OtherVehicleRequestDTO.builder()
                .name(name)
                .dimLength(dimLength)
                .dimWidth(dimWidth)
                .dimHeight(dimHeight)
                .countryId(countryId)
                .description(description)
                .tonnage(tonnage)
                .payroll(payroll)
                .sideId(sideId)
                .equipment(equipment)
                .speedMax(speedMax)
                .imageLst(imageLst)
                .fileAttachmentLst(fileAttachmentLst)
                .keywordLst(keywordLst)
                .relationshipLst(relationshipLst)
                .build();

        return otherVehicleRequestDTO;
    }

    private OtherVehicleFilterDTO buildOtherVehicleFilterDTO(Map<String, Object> bodyParam) {
        Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
        Integer size = bodyParam.get("size") != null ? (Integer) bodyParam.get("size") : 20;
        String sort = (String) bodyParam.getOrDefault("sort", "");
        String term = (String) bodyParam.getOrDefault("term", "");
        List<Integer> countryIds = bodyParam.get("countryIds") != null ? (List<Integer>) bodyParam.get("countryIds") : null;
        List<String> sideIds = bodyParam.get("sideIds") != null ? (List<String>) bodyParam.get("sideIds") : null;
        List<String> keywordIds = bodyParam.get("keywordIds") != null ? (List<String>) bodyParam.get("keywordIds") : null;

        OtherVehicleFilterDTO otherVehicleFilterDTO = OtherVehicleFilterDTO.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .term(term)
                .countryIds(countryIds)
                .sideIds(sideIds)
                .keywordIds(keywordIds)
                .build();
        return otherVehicleFilterDTO;
    }
}
