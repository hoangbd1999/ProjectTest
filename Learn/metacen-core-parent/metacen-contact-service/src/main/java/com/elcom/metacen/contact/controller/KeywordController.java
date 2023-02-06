package com.elcom.metacen.contact.controller;

import com.elcom.metacen.contact.constant.Constant;
import com.elcom.metacen.contact.model.Keyword;
import com.elcom.metacen.contact.model.KeywordData;
import com.elcom.metacen.contact.model.ObjectKeyword;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.service.KeywordDataService;
import com.elcom.metacen.contact.service.KeywordService;
import com.elcom.metacen.contact.service.ObjectKeywordService;
import com.elcom.metacen.contact.validation.KeywordValidation;
import com.elcom.metacen.contact.validation.ObjectValidation;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.utils.StringUtil;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

@Controller
public class KeywordController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeywordController.class);

    @Autowired
    private KeywordService keywordService;

    @Autowired
    private KeywordDataService keywordDataService;

    @Autowired
    private ObjectKeywordService objectKeywordService;

    @Autowired
    protected ModelMapper modelMapper;

    public ResponseMessage findAll(Map<String, String> headerParam, String urlParam, String requestPath) {
        LOGGER.info("Find all with request >>> {}", urlParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "LIST", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            KeywordFilterDTO keywordFilterDTO = buildKeywordFilterRequest(urlParam);
            String validationMsg = new KeywordValidation().validateFilterKeyword(keywordFilterDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            Page<Keyword> pagedResult = keywordService.findAll(keywordFilterDTO);
            List<KeywordDTO> results = pagedResult.getContent()
                    .parallelStream()
                    .map(this::entityToDto)
                    .collect(Collectors.toList());

            return new ResponseMessage(new MessageContent(results, pagedResult.getTotalElements()));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }
    public ResponseMessage findAllKeyword() {
            List<Keyword> results = keywordService.findAll();
            return new ResponseMessage(new MessageContent(results, (long) results.size()));
    }

    public ResponseMessage findById(Map<String, String> headerParam, String pathParam, String requestPath) {
        LOGGER.info("Find keyword by id >>> {}", pathParam);

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
            Keyword keyword = keywordService.findById(id);
            if (keyword == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Keyword không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Keyword không tồn tại", null));
            }

            // get list objects
            // List<KeywordObjectTypeDTO> objects = buildObjects(id);
            // TODO: get list records
            KeywordDetailDTO keywordDetailDTO = KeywordDetailDTO.builder()
                    .id(keyword.getId())
                    .uuid(keyword.getUuid())
                    .name(keyword.getName())
                    .description(keyword.getDescription())
                    .objects(new ArrayList<>())
                    .records(new ArrayList<>())
                    .createdBy(keyword.getCreatedBy())
                    .createdDate(keyword.getCreatedDate())
                    .modifiedBy(keyword.getModifiedBy())
                    .modifiedDate(keyword.getModifiedDate())
                    .build();

            return new ResponseMessage(new MessageContent(keywordDetailDTO));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage create(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Create keyword with request >>> {}", bodyParam);

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

            KeywordRequestDTO keywordRequestDTO = buildKeywordRequestDTO(bodyParam);
            String validationMsg = new KeywordValidation().validateKeywordRequest(keywordRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }
            Keyword keywordCheckName = keywordService.findByName(keywordRequestDTO.getName());
            if(keywordCheckName != null){
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Tên từ khóa không được trùng nhau",
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), "Tên từ khóa không được trùng nhau", null));
            }
            Keyword keyword = keywordService.save(keywordRequestDTO);
            if (keyword != null) {
                return new ResponseMessage(new MessageContent(entityToDto(keyword)));
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
        LOGGER.info("Update keyword id {} with request >>> {}", pathParam, bodyParam);

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
            Keyword keyword = keywordService.findById(id);
            if (keyword == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Keyword không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Keyword không tồn tại", null));
            }

            if (bodyParam == null || bodyParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }

            KeywordRequestDTO keywordRequestDTO = buildKeywordRequestDTO(bodyParam);
            String validationMsg = new KeywordValidation().validateKeywordRequest(keywordRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            keyword.setName(keywordRequestDTO.getName());
            keyword.setDescription(keywordRequestDTO.getDescription());

            Keyword result = keywordService.update(keyword);
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

    public ResponseMessage delete(Map<String, String> headerParam, String pathParam, String requestPath) {
        LOGGER.info("Delete keyword by id >>> {}", pathParam);

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
            Keyword keyword = keywordService.findById(id);
            if (keyword == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Keyword không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Keyword không tồn tại", null));
            }

            Keyword result = keywordService.delete(keyword);
            if (result != null) {
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

    private KeywordFilterDTO buildKeywordFilterRequest(String urlParam) {
        Map<String, String> params = StringUtil.getUrlParamValues(urlParam);

        Integer page = params.get("page") != null ? Integer.parseInt(params.get("page")) : 0;
        Integer size = params.get("size") != null ? Integer.parseInt(params.get("size")) : 20;
        String term = params.getOrDefault("term", "");

        KeywordFilterDTO keywordFilterDTO = KeywordFilterDTO.builder()
                .page(page)
                .size(size)
                .term(term)
                .build();
        return keywordFilterDTO;
    }

    private KeywordRequestDTO buildKeywordRequestDTO(Map<String, Object> bodyParam) {
        String name = (String) bodyParam.getOrDefault("name", "");
        String description = (String) bodyParam.getOrDefault("description", "");

        KeywordRequestDTO keywordRequestDTO = KeywordRequestDTO.builder()
                .name(name)
                .description(description)
                .build();

        return keywordRequestDTO;
    }

    private List<KeywordObjectTypeDTO> buildObjects(String keywordId) {
        List<KeywordObjectTypeDTO> objects = new ArrayList<>();

        List<KeywordData> keywordDataList = keywordDataService.findByKeywordId(keywordId);
        Map<String, List<KeywordData>> keywordDataMap = keywordDataList.parallelStream().collect(Collectors.groupingBy(KeywordData::getRefType));
        keywordDataMap.forEach((refType, keywordDataLst) -> {
            List<ObjectDTO> objectDtoList = new ArrayList<>();
            keywordDataLst.forEach((item) -> {
                ObjectDTO objectDTO = new ObjectDTO();
                objectDTO.setRefId(item.getRefId());
                objectDTO.setRefName("");
                objectDtoList.add(objectDTO);
            });

            KeywordObjectTypeDTO keywordObjectTypeDTO = new KeywordObjectTypeDTO();
            keywordObjectTypeDTO.setType(refType);
            keywordObjectTypeDTO.setObjectList(objectDtoList);
            objects.add(keywordObjectTypeDTO);
        });

        return objects;
    }

    private KeywordDTO entityToDto(Keyword keyword) {
        KeywordDTO keywordDTO = KeywordDTO.builder()
                .id(keyword.getId())
                .uuid(keyword.getUuid())
                .name(keyword.getName())
                .description(keyword.getDescription())
                .createdBy(keyword.getCreatedBy())
                .createdDate(keyword.getCreatedDate())
                .modifiedBy(keyword.getModifiedBy())
                .modifiedDate(keyword.getModifiedDate())
                .build();

        return keywordDTO;
        // return modelMapper.map(keyword, KeywordDTO.class);
    }

    // create new keyword and grant for TTLL and object
    public ResponseMessage createKeywordGrant(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Create grant with request >>> {}", bodyParam);

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

            KeywordGrantRequestDTO keywordGrantRequestDTO = buildKeywordGrantRequestDTO(bodyParam);
            String validationMsg = new KeywordValidation().validateKeywordGrantRequest(keywordGrantRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }
            Keyword keyword = keywordService.insert(keywordGrantRequestDTO);
            KeywordData keywordData = keywordDataService.insert(keywordGrantRequestDTO,keyword.getUuid());

            if (keyword != null && keywordData != null) {
                return new ResponseMessage(new MessageContent(keywordGrantRequestDTO));
            } else {
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage createKeywordData(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Create keyword data with request >>> {}", bodyParam);

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

            KeywordDataRequestDTO keywordDataRequestDTO = buildKeywordDataRequestDTO(bodyParam);
            String validationMsg = new KeywordValidation().validateKeywordDataRequest(keywordDataRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }
            KeywordData keywordData = keywordDataService.save(keywordDataRequestDTO);

            if (keywordData != null) {
                return new ResponseMessage(new MessageContent(keywordDataRequestDTO));
            } else {
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage insertKeywordDataInternal(Map<String, Object> bodyParam) {
            if (bodyParam == null || bodyParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }
            KeywordDataRequestDTO keywordDataRequestDTO = buildKeywordDataRequestDTO(bodyParam);
            String validationMsg = new KeywordValidation().validateKeywordDataRequest(keywordDataRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }
            KeywordData keywordData = null;
            KeywordData data = keywordDataService.findByRefIdAndType(keywordDataRequestDTO.getRefId(),keywordDataRequestDTO.getKeywordIds(),keywordDataRequestDTO.getType());
            if(data != null){
                return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK.toString(),
                        new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(), null));
            } else {
                keywordData = keywordDataService.save(keywordDataRequestDTO);
            }
            if (keywordData != null) {
                return new ResponseMessage(new MessageContent(keywordDataRequestDTO));
            } else {
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
    }

    public ResponseMessage deleteKeywordData(Map<String, String> headerParam,Map<String, Object> bodyParam, String requestPath) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DELETE", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            String refId = (String) bodyParam.getOrDefault("refId", "");
            List<String> keywordIds = bodyParam.get("keywordIds") != null ? (List<String>) bodyParam.get("keywordIds") : null;
            List<KeywordData> keywordData = null;
            for (int i = 0; i < keywordIds.size(); i++) {
                keywordData = keywordDataService.findByRefId(refId,keywordIds.get(i));
            }
            if (keywordData == null) {
                return new ResponseMessage(HttpStatus.OK.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.OK.value(), "Dữ liệu không tồn tại", null));
            }
            this.keywordDataService.delete(keywordData);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    // get keyword-data of media
    public ResponseMessage getKeywordDataMedia(String requestPath, Map<String, String> headerParam, String urlParam) {
        LOGGER.info("Find keyword by type >>> {}", urlParam);
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DETAIL", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            Map<String, String> params = StringUtil.getUrlParamValues(urlParam);
            String refId = params.get("refId");
            Integer type = Integer.parseInt(params.get("type"));

            List<KeywordData> keywordData = keywordDataService.findByRefIdAndType(refId,type);
            List<KeywordDataResponseDTO> listData = keywordData.stream().map(data
                    -> modelMapper.map(data, KeywordDataResponseDTO.class)).collect(Collectors.toList());
            for (int i = 0; i < keywordData.size(); i++) {
                Keyword keyword = keywordService.findById(keywordData.get(i).getKeywordId());
                KeywordDTO keywordDTO = modelMapper.map(keyword, KeywordDTO.class);
                listData.get(i).setKeyword(keywordDTO);
            }
            if (keywordData.isEmpty()) {
                return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "200 OK", null));
            }
            return new ResponseMessage(new MessageContent(listData));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    // get the object list of keywords
    public ResponseMessage getKeywordDataObject(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Get object with request >>> {}", bodyParam);
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
            KeyworDataObject keyworDataObject = buildKeyworDataCriteria(bodyParam);
            String validationMsg = new KeywordValidation().validateKeywordDataObject(keyworDataObject);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            Page<KeywordDataObjectGeneralInfoDTO> pagedResult = keywordDataService.getKeywordDataObject(keyworDataObject);
//            List<KeywordDataObjectGeneralInfoDTO> listData = pagedResult.stream().map(data
//                    -> modelMapper.map(data, KeywordDataObjectGeneralInfoDTO.class)).collect(Collectors.toList());
//            for (int i = 0; i < listData.size(); i++) {
//                listData.get(i).setNumberOfKeyword(listData.get(i).getKeywordIds().size());
//            }
//            Collections.sort(listData, (f1, f2) -> {
//                return f2.getNumberOfKeyword().compareTo(f1.getNumberOfKeyword());
//            });
            return new ResponseMessage(new MessageContent(pagedResult.getContent(), pagedResult.getTotalElements()));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    private KeywordGrantRequestDTO buildKeywordGrantRequestDTO(Map<String, Object> bodyParam) {
        String name = (String) bodyParam.getOrDefault("name", "");
        Integer type = bodyParam.get("type") != null ? (Integer) bodyParam.get("type") : null;
        String refId = (String) bodyParam.getOrDefault("refId", "");
        String refType = (String) bodyParam.getOrDefault("refType", "");

        KeywordGrantRequestDTO keywordGrantRequestDTO = KeywordGrantRequestDTO.builder()
                .name(name)
                .type(type)
                .refId(refId)
                .refType(refType)
                .build();

        return keywordGrantRequestDTO;
    }

    private KeywordDataRequestDTO buildKeywordDataRequestDTO(Map<String, Object> bodyParam) {
        Integer type = bodyParam.get("type") != null ? (Integer) bodyParam.get("type") : null;
        String refId = (String) bodyParam.getOrDefault("refId", "");
        String refType = (String) bodyParam.getOrDefault("refType", "");
        List<String> keywordIds = bodyParam.get("keywordIds") != null ? (List<String>) bodyParam.get("keywordIds") : null;

        KeywordDataRequestDTO keywordDataRequestDTO = KeywordDataRequestDTO.builder()
                .type(type)
                .refId(refId)
                .refType(refType)
                .keywordIds(keywordIds)
                .build();

        return keywordDataRequestDTO;
    }

    private KeyworDataObject buildKeyworDataCriteria(Map<String, Object> bodyParam) {
        Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
        Integer size = bodyParam.get("size") != null ? (Integer) bodyParam.get("size") : 20;
        String term = (String) bodyParam.getOrDefault("term", "");
        List<String> objectTypeLst = bodyParam.get("objectTypeLst") != null ? (List<String>) bodyParam.get("objectTypeLst") : null;
        List<String> keywordIds = bodyParam.get("keywordIds") != null ? (List<String>) bodyParam.get("keywordIds") : null;
        List<String> objectIds = bodyParam.get("objectIds") != null ? (List<String>) bodyParam.get("objectIds") : null;
        if(!StringUtil.isNullOrEmpty(term) || objectTypeLst != null && !objectTypeLst.isEmpty()){
            objectIds = Collections.<String>emptyList();
        }

        KeyworDataObject keyworDataObject = KeyworDataObject.builder()
                .page(page)
                .size(size)
                .term(term)
                .objectTypeLst(objectTypeLst)
                .keywordIds(keywordIds)
                .objectIds(objectIds)
                .build();
        return keyworDataObject;
    }
}
