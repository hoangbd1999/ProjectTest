package com.elcom.metacen.contact.controller;

import com.elcom.metacen.contact.model.Group;
import com.elcom.metacen.contact.model.MarineVesselInfo;
import com.elcom.metacen.contact.model.People;
import com.elcom.metacen.contact.model.dto.ABACResponseDTO;
import com.elcom.metacen.contact.model.dto.AuthorizationResponseDTO;
import com.elcom.metacen.contact.model.dto.GroupDTO.*;
import com.elcom.metacen.contact.repository.MarineVesselInfoCustomRepository;
import com.elcom.metacen.contact.repository.PeopleCustomRepository;
import com.elcom.metacen.contact.service.GroupObjectMappingService;
import com.elcom.metacen.contact.service.GroupService;
import com.elcom.metacen.contact.validation.GroupValidation;
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

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class GroupController extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupController.class);

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupObjectMappingService groupObjectMappingService;

    @Autowired
    private PeopleCustomRepository peopleCustomRepository;

    @Autowired
    private MarineVesselInfoCustomRepository marineVesselInfoCustomRepository;

    @Autowired
    private ModelMapper modelMapper;

    public ResponseMessage insertGroup(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Create group with request >>> {}", bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "POST", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            GroupDTO groupDTO = buildGroupDTO(bodyParam);
            String validationMsg = new GroupValidation().validateGroup(groupDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }
            Group group = groupService.save(groupDTO, dto.getUserName());
            if (group != null) {
                return new ResponseMessage(new MessageContent(entityToDto(group)));
            } else {
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage updateGroup(Map<String, String> headerParam, Map<String, Object> bodyParam, String pathParam, String requestPath) {
        LOGGER.info("Update group id {} with request >>> {}", pathParam, bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "PUT", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            String uuidKey = pathParam;
            Group group = groupService.findById(uuidKey);
            if (group == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Nhóm không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Nhóm không tồn tại", null));
            }
            GroupDTO groupDTO = buildGroupDTO(bodyParam);
            String validationMsg = new GroupValidation().validateGroup(groupDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }
            Group result = groupService.updateGroup(group, groupDTO, dto.getUserName());
            if (result != null) {
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

//    public ResponseMessage getGroupById(String requestPath, Map<String, String> headerParam, String pathParam) {
//        // Check isLogged
//        AuthorizationResponseDTO dto = authenToken(headerParam);
//        if (dto == null) {
//            return unauthorizedResponse();
//        }
//        // Check ABAC
//        Map<String, Object> body = new HashMap<String, Object>();
//        ABACResponseDTO abacStatus = authorizeABAC(body, "DETAIL", dto.getUuid(), requestPath);
//
//        if (abacStatus != null && abacStatus.getStatus()) {
//            Group group = groupService.findById(pathParam);
//            if (group == null) {
//                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Nhóm không tồn tại",
//                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Nhóm không tồn tại", null));
//            }
//            return new ResponseMessage(new MessageContent(group));
//        } else {
//            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
//                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
//        }
//    }

    public ResponseMessage getGroupList(String requestPath, Map<String, String> headerParam, String urlParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "LIST", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            try {
                GroupFilterDTO groupFilterDTO = buildGroupFilterDTO(urlParam);
                String validationMsg = new GroupValidation().validateFilterGroup(groupFilterDTO);
                if (validationMsg != null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
                }
                Page<Group> pagedResult = groupService.findListGroup(groupFilterDTO);
                List<GroupDetailDTO> results = pagedResult.getContent()
                        .parallelStream()
                        .map(this::entityToDtoList)
                        .collect(Collectors.toList());
                return new ResponseMessage(new MessageContent(results, pagedResult.getTotalElements()));

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

    public ResponseMessage deleteGroup(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DELETE", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            Group group = groupService.findById(pathParam);
            if (group == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Nhóm không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Nhóm không tồn tại", null));
            }
            this.groupService.delete(group);
            this.groupObjectMappingService.updateIsDelete(UUID.fromString(pathParam));
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    private GroupDTO buildGroupDTO(Map<String, Object> bodyParam) {
        String name = (String) bodyParam.getOrDefault("name", "");
        String note = (String) bodyParam.getOrDefault("note", "");
        String sideId = (String) bodyParam.getOrDefault("sideId", "");
        List list = (List) bodyParam.get("groupObject");
        ObjectMapper mapper = new ObjectMapper();
        List<GroupObjectMappingDTO> ObjectList = mapper.convertValue(
                list,
                new TypeReference<List<GroupObjectMappingDTO>>() {
                }
        );

        GroupDTO groupDTO = GroupDTO.builder()
                .name(name)
                .note(note)
                .sideId(sideId)
                .groupObject(ObjectList)
                .build();

        return groupDTO;
    }

    private GroupFilterDTO buildGroupFilterDTO(String urlParam) {
        Map<String, String> params = StringUtil.getUrlParamValues(urlParam);
        Integer page = params.get("page") != null ? Integer.parseInt(params.get("page")) : 0;
        Integer size = params.get("size") != null ? Integer.parseInt(params.get("size")) : 20;
        String term = params.getOrDefault("term", "");

        GroupFilterDTO groupFilterDTO = GroupFilterDTO.builder()
                .page(page)
                .size(size)
                .term(term)
                .build();
        return groupFilterDTO;
    }

    private GroupRequestDTO entityToDto(Group group) {
        GroupRequestDTO groupRequestDTO = GroupRequestDTO.builder()
                .id(group.getId())
                .uuidKey(group.getUuidKey())
                .name(group.getName())
                .note(group.getNote())
                .createdBy(group.getModifiedBy())
                .createdDate(group.getCreatedDate())
                .modifiedDate(group.getModifiedDate())
                .modifiedBy(group.getModifiedBy())
                .sideId(group.getSideId())
                .groupObject(group.getGroupObject())
                .build();

        return groupRequestDTO;
    }

    private GroupDetailDTO entityToDtoList(Group group) {
        GroupDetailDTO groupDetailDTO = GroupDetailDTO.builder()
                .id(group.getId())
                .uuidKey(group.getUuidKey())
                .name(group.getName())
                .note(group.getNote())
                .createdBy(group.getModifiedBy())
                .createdDate(group.getCreatedDate())
                .modifiedDate(group.getModifiedDate())
                .modifiedBy(group.getModifiedBy())
                .sideId(group.getSideId())
                .groupObject(group.getGroupObject())
                .numberTotal((long) group.getGroupObject().size())
                .build();

        return groupDetailDTO;
    }

    public ResponseMessage getGroupById(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DETAIL", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            Group group = groupService.findById(pathParam);
            if (group == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Nhóm không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Nhóm không tồn tại", null));
            }

            List<GroupObjectMappingDTO> list = group.getGroupObject();
            HashMap<String, List<String>> mapGroupObject = new HashMap<>();
            HashSet<String> setGroupObject = new HashSet();
            list.stream().forEach(n -> setGroupObject.add(n.getObjectType()));
            for (String key : setGroupObject) {
                List<String> listSub = new ArrayList<>();
                list.stream().forEach(n -> {
                    if (n.getObjectType().equalsIgnoreCase(key)) {
                        listSub.add(n.getObjectId());
                    }
                });
                mapGroupObject.put(key, listSub);
            }
            GroupFindByIdResponse response = modelMapper.map(group, GroupFindByIdResponse.class);
            response.getGroupObject().clear();
            Set<String> set = mapGroupObject.keySet();
            for (String key : set) {
                if (key.equalsIgnoreCase("PEOPLE")) {
                    List<People> listPeople = peopleCustomRepository.findListPeople(mapGroupObject.get(key));
                    listPeople.stream().forEach(n -> {
                        GroupObjectDTO groupObjectDTO = modelMapper.map(n, GroupObjectDTO.class);
                        groupObjectDTO.setObjectId(n.getUuid());
                        groupObjectDTO.setObjectType(key);
                        response.getGroupObject().add(groupObjectDTO);
                    });
                }
                if (key.equalsIgnoreCase("VESSEL")) {
                    List<MarineVesselInfo> listVessel = marineVesselInfoCustomRepository.findListMarineVessel(mapGroupObject.get(key));
                    listVessel.stream().forEach(n -> {
                        GroupObjectDTO groupObjectDTO = modelMapper.map(n, GroupObjectDTO.class);
                        groupObjectDTO.setObjectId(n.getUuid());
                        groupObjectDTO.setObjectType(key);
                        response.getGroupObject().add(groupObjectDTO);
                    });
                }
            }
            return new ResponseMessage(new MessageContent(response));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }
}
