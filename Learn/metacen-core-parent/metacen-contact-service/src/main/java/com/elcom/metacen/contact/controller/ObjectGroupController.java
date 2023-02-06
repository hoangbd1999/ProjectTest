package com.elcom.metacen.contact.controller;

import com.elcom.metacen.contact.constant.Constant;
import com.elcom.metacen.contact.model.ObjectGroup;
import com.elcom.metacen.contact.model.ObjectGroupMapping;
import com.elcom.metacen.contact.model.dto.ABACResponseDTO;
import com.elcom.metacen.contact.model.dto.AuthorizationResponseDTO;
import com.elcom.metacen.contact.model.dto.ObjectGroup.*;
import com.elcom.metacen.contact.service.ObjectGroupMappingService;
import com.elcom.metacen.contact.service.ObjectGroupService;
import com.elcom.metacen.contact.validation.ObjectGroupValidation;

import com.elcom.metacen.dto.redis.VsatVesselType;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Controller
public class ObjectGroupController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectGroupController.class);
    // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    public static DateTimeFormatter defaultFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private ObjectGroupService objectGroupService;

    @Autowired
    private ObjectGroupMappingService objectGroupMappingService;

    @Autowired
    protected ModelMapper modelMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    public ResponseMessage filterObjectGroupUnconfirmed(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
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
                ObjectGroupUnconfirmedFilterDTO objectGroupUnconfirmedFilterDTO = buildObjectGroupUnconfirmedFilterDTO(bodyParam);
                Page<ObjectGroupResponseDTO> pagedResult = objectGroupService.findListObjectGroupUnconfirmed(objectGroupUnconfirmedFilterDTO);
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

    public ResponseMessage filterObjectGroupConfirmed(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
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
                ObjectGroupConfirmedFilterDTO objectGroupConfirmedFilterDTO = buildObjectGroupConfirmedFilterDTO(bodyParam);
                Page<ObjectGroupResponseDTO> pagedResult = objectGroupService.findListObjectGroupConfirmed(objectGroupConfirmedFilterDTO);

                List<ObjectGroupResponseDTO> objectGroupLst = (List<ObjectGroupResponseDTO>) pagedResult.getContent();
                if (objectGroupLst == null || objectGroupLst.isEmpty()) {
                    return new ResponseMessage(HttpStatus.OK.value(), "200 OK", new MessageContent(HttpStatus.OK.value(), "200 OK", objectGroupLst));
                }

                try {
                    // get name of vessel type from redis
                    String key = Constant.REDIS_VESSEL_LST_KEY;

                    if (this.redisTemplate.hasKey(key)) {
                        List<VsatVesselType> vsatVesselTypesFromCaches = (List<VsatVesselType>) this.redisTemplate.opsForList().range(key, 0, Constant.REDIS_VESSEL_LST_FETCH_MAX);
                        if (vsatVesselTypesFromCaches != null && !vsatVesselTypesFromCaches.isEmpty()) {
                            for (ObjectGroupResponseDTO object : objectGroupLst) {
                                for (VsatVesselType vsatVesselType : vsatVesselTypesFromCaches) {
                                    for (ObjectGroupMappingDTO objectMapping : object.getObjects()) {
                                        if (vsatVesselType.getTypeCode().equals(objectMapping.getObjTypeId() + "")) {
                                            objectMapping.setObjTypeName(vsatVesselType.getTypeName());
                                            //break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                } catch (Exception ex) {
                    LOGGER.error("ex: ", ex);
                }
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

    public ResponseMessage insertObjectMapping(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Create object group with request >>> {}", bodyParam);

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
            ObjectGroupMappingRequestDTO requestDTO = buildObjectGroupMappingDTO(bodyParam);
            ObjectGroupMapping findObjectGroupMapping = objectGroupMappingService.findByObjIdAndGroupId(requestDTO.getObjId(), requestDTO.getGroupId());
            if (findObjectGroupMapping != null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Đối tượng đã tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Đối tượng đã tồn tại", null));
            }
            ObjectGroupMapping objectGroupMapping = objectGroupMappingService.save(requestDTO);
            if (objectGroupMapping != null) {
                return new ResponseMessage(new MessageContent(objectGroupMapping));
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
        LOGGER.info("Update object group id {} with request >>> {}", pathParam, bodyParam);

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
            ObjectGroup objectGroup = objectGroupService.findByUuid(uuid);
            if (objectGroup == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
            }
            ObjectGroupRequestDTO objectGroupRequestDTO = buildObjectGroupDTO(bodyParam);
            String validationMsg = new ObjectGroupValidation().validateObjectGroup(objectGroupRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }
            ObjectGroup findByName = objectGroupService.findByName(objectGroupRequestDTO.getName());
            if (findByName != null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Tên nhóm bị trùng, vui lòng đặt tên khác",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Tên nhóm bị trùng, vui lòng đặt tên khác", null));
            }
            ObjectGroup result = objectGroupService.update(objectGroup, objectGroupRequestDTO, dto.getUserName());
            if (result != null) {
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

    public ResponseMessage updateObjectGroupName(Map<String, String> headerParam, Map<String, Object> bodyParam, String pathParam, String requestPath) {
        LOGGER.info("Update object group id {} with request >>> {}", pathParam, bodyParam);

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
            ObjectGroup objectGroup = objectGroupService.findByUuid(uuid);
            if (objectGroup == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
            }
            String name = (String) bodyParam.getOrDefault("name", "");
            String validationMsg = new ObjectGroupValidation().validateObjectGroupName(name);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }
            ObjectGroup findByName = objectGroupService.findByName(name);

            ObjectGroup result = new ObjectGroup();
            if (findByName == null) {
                result = objectGroupService.updateObjectGroupName(objectGroup, dto.getUserName(), name);
            } else if (findByName != null) {
                if (findByName.getName().equalsIgnoreCase(objectGroup.getName())) {
                    result = objectGroupService.updateObjectGroupName(objectGroup, dto.getUserName(), name);
                } else {
                    return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Tên nhóm bị trùng, vui lòng đặt tên khác",
                            new MessageContent(HttpStatus.NOT_FOUND.value(), "Tên nhóm bị trùng, vui lòng đặt tên khác", null));
                }
            }
            if (result != null) {
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

    public ResponseMessage updateObjectMapping(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "PUT", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            String objId = (String) bodyParam.getOrDefault("objId", "");
            String groupId = (String) bodyParam.getOrDefault("groupId", "");
            ObjectGroupMapping objectGroupMapping = objectGroupMappingService.findByObjIdAndGroupId(objId, groupId);
            if (objectGroupMapping == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
            }
            String objNote = (String) bodyParam.getOrDefault("objNote", "");
            ObjectGroupMapping result = objectGroupMappingService.updateObjectMapping(objectGroupMapping, objNote);
            if (result != null) {
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

    public ResponseMessage delete(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DELETE", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            String uuid = pathParam;
            ObjectGroup objectGroup = objectGroupService.findByUuid(uuid);
            if (objectGroup == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
            }
            // delete object group mapping
             objectGroupMappingService.delete(uuid);
//            List<ObjectGroupMapping> objectGroupMapping = objectGroupMappingService.findByGroupId(uuid);
//            if (!objectGroupMapping.isEmpty()) {
//                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Nhóm đang có đối tượng, không được xóa!",
//                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Nhóm đang có đối tượng, không được xóa!", null));
//            }
            objectGroupService.delete(objectGroup);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage deleteUnconfirmed(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DELETE", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            String uuid = pathParam;
            ObjectGroup objectGroup = objectGroupService.findByUuid(uuid);
            if (objectGroup == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
            }
            // delete object group mapping
            objectGroupMappingService.delete(uuid);
            // delete object group
            objectGroupService.delete(objectGroup);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage deleteObjectMapping(String requestPath, Map<String, String> headerParam, Map<String, Object> bodyParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DELETE", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            String objId = (String) bodyParam.getOrDefault("objId", "");
            String groupId = (String) bodyParam.getOrDefault("groupId", "");
            ObjectGroupMapping objectGroupMapping = objectGroupMappingService.findByObjIdAndGroupId(objId, groupId);
            if (objectGroupMapping == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
            }
            // delete object group mapping
            objectGroupMappingService.delete(objectGroupMapping);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage checkExistObjectGroup(String pathParam) {
        String configUuid = pathParam;
        List<ObjectGroup> objectGroup = objectGroupService.findByConfigUuid(configUuid);
        if (objectGroup == null || objectGroup.isEmpty()) {
            return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại",
                    new MessageContent(HttpStatus.NOT_FOUND.value(), "Dữ liệu không tồn tại", null));
        } else {
            return new ResponseMessage(new MessageContent(objectGroup));
        }
    }

    private ObjectGroupUnconfirmedFilterDTO buildObjectGroupUnconfirmedFilterDTO(Map<String, Object> bodyParam) throws ParseException {
        Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
        Integer size = bodyParam.get("size") != null ? (Integer) bodyParam.get("size") : 20;
        String term = (String) bodyParam.getOrDefault("term", "");
        String termObject = (String) bodyParam.getOrDefault("termObject", "");
        String configName = (String) bodyParam.getOrDefault("configName", "");
        Date fromTime = (bodyParam.get("fromTime") != "") && (bodyParam.get("fromTime") != null) ? dateFormat.parse((String) bodyParam.get("fromTime") + ".000") : null;
        Date toTime = (bodyParam.get("toTime") != "") && (bodyParam.get("toTime") != null) ? dateFormat.parse((String) bodyParam.get("toTime") + ".999") : null;

        ObjectGroupUnconfirmedFilterDTO objectGroupUnconfirmedFilterDTO = ObjectGroupUnconfirmedFilterDTO.builder()
                .page(page)
                .size(size)
                .term(term)
                .termObject(termObject)
                .configName(configName)
                .fromTime(fromTime)
                .toTime(toTime)
                .build();
        return objectGroupUnconfirmedFilterDTO;
    }

    private ObjectGroupConfirmedFilterDTO buildObjectGroupConfirmedFilterDTO(Map<String, Object> bodyParam) throws ParseException {
        Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
        Integer size = bodyParam.get("size") != null ? (Integer) bodyParam.get("size") : 20;
        String name = (String) bodyParam.getOrDefault("name", "");
        String configName = (String) bodyParam.getOrDefault("configName", "");
        String term = (String) bodyParam.getOrDefault("term", "");
        Date fromTime = (bodyParam.get("fromTime") != "") && (bodyParam.get("fromTime") != null) ? dateFormat.parse((String) bodyParam.get("fromTime") + ".000") : null;
        Date toTime = (bodyParam.get("toTime") != "") && (bodyParam.get("toTime") != null) ? dateFormat.parse((String) bodyParam.get("toTime") + ".999") : null;

        ObjectGroupConfirmedFilterDTO objectGroupConfirmedFilterDTO = ObjectGroupConfirmedFilterDTO.builder()
                .page(page)
                .size(size)
                .name(name)
                .configName(configName)
                .term(term)
                .fromTime(fromTime)
                .toTime(toTime)
                .build();
        return objectGroupConfirmedFilterDTO;
    }

    private ObjectGroupRequestDTO buildObjectGroupDTO(Map<String, Object> bodyParam) {

        ObjectMapper mapper = new ObjectMapper();
        String name = (String) bodyParam.getOrDefault("name", "");
        String note = (String) bodyParam.getOrDefault("note", "");
        String configName = (String) bodyParam.getOrDefault("configName", "");
        String configUuid = (String) bodyParam.getOrDefault("configUuid", "");
        Integer configDistanceLevel = bodyParam.get("configDistanceLevel") != null ? (Integer) bodyParam.get("configDistanceLevel") : null;
        Integer configTogetherTime = bodyParam.get("configTogetherTime") != null ? (Integer) bodyParam.get("configTogetherTime") : null;

//        List<String> strings = ((List<String>) bodyParam.get("eventTimes"));
//
//        List<Date> eventTimes = strings.stream().map(x -> {
//            try {
//                return dateFormat.parse(x.toString());
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }).collect(Collectors.toList());

        List<ObjectGroupMappingDTO> objects = null;
        if (bodyParam.get("objects") != null) {
            objects = mapper.convertValue(
                    bodyParam.get("objects"),
                    new TypeReference<List<ObjectGroupMappingDTO>>() {
                    });
        }
        ObjectGroupRequestDTO objectGroupRequestDTO = ObjectGroupRequestDTO.builder()
                .name(name)
                .note(note)
                .configName(configName)
                .configUuid(configUuid)
                .configDistanceLevel(configDistanceLevel)
                .configTogetherTime(configTogetherTime)
                //   .eventTimes(eventTimes)
                .objects(objects)
                .build();

        return objectGroupRequestDTO;
    }

    private ObjectGroupMappingRequestDTO buildObjectGroupMappingDTO(Map<String, Object> bodyParam) {
        String objId = (String) bodyParam.getOrDefault("objId", "");
        String objName = (String) bodyParam.getOrDefault("objName", "");
        String groupId = (String) bodyParam.getOrDefault("groupId", "");

        ObjectGroupMappingRequestDTO objectGroupMappingRequestDTO = ObjectGroupMappingRequestDTO.builder()
                .objId(objId)
                .objName(objName)
                .groupId(groupId)
                .build();

        return objectGroupMappingRequestDTO;
    }

}
