package com.elcom.metacen.id.controller;

import com.elcom.metacen.id.constant.Constant;
import com.elcom.metacen.id.model.Unit;
import com.elcom.metacen.id.model.User;
import com.elcom.metacen.id.model.dto.ABACResponseDTO;
import com.elcom.metacen.id.model.dto.AuthorizationResponseDTO;
import com.elcom.metacen.id.service.StageService;
import com.elcom.metacen.id.service.UnitService;
import com.elcom.metacen.id.service.UserService;
import com.elcom.metacen.id.validation.UnitValidation;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.utils.StringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Controller
public class UnitController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitController.class);

    @Autowired
    private UnitService unitService;

    @Autowired
    private StageService stageService;

    @Autowired
    private UserService userService;

    public ResponseMessage getGroupsList(Map<String, String> headerParam, String requestPath, String method, String urlParam, Map<String, Object> bodyParam) {
        ResponseMessage response = null;
        AuthorizationResponseDTO dto = getAuthorFromToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập", null));
        } else {
            //Check RBAC quyền xử lý vi phạm
            ABACResponseDTO abacResponseDTO = authorizeABAC(bodyParam, "LIST", dto.getUuid(), requestPath);
            if (abacResponseDTO != null && abacResponseDTO.getStatus()) {
                Pageable pageable = null;
                String search = "";
                Page<Unit> unitList = null;
                if (!StringUtil.isNullOrEmpty(urlParam)) {
                    Map<String, String> params = StringUtil.getUrlParamValues(urlParam);
                    Integer page = params.get("page") != null ? Integer.parseInt(params.get("page")) : null;
                    Integer size = params.get("size") != null ? Integer.parseInt(params.get("size")) : null;
                    search = params.get("search");
                    pageable = PageRequest.of(page, size);
                } else {
                    pageable = PageRequest.of(0, 20);
                }
                unitList = unitService.findAll(pageable.getPageNumber(), pageable.getPageSize(), search, Sort.by("createdDate").descending());

                if (unitList.isEmpty()) {
                    response = new ResponseMessage(new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(), null));
                } else {
                    response = new ResponseMessage(new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(), unitList.getContent(), unitList.getTotalElements()));
                }
            } else {
                response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(),
                        "Bạn không có quyền xem danh sách đội", null));
            }
        }
        return response;
    }

    public ResponseMessage getUnitById(Map<String, String> headerParam, String requestPath, String method, String pathParam, Map<String, Object> bodyParam) {
        ResponseMessage response = null;
        AuthorizationResponseDTO dto = getAuthorFromToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập", null));
        } else {
            ABACResponseDTO abacResponseDTO = authorizeABAC(bodyParam, "DETAIL", dto.getUuid(), requestPath);
            if (abacResponseDTO != null && abacResponseDTO.getStatus()) {
                Unit unit = unitService.findByUuid(pathParam);
                if (unit == null) {
                    response = new ResponseMessage(HttpStatus.NOT_FOUND.value(), Constant.VALIDATION_DATA_NOT_FOUND, new MessageContent(HttpStatus.NOT_FOUND.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
                } else {
                    response = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), new MessageContent(unit));
                }
            } else {
                response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(),
                        "Bạn không có quyền xem danh sách đội", null));
            }
        }
        return response;
    }

    public ResponseMessage deleteByListId(Map<String, String> headerParam, String requestPath, String method, String pathParam, Map<String, Object> bodyParam) {
        ResponseMessage response = null;
        AuthorizationResponseDTO dto = getAuthorFromToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập", null));
        } else {
            ABACResponseDTO abacResponseDTO = authorizeABAC(bodyParam, "DELETE", dto.getUuid(), requestPath);
            if (abacResponseDTO != null && abacResponseDTO.getStatus()) {
                try {
                    Boolean isDelete = true;
                    List<String> uuids = (List<String>) bodyParam.get("uuids");
                    for (String uuid : uuids) {
                        List<User> userList = userService.findByGroup(uuid);
                        if (!CollectionUtils.isEmpty(userList)) {
                            isDelete = false;
                        }
                    }
                    if (isDelete) {
                        List<Unit> listUnits = unitService.deleteByListId(uuids);
                        publishDeletedListUnit(listUnits);
                        return new ResponseMessage(new MessageContent(HttpStatus.OK.value()));
                    } else {
                        return new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Đội tồn tại người dùng. Vui lòng kiểm tra lại", null));
                    }
                } catch (Exception e) {
                    response = new ResponseMessage(HttpStatus.NOT_FOUND.value(), Constant.VALIDATION_DATA_NOT_FOUND, new MessageContent(HttpStatus.NOT_FOUND.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
                }
            } else {
                response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(),
                        "Bạn không có quyền xóa đội", null));
            }
        }
        return response;
    }

    public ResponseMessage createUnit(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath, String requestMethod) {
        ResponseMessage response = null;
        AuthorizationResponseDTO dto = getAuthorFromToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập", null));
        } else {
            ABACResponseDTO abacResponseDTO = authorizeABAC(bodyParam, "POST", dto.getUuid(), requestPath);
            if (abacResponseDTO != null && abacResponseDTO.getStatus()) {
                if (bodyParam == null || bodyParam.isEmpty()) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
                } else {
                    String code = (String) bodyParam.get("code");
                    String name = (String) bodyParam.get("name");
                    String address = (String) bodyParam.get("address");
                    String phone = (String) bodyParam.get("phone");
                    String email = (String) bodyParam.get("email");
                    String description = (String) bodyParam.get("description");
                    String lisOfStage = (String) bodyParam.get("lisOfStage");
                    String listOfJob = (String) bodyParam.get("listOfJob");

                    Unit unit = new Unit(UUID.randomUUID().toString());

                    unit.setCode(code);
                    unit.setName(name);
                    unit.setAddress(address);
                    if (!StringUtil.isNullOrEmpty(phone)) {
                        if (phone.startsWith("+84")) {
                            phone = phone.replace("+84", "0");
                        } else if (phone.startsWith("84") && phone.length() == 11) {
                            phone = phone.replaceFirst("84", "0");
                        }
                    }
                    unit.setPhone(phone);
                    unit.setEmail(email);
                    unit.setDescription(description);
                    unit.setLisOfStage(lisOfStage);
                    unit.setListOfJob(listOfJob);
                    unit.setCreatedDate(new Date());
                    unit.setCreatedBy(dto.getUuid());

                    String invalidData = new UnitValidation().validateInsertUnit(unit);
                    if (invalidData != null) {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), invalidData,
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), invalidData, null));
                    } else {
                        Unit existUser = null;
                        //Check email exist
                        if (StringUtil.validateEmail(unit.getEmail())) {
                            existUser = unitService.findByEmail(unit.getEmail());
                        }
                        if (existUser != null) {
                            invalidData = "Đã tồn tại email " + unit.getEmail();
                            response = new ResponseMessage(HttpStatus.CONFLICT.value(), invalidData,
                                    new MessageContent(HttpStatus.CONFLICT.value(), invalidData,
                                            null));
                        } else {
                            //Check mobile exist
                            if (StringUtil.checkMobilePhoneNumberNew(unit.getPhone())) {
                                existUser = unitService.findByPhone(unit.getPhone());
                            }
                            if (existUser != null) {
                                invalidData = "Đã tồn tại số điện thoại " + unit.getPhone();
                                response = new ResponseMessage(HttpStatus.CONFLICT.value(), invalidData,
                                        new MessageContent(HttpStatus.CONFLICT.value(), invalidData,
                                                null));
                            } else {
                                //Check user_name exist
                                if (!StringUtil.isNullOrEmpty(unit.getName())) {
                                    existUser = unitService.findByName(unit.getName());
                                }
                                if (existUser != null) {
                                    invalidData = "Đã tồn tại tên đơn vị " + unit.getName();
                                    response = new ResponseMessage(HttpStatus.CONFLICT.value(), invalidData,
                                            new MessageContent(HttpStatus.CONFLICT.value(), invalidData,
                                                    null));
                                } else {
                                    if (!StringUtil.isNullOrEmpty(unit.getCode())) {
                                        existUser = unitService.findByCode(unit.getCode());
                                    }
                                    if (existUser != null) {
                                        invalidData = "Đã tồn tại mã đơn vị " + unit.getCode();
                                        response = new ResponseMessage(HttpStatus.CONFLICT.value(), invalidData,
                                                new MessageContent(HttpStatus.CONFLICT.value(), invalidData,
                                                        null));
                                    } else {
                                        try {
                                            unitService.save(unit);
                                            response = new ResponseMessage(HttpStatus.CREATED.value(), HttpStatus.CREATED.getReasonPhrase(), new MessageContent(unit));
                                        } catch (Exception ex) {
                                            response = new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                                    new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), null));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(),
                        "Bạn không có quyền tạo đơn vị", null));
            }
        }
        return response;
    }

    public ResponseMessage updateGroup(String requestPath, Map<String, String> headerParam, Map<String, Object> bodyParam, String requestMethod, String pathParam) {
        ResponseMessage response = null;
        AuthorizationResponseDTO dto = getAuthorFromToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập", null));
        } else {
            ABACResponseDTO abacResponseDTO = authorizeABAC(bodyParam, "PUT", dto.getUuid(), requestPath);
            if (abacResponseDTO != null && abacResponseDTO.getStatus()) {
                if (bodyParam == null || bodyParam.isEmpty()) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
                } else {
                    String code = (String) bodyParam.get("code");
                    String name = (String) bodyParam.get("name");
                    String address = (String) bodyParam.get("address");
                    String phone = (String) bodyParam.get("phone");
                    String email = (String) bodyParam.get("email");
                    String description = (String) bodyParam.get("description");
                    String lisOfStage = (String) bodyParam.get("lisOfStage");
                    String listOfJob = (String) bodyParam.get("listOfJob");

                    Unit unit = new Unit();

                    unit.setUuid(pathParam);
                    unit.setCode(code);
                    unit.setName(name);
                    unit.setAddress(address);
                    if (!StringUtil.isNullOrEmpty(phone)) {
                        if (phone.startsWith("+84")) {
                            phone = phone.replace("+84", "0");
                        } else if (phone.startsWith("84") && phone.length() == 11) {
                            phone = phone.replaceFirst("84", "0");
                        }
                    }
                    unit.setPhone(phone);
                    unit.setEmail(email);
                    unit.setDescription(description);
                    unit.setLisOfStage(lisOfStage);
                    unit.setListOfJob(listOfJob);

                    String invalidData = new UnitValidation().validateUpdateUnit(unit);
                    if (invalidData != null) {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), invalidData,
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), invalidData, null));
                    } else {
                        Unit existUser = null;
                        //Check email exist
                        if (StringUtil.validateEmail(unit.getEmail())) {
                            existUser = unitService.findByEmail(unit.getEmail());
                        }
                        if (existUser != null && !existUser.getUuid().equalsIgnoreCase(pathParam)) {
                            invalidData = "Đã tồn tại email " + unit.getEmail();
                            response = new ResponseMessage(HttpStatus.CONFLICT.value(), invalidData,
                                    new MessageContent(HttpStatus.CONFLICT.value(), invalidData,
                                            null));
                        } else {
                            //Check mobile exist
                            if (StringUtil.checkMobilePhoneNumberNew(unit.getPhone())) {
                                existUser = unitService.findByPhone(unit.getPhone());
                            }
                            if (existUser != null && !existUser.getUuid().equalsIgnoreCase(pathParam)) {
                                invalidData = "Đã tồn tại số điện thoại " + unit.getPhone();
                                response = new ResponseMessage(HttpStatus.CONFLICT.value(), invalidData,
                                        new MessageContent(HttpStatus.CONFLICT.value(), invalidData,
                                                null));
                            } else {
                                //Check user_name exist
                                if (!StringUtil.isNullOrEmpty(unit.getName())) {
                                    existUser = unitService.findByName(unit.getName());
                                }
                                if (existUser != null && !existUser.getUuid().equalsIgnoreCase(pathParam)) {
                                    invalidData = "Đã tồn tại tên đơn vị " + unit.getName();
                                    response = new ResponseMessage(HttpStatus.CONFLICT.value(), invalidData,
                                            new MessageContent(HttpStatus.CONFLICT.value(), invalidData,
                                                    null));
                                } else {
                                    if (!StringUtil.isNullOrEmpty(unit.getCode())) {
                                        existUser = unitService.findByCode(unit.getCode());
                                    }
                                    if (existUser != null && !existUser.getUuid().equalsIgnoreCase(pathParam)) {
                                        invalidData = "Đã tồn tại mã đơn vị " + unit.getCode();
                                        response = new ResponseMessage(HttpStatus.CONFLICT.value(), invalidData,
                                                new MessageContent(HttpStatus.CONFLICT.value(), invalidData,
                                                        null));
                                    } else {
                                        Unit unitDetail = unitService.findByUuid(pathParam);
                                        if (unitDetail != null) {
                                            unitService.save(unit);
                                            response = new ResponseMessage(new MessageContent(unit));
                                        } else {
                                            response = new ResponseMessage(new MessageContent(HttpStatus.BAD_REQUEST.value(), "Không tìm thấy unit tương ứng với id: " + pathParam, null));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(),
                        "Bạn không có quyền sửa đội", null));
            }
        }
        return response;
    }

    public ResponseMessage deleteUnit(String requestPath, Map<String, String> headerParam, Map<String, Object> bodyParam, String requestMethod, String pathParam) throws JsonProcessingException {
        ResponseMessage response = null;
        AuthorizationResponseDTO dto = getAuthorFromToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập", null));
        } else {
            //Check RBAC quyền xử lý vi phạm
            ABACResponseDTO abacResponseDTO = authorizeABAC(bodyParam, "DELETE", dto.getUuid(), requestPath);
            if (abacResponseDTO != null && abacResponseDTO.getStatus()) {
                List<User> userList = userService.findByGroup(pathParam);
                if (CollectionUtils.isEmpty(userList)) {
                    Unit unit = unitService.deleteByUuid(pathParam);
                    if (unit == null) {
                        return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Không tìm thấy id tương ứng: " + pathParam, null);
                    } else {
                        response = new ResponseMessage(new MessageContent(unit));
                        publishDeletedUnit(unit);
                    }
                } else {
                    response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(),
                            "Đội tồn tại người dùng. Vui lòng kiểm tra lại", null));
                }
            } else {
                response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(),
                        "Bạn không có quyền xóa đội", null));
            }
        }
        return response;
    }

    public ResponseMessage getGroupsByEventList(Map<String, String> headerParam, String requestPath,
            String method, String urlParam, Map<String, Object> bodyParam) {
//        ResponseMessage response = null;
//        AuthorizationResponseDTO dto = getAuthorFromToken(headerParam);
//        if (dto == null) {
//            response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập", null));
//        } else {
//            //Check RBAC quyền xử lý vi phạm
//            ABACResponseDTO abacResponseDTO = authorizeABAC(bodyParam, "LIST", dto.getUuid(), requestPath);
//            if (abacResponseDTO != null) {
//                if (!StringUtil.isNullOrEmpty(urlParam)) {
//                    Map<String, String> params = StringUtil.getUrlParamValues(urlParam);
//                    String siteId = params.get("siteId");
//                    String listSite = params.get("listSite");
//                    String jobType = params.get("jobType");
//                    String siteIdList = siteId;
//                    if (!StringUtil.isNullOrEmpty(listSite)) {
//                        siteIdList += "," + listSite;
//                    }
//                    List<String> stageIdList = stageService.findIdListBySiteIdList(siteIdList);
//                    if (stageIdList != null && !stageIdList.isEmpty()) {
//                        List<Unit> unitList = unitService.findUnitByStageIdList(stageIdList);
//                        List<UnitDTO> unitDTOList = unitService.transformWithPermission(unitList, jobType);
//                        response = new ResponseMessage(new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(), unitDTOList));
//                    } else {
//                        response = new ResponseMessage(new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(), null));
//                    }
//                } else {
//                    response = new ResponseMessage(new MessageContent(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.toString(), null));
//                }
//            } else {
//                response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(),
//                        "Bạn không có quyền xem danh sách đội", null));
//            }
//        }
//        return response;
        return null;
    }

    public ResponseMessage getGroupsByList( Map<String, Object> bodyParam) {
        ResponseMessage response = null;
        List<String> group = (List<String>) bodyParam.get("group");
        List<Unit> userList;
        if(group == null || group.isEmpty()){
            userList = unitService.findAll();
        } else {
            userList = unitService.findByIds(group);
        }
        response = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK.toString(), new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(),userList));
        return response;
    }
    public ResponseMessage getAllGroupInternal() {
        ResponseMessage response = null;
        List<Unit> userList = unitService.findAll();
        Map<String,Unit> groupMap = new HashMap<>();
        for (Unit group: userList){
            groupMap.put(group.getUuid(),group);
        }
        response = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK.toString(), new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(),groupMap));
        return response;
    }

    public ResponseMessage getManagerList(Map<String, String> headerParam, String requestPath,
            String method, String urlParam, Map<String, Object> bodyParam) {
        ResponseMessage response = null;
        AuthorizationResponseDTO dto = getAuthorFromToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập", null));
        } else {
            //Check RBAC quyền xử lý vi phạm
            ABACResponseDTO abacResponseDTO = authorizeABAC(bodyParam, "LIST", dto.getUuid(), requestPath);
            if (abacResponseDTO != null && abacResponseDTO.getStatus()) {
                if (abacResponseDTO.getAdmin() != null && abacResponseDTO.getAdmin()) {
                    Pageable pageable = null;
                    String search = "";
                    Page<Unit> unitList = null;
                    if (!StringUtil.isNullOrEmpty(urlParam)) {
                        Map<String, String> params = StringUtil.getUrlParamValues(urlParam);
                        Integer page = params.get("page") != null ? Integer.parseInt(params.get("page")) : null;
                        Integer size = params.get("size") != null ? Integer.parseInt(params.get("size")) : null;
                        search = params.get("search");
                        pageable = PageRequest.of(page, size);
                    } else {
                        pageable = PageRequest.of(0, 20);
                    }
                    unitList = unitService.findAll(pageable.getPageNumber(), pageable.getPageSize(), search,Sort.by("createdDate").descending());

                    if (unitList.isEmpty()) {
                        response = new ResponseMessage(new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(), null));
                    } else {
                        response = new ResponseMessage(new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(), unitList.getContent(), unitList.getTotalElements()));
                    }
                } else {
                    Unit unit = unitService.findByUuid(dto.getUnit().getUuid());
                    List<Unit> result = unit != null ? Collections.singletonList(unit) : null;
                    response = new ResponseMessage(new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(), result));
                }
            } else {
                response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(),
                        "Bạn không có quyền xem danh sách đội", null));
            }
        }
        return response;
    }

    public ResponseMessage getUnitByIdInternal(String pathParam) {
        ResponseMessage response = null;
        Unit unit = unitService.findByUuid(pathParam);
        if (unit == null) {
            response = new ResponseMessage(HttpStatus.NOT_FOUND.value(), Constant.VALIDATION_DATA_NOT_FOUND, new MessageContent(HttpStatus.NOT_FOUND.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
        } else {
            response = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), new MessageContent(unit));
        }
        return response;
    }

}
