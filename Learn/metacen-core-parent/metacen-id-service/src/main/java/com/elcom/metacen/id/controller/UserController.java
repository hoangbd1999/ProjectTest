package com.elcom.metacen.id.controller;

import com.elcom.metacen.id.checkPolicy.ResultCheckDto;
import com.elcom.metacen.id.constant.Constant;
import com.elcom.metacen.id.mail.MailContentDTO;
import com.elcom.metacen.id.model.Unit;
import com.elcom.metacen.id.model.User;
import com.elcom.metacen.id.model.dto.*;
import com.elcom.metacen.id.service.TokenService;
import com.elcom.metacen.id.service.UnitService;
import com.elcom.metacen.id.service.UserService;
import com.elcom.metacen.id.thread.IdThreadManager;
import com.elcom.metacen.id.utils.encrypt.AES;
import com.elcom.metacen.id.validation.UserValidation;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.utils.StringUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author anhdv
 */
@Controller
public class UserController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UnitService unitService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private IdThreadManager idThreadManager;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TokenService tokenService;

    public ResponseMessage getAllUser(Map<String, String> headerParam, String requestPath, String method, String urlParam) {
        ResponseMessage response = null;
        // Lấy thông tin user tu Param
        AuthorizationResponseDTO dto = getAuthorFromToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập", null));
        } else {
            //Check ABAC
            Map<String, Object> body = new HashMap<String, Object>();
            ABACResponseDTO abacStatus = authorizeABAC(body, "LIST", dto.getUuid(), requestPath);

            if (abacStatus != null && abacStatus.getStatus()) {
                    Map<String, String> params = StringUtil.getUrlParamValues(urlParam);

                    Integer currentPage = 1;
                    Integer rowsPerPage = 20;

                    if (StringUtil.isNullOrEmpty(params.get("currentPage"))) {
                        currentPage = 1;
                    } else {
                        currentPage = Integer.parseInt(params.get("currentPage"));
                    }
                    if (StringUtil.isNullOrEmpty(params.get("rowsPerPage"))) {
                        rowsPerPage = 20;
                    } else {
                        rowsPerPage = Integer.parseInt(params.get("rowsPerPage"));
                    }

                    String sort = params.get("sort");
                    String keyword = params.get("keyword");
                    String keywordDecode = "";
                    try {
                        keywordDecode = URLDecoder.decode(keyword);
                    } catch (Exception e) {
                        keywordDecode = "";
                    }

                    Integer status = null;
                    String strStatus = params.get("status");
                    if (StringUtil.isNumeric(strStatus)) {
                        status = Integer.parseInt(strStatus);
                    }
                    String startDate = params.get("startDate");
                    String endDate = params.get("endDate");
                    Integer signupType = null;
                    String strSignupType = params.get("signupType");
                    if (StringUtil.isNumeric(strSignupType)) {
                        signupType = Integer.parseInt(strSignupType);
                    }
                    Integer mobileVerify = null;
                    String strMobileVerify = params.get("mobileVerify");
                    if (StringUtil.isNumeric(strMobileVerify)) {
                        mobileVerify = Integer.parseInt(strMobileVerify);
                    }

                    if (currentPage == 0 || rowsPerPage == 0) {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
                    } else {
                        if (!StringUtil.isNullOrEmpty(sort) && !"uuid".equalsIgnoreCase(sort) && !"email".equalsIgnoreCase(sort)
                                && !"mobile".equalsIgnoreCase(sort) && !"userName".equalsIgnoreCase(sort) && !"fullName".equalsIgnoreCase(sort)
                                && !"createdAt".equalsIgnoreCase(sort)) {
                            response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Không có kiểu sort theo " + sort,
                                    new MessageContent(HttpStatus.BAD_REQUEST.value(), "Không có kiểu sort theo " + sort, null));
                        } else {
                            UserPagingDTO userDTO = userService.findAll(keywordDecode, status, currentPage, rowsPerPage,
                                    sort, signupType, mobileVerify, null, startDate, endDate);
                            if (userDTO == null || userDTO.getDataRows() == null || userDTO.getDataRows().isEmpty()) {
                                response = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK.toString(),
                                        new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(), null));
                            } else {
                                List<User> userList = userDTO.getDataRows();
                                if (userList != null && !userList.isEmpty()) {
                                    //Get list role with uuid user list
                                    List<String> uuidList = new ArrayList<>();
                                    userList.forEach((user) -> {
                                        uuidList.add(user.getUuid());
                                    });
                                }
                                response = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK.toString(),
                                        new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(), userList, userDTO.getTotalRows()));
                            }
                        }
                    }
            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Module kiểm tra quyền không phản hồi",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Module kiểm tra quyền không phản hồi", null));
            }
        }
        return response;
    }

    public ResponseMessage getAllUserInternal() {
        ResponseMessage response = null;
        List<User> userList = userService.findAll();
        Map<String,User> userMap = new HashMap<>();
        for (User user: userList){
            userMap.put(user.getUuid(),user);
        }
        response = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK.toString(),
                new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(),userMap));
        return response;
    }

    public ResponseMessage getDetailUser(String requestUrl, String method, String sId, Map<String, String> headerParam) {
        AuthorizationResponseDTO dto = getAuthorFromToken(headerParam);
        if (dto == null) {
            return new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            // Check ABAC
            Map<String, Object> body = new HashMap<String, Object>();
            ABACResponseDTO abacStatus = authorizeABAC(body, "DETAIL", dto.getUuid(), requestUrl);
            if (abacStatus != null && abacStatus.getStatus()) {
                User user = userService.findByUuid(sId);
                if (user == null) {
                    return new ResponseMessage(HttpStatus.NOT_FOUND.value(), Constant.VALIDATION_DATA_NOT_FOUND,
                            new MessageContent(HttpStatus.NOT_FOUND.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
                } else {
                    UserDetailDTO detailDTO = new UserDetailDTO(user);
                    return new ResponseMessage(new MessageContent(detailDTO));
                }
            } else {
                return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem chi tiết tài khoản",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem chi tiết tài khoản", null));
            }
        }
    }

    public ResponseMessage getGroup(String groupId) {
        ResponseMessage response = null;
        List<User> userList = userService.findByGroup(groupId);
        List<String> users = userList.stream().map((item) -> item.getUuid()).collect(Collectors.toList());
        response = new ResponseMessage(new MessageContent(users));
        return response;
    }

    public ResponseMessage getDetailUser(Map<String, String> headerParam) {
        ResponseMessage response;
        AuthorizationResponseDTO dto = getAuthorFromToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            //New
            UserDetailDTO detailDTO = new UserDetailDTO(dto);
            response = new ResponseMessage(new MessageContent(detailDTO));
        }
        return response;
    }

    public ResponseMessage createUser(String requestUrl, String method, Map<String, String> headerParam, Map<String, Object> bodyParam, String urlParam) {
        ResponseMessage response = null;
//        AuthorizationResponseDTO dto = GetUrlParam(urlParam);
        AuthorizationResponseDTO dto = getAuthorFromToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            LOGGER.info("Check Abac");
            Map<String, Object> subject = new HashMap<>();
            Map<String, Object> attributes = new HashMap<>();
            ResultCheckDto resultCheckDto = authorizeABAC(subject, attributes, dto.getUuid(), requestUrl, method);
            LOGGER.info("Nhận kết quả check abac");
            if (resultCheckDto != null) {
                if (resultCheckDto.getStatus()) {
                    String userName = (String) bodyParam.get("userName");
                    String email = (String) bodyParam.get("email");
                    String mobile = (String) bodyParam.get("mobile");
                    String fullName = (String) bodyParam.get("fullName");
                    String password = (String) bodyParam.get("password");
                    String matchingPassword = (String) bodyParam.get("matchingPassword");
                    Integer signupType = (Integer) bodyParam.get("signupType");
                    String address = (String) bodyParam.get("address");
                    String birthDay = (String) bodyParam.get("birthDay");
                    String fbId = (String) bodyParam.get("fbId");
                    String ggId = (String) bodyParam.get("ggId");
                    String avatar = (String) bodyParam.get("avatar");
                    String policeRank = (String) bodyParam.get("policeRank");
                    String position = (String) bodyParam.get("position");
                    String createDefaultRoleUser = (String) bodyParam.get("defaultRole");
                    String unitId = (String) bodyParam.get("unit");
                    Unit unit = unitService.findByUuid(unitId);
                    if (signupType == null) {
                        signupType = Constant.USER_SIGNUP_NORMAL;
                    }

                    User user = new User();
                    user.setUuid(UUID.randomUUID().toString());
                    user.setUserName(userName);
                    user.setEmail(email);
                    if (!StringUtil.isNullOrEmpty(mobile)) {
                        if (mobile.startsWith("+84")) {
                            mobile = mobile.replace("+84", "0");
                        } else if (mobile.startsWith("84") && mobile.length() == 11) {
                            mobile = mobile.replaceFirst("84", "0");
                        }
                    }
                    user.setMobile(mobile);
                    user.setFullName(fullName);
                    user.setPassword(password);
                    user.setMatchingPassword(matchingPassword);
                    user.setSignupType(signupType);
                    user.setFacebook(fbId);
                    user.setAddress(address);
                    user.setBirthDay(birthDay);
                    user.setGgId(ggId);
                    user.setFbId(fbId);
                    user.setIsDelete(0);
                    user.setStatus(1);
                    user.setIsActive(0);
                    user.setEmailVerify(0);
                    user.setMobileVerify(0);
                    user.setSetPassword(1);
                    user.setAvatar(avatar);
                    user.setPoliceRank(policeRank);
                    user.setPosition(position);
                    user.setUnit(unit);

                    String invalidData = new UserValidation().validateInsertUser(user);
                    if (invalidData != null) {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), invalidData, new MessageContent(HttpStatus.BAD_REQUEST.value(), invalidData, null));
                    } else {
                        User existUser = null;
                        //Check email exist
                        if (signupType == Constant.USER_SIGNUP_NORMAL && StringUtil.validateEmail(user.getEmail())) {
                            existUser = userService.findByEmail(user.getEmail());
                        }
                        if (existUser != null) {
                            invalidData = "Đã tồn tại tài khoản trên hệ thống ứng với email " + user.getEmail();
                            response = new ResponseMessage(HttpStatus.CONFLICT.value(), invalidData, new MessageContent(HttpStatus.CONFLICT.value(), invalidData, null));
                        } else {
                            //Check mobile exist
                            if (signupType == Constant.USER_SIGNUP_NORMAL && StringUtil.checkMobilePhoneNumberNew(user.getMobile())) {
                                existUser = userService.findByMobile(user.getMobile());
                            }
                            if (existUser != null) {
                                invalidData = "Đã tồn tại tài khoản trên hệ thống ứng với số điện thoại " + user.getMobile();
                                response = new ResponseMessage(HttpStatus.CONFLICT.value(), invalidData, new MessageContent(HttpStatus.CONFLICT.value(), invalidData, null));
                            } else {
                                //Check user_name exist
                                if (signupType == Constant.USER_SIGNUP_NORMAL && !StringUtil.isNullOrEmpty(user.getUserName())) {
                                    existUser = userService.findByUserName(user.getUserName());
                                }
                                if (existUser != null) {
                                    invalidData = "Đã tồn tại user trên hệ thống ứng với user_name " + user.getUserName();
                                    response = new ResponseMessage(HttpStatus.CONFLICT.value(), invalidData, new MessageContent(HttpStatus.CONFLICT.value(), invalidData, null));
                                } else {
                                    user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
                                    user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                                    try {
                                        userService.save(user);
                                        response = new ResponseMessage(HttpStatus.CREATED.value(), HttpStatus.CREATED.getReasonPhrase(), new MessageContent(user));
                                    } catch (Exception ex) {
                                        response = new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), null));
                                    }
                                }
                            }
                        }
                    }
                } else {
                    response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền tạo người dùng", new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền tạo người dùng", null));
                }
            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Module kiểm tra quyền không phản hồi", new MessageContent(HttpStatus.FORBIDDEN.value(), "Module kiểm tra quyền không phản hồi", null));
            }
        }
        return response;
    }

    public ResponseMessage deleteUser(String requestUrl, String method, String sId, Map<String, String> headerParam, String urlParam) {
        ResponseMessage response = null;
        AuthorizationResponseDTO dto = getAuthorFromToken(headerParam);
   //   dto = GetUrlParam(urlParam);
        if (dto == null) {
            response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập", null));
        } else {
            if (!StringUtil.isNumberic(sId) && !StringUtil.isUUID(sId)) {
                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            } else {
                User user = userService.findByUuid(sId);
                if (user == null) {
                    response = new ResponseMessage(HttpStatus.NOT_FOUND.value(), Constant.VALIDATION_DATA_NOT_FOUND,
                            new MessageContent(HttpStatus.NOT_FOUND.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
                } else {
                    LOGGER.info("check abac");
                    Map<String, Object> subject = new HashMap<>();
                    Map<String, Object> attributes = new HashMap<>();
                    List<String> groupUuids = new ArrayList<>();
                    attributes.put("groupUuid", groupUuids);
                    ResultCheckDto resultCheckDto = authorizeABAC(subject, attributes, dto.getUuid(), requestUrl, method);
                    LOGGER.info("Nhận kết quả check abac");
                    if (resultCheckDto != null) {
                        if (resultCheckDto.getStatus()) {
                            //K cho xóa user admin hoặc chính mình
                            if ("admin".equalsIgnoreCase(user.getUserName())) {
                                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không thể xóa user admin của hệ thống",
                                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không thể xóa user admin của hệ thống", null));
                            } else if (dto.getUuid().equals(user.getUuid())) {
                                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không thể xóa chính mình",
                                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không thể xóa chính mình", null));
                            } else {
                          //    userService.remove(user);
                                userService.deleteByUuid(user);
                                response = new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
                            }
                        } else {
                            response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xóa người dùng", new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xóa người dùng", null));
                        }
                    } else {
                        response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Module kiểm tra quyền không phản hồi", new MessageContent(HttpStatus.FORBIDDEN.value(), "Module kiểm tra quyền không phản hồi", null));
                    }
                }
            }

        }
        return response;
    }

    public ResponseMessage deleteUser(String requestUrl, String method, Map<String, Object> bodyParam, Map<String, String> headerParam, String urlParam) {
        ResponseMessage response = null;
        AuthorizationResponseDTO dto;
        dto = GetUrlParam(urlParam);
        if (dto == null) {
            return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.toString(), new MessageContent(HttpStatus.BAD_REQUEST.value(), " Lỗi trong quá trình lấy thông tin user", null));
        } else {
            List<String> uuidList = (List<String>) bodyParam.get("uuids");
            if (uuidList == null || uuidList.isEmpty()) {
                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "uuids không được bỏ trống hoặc không đúng định dạng array",
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), "uuids không được bỏ trống hoặc không đúng định dạng array", null));
            } else {
                try {
                    //Check bo xoa user admin va chinh minh
                    Set<String> groupUuids = new HashSet<>();
                    List<User> userList = userService.findByUuidIn(uuidList);
                    if (userList != null && !userList.isEmpty()) {
                        userList.stream().filter((user) -> ("admin".equalsIgnoreCase(user.getUserName()) || dto.getUuid().equals(user.getUuid()))).forEachOrdered((user) -> {
                            uuidList.remove(user.getUuid());
                        });
                    }
                    LOGGER.info("check abac");
                    Map<String, Object> subject = new HashMap<>();
                    Map<String, Object> attributes = new HashMap<>();
                    attributes.put("groupUuid", groupUuids);
                    ResultCheckDto resultCheckDto = authorizeABAC(subject, attributes, dto.getUuid(), requestUrl, method);
                    LOGGER.info("Nhận kết quả check abac");
                    if (resultCheckDto != null) {
                        if (resultCheckDto.getStatus()) {
                            userService.remove(uuidList);
                            response = new ResponseMessage(new MessageContent(null));
                        } else {
                            response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xóa người dùng", new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xóa người dùng", null));
                        }
                    } else {
                        response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Module kiểm tra quyền không phản hồi", new MessageContent(HttpStatus.FORBIDDEN.value(), "Module kiểm tra quyền không phản hồi", null));
                    }
                } catch (Exception ex) {
                    LOGGER.error(ex.toString());
                    response = new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR + " >>> " + ex, null));
                }
            }
        }
        return response;
    }

    public ResponseMessage updateUser(Map<String, Object> bodyParam, Map<String, String> headerParam, String pathParam, String method, String requestPath, String urlParam) {
        ResponseMessage response = null;

        AuthorizationResponseDTO dto = GetUrlParam(urlParam);
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập", null));
        } else {
//            dto = (AuthorizationResponseDTO) response.getData().getData();
            boolean hasRight = true;
            //Truong hop la path param update => Check RBAC quyen update
            if (!StringUtil.isNullOrEmpty(pathParam)) {
                LOGGER.info("Check abac");
                Map<String, Object> subject = new HashMap<>();
                Map<String, Object> attributes = new HashMap<>();
                attributes.put("groupUuid", bodyParam.get("groupsUuid"));
                ResultCheckDto resultCheckDto = authorizeABAC(subject, attributes, dto.getUuid(), requestPath, "PUT");
                LOGGER.info("Nhận kết quả check abac");
                if (resultCheckDto != null) {
                    hasRight = resultCheckDto.getStatus();
                } else {
                    hasRight = false;
                }
            }
            if (hasRight) {
                if (bodyParam == null || bodyParam.isEmpty()) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
                } else {
                    String uuid = !StringUtil.isNullOrEmpty(pathParam) ? pathParam : dto.getUuid();
                    String mobile = (String) bodyParam.get("mobile");
                    String email = (String) bodyParam.get("email");
                    String fullName = (String) bodyParam.get("fullName");
                    String skype = (String) bodyParam.get("skype");
                    String facebook = (String) bodyParam.get("facebook");
                    String avatar = (String) bodyParam.get("avatar");
                    String address = (String) bodyParam.get("address");
                    String birthDay = (String) bodyParam.get("birthDay");
                    Integer gender = (Integer) bodyParam.get("gender");
                    Integer status = (Integer) bodyParam.get("status");
                    String groupsUuid = (String) bodyParam.get("groupsUuid");
                    String policeRank = (String) bodyParam.get("policeRank");
                    String position = (String) bodyParam.get("position");
                    String password = (String) bodyParam.get("password");
                    String matchingPassword = (String) bodyParam.get("matchingPassword");
                    String unitId = (String) bodyParam.get("unit");
                    Unit unit = unitService.findByUuid(unitId);

                    User user = new User();
                    if (!StringUtil.isNullOrEmpty(pathParam)) {
                        user = userService.findByUuid(uuid);
                    }
                    if (user == null) {
                        String info = "Không tìm thấy thông tin người dùng ứng với uuid: " + uuid;
                        response = new ResponseMessage(HttpStatus.NOT_FOUND.value(), info, new MessageContent(HttpStatus.NOT_FOUND.value(), info, null));
                    } else {
                        user.setUuid(uuid);
                        if (!StringUtil.isNullOrEmpty(mobile)) {
                            if (mobile.startsWith("+84")) {
                                mobile = mobile.replace("+84", "0");
                            } else if (mobile.startsWith("84") && mobile.length() == 11) {
                                mobile = mobile.replaceFirst("84", "0");
                            }
                        }
                        user.setMobile(mobile);
                        user.setEmail(email);
                        user.setFullName(fullName);
                        user.setSkype(skype);
                        user.setFacebook(facebook);
                        user.setAvatar(avatar);
                        user.setAddress(address);
                        user.setBirthDay(birthDay);
                        user.setGender(gender);
                        if (status != null) {
                            user.setStatus(status);
                        }
                        user.setPoliceRank(policeRank);
                        user.setPosition(position);
                        user.setUnit(unit);

                        String invalidData = new UserValidation().validateUpdateUser(user, password, matchingPassword);
                        if (invalidData != null) {
                            response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), invalidData, new MessageContent(HttpStatus.BAD_REQUEST.value(), invalidData, null));
                        } else {
                            User userExist = null;
                            userExist = userService.findByEmail(email);
                            if (userExist != null && !userExist.getUuid().equalsIgnoreCase(uuid)) {
                                invalidData = "Đã tồn tại tài khoản trên hệ thống ứng với email " + user.getEmail();
                                response = new ResponseMessage(HttpStatus.CONFLICT.value(), invalidData, new MessageContent(HttpStatus.CONFLICT.value(), invalidData, null));
                            } else {
                                userExist = userService.findByMobile(mobile);
                                if (userExist != null && !userExist.getUuid().equalsIgnoreCase(uuid)) {
                                    invalidData = "Đã tồn tại tài khoản trên hệ thống ứng với số điện thoại " + user.getMobile();
                                    response = new ResponseMessage(HttpStatus.CONFLICT.value(), invalidData, new MessageContent(HttpStatus.CONFLICT.value(), invalidData, null));
                                } else {
                                    if (!StringUtil.isNullOrEmpty(password)) {
                                        user.setPassword(new BCryptPasswordEncoder().encode(password));
                                    }
                                    Timestamp now = new Timestamp(System.currentTimeMillis());
                                    user.setLastUpdate(now);
                                    //Check profile update
                                    if (!StringUtil.isNullOrEmpty(fullName) && !StringUtil.isNullOrEmpty(birthDay) && gender != null && !StringUtil.isNullOrEmpty(address)) {
                                        user.setProfileUpdate(now);
                                    }
                                    //Check avatar update
                                    if (!StringUtil.isNullOrEmpty(avatar)) {
                                        user.setAvatarUpdate(now);
                                    }
                                    try {
//                                boolean result = userService.update(user);
                                        userService.save(user);
//                                if (result && !StringUtil.isNullOrEmpty(password)) {
                                        user.setSetPassword(1);
                                        userService.changePassword(user);
                                        // Xác thực thông tin người dùng Request lên, nếu không xảy ra exception tức là thông tin hợp lệ
                                        Authentication authentication = null;
                                        try {
                                            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUserName(), password));
                                        } catch (AuthenticationException ex) {
                                            LOGGER.error("Error to set new authentication >>> " + ex);
                                        }
                                        // Set thông tin authentication mới vào Security Context
                                        SecurityContextHolder.getContext().setAuthentication(authentication);
//                                }
                                        response = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK.toString(), new MessageContent(user));
                                    } catch (Exception ex) {
                                        response = new ResponseMessage(HttpStatus.NOT_MODIFIED.value(), "Lỗi không cập nhật " + ex, new MessageContent(HttpStatus.NOT_MODIFIED.value(), "Lỗi không cập nhật " + ex, null));
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                String userId = !StringUtil.isNullOrEmpty(pathParam) ? pathParam : dto.getUuid();
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền cập nhật thông tin tài khoản", new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền cập nhật thông tin tài khoản", null));
            }
        }
        return response;
    }

    public ResponseMessage findByUuid(Map<String, String> headerParam, Map<String, Object> bodyParam) {
        ResponseMessage response = null;
        List<String> uuidList = (List<String>) bodyParam.get("uuids");
        if (uuidList == null || uuidList.isEmpty()) {
            response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "uuids không được bỏ trống hoặc không đúng định dạng array", new MessageContent(HttpStatus.BAD_REQUEST.value(), "uuids không được bỏ trống hoặc không đúng định dạng array", null));
        } else {
            try {
                List<User> userList = userService.findByUuidIn(uuidList);
                if (userList == null || userList.isEmpty()) {
                    response = new ResponseMessage(HttpStatus.NO_CONTENT.value(), "Không tìm thấy tài khoản ứng với list uuid", new MessageContent(HttpStatus.NO_CONTENT.value(), "Không tìm thấy tài khoản ứng với list uuid", null));
                } else {
                    response = new ResponseMessage(new MessageContent(userList));
                }
            } catch (Exception ex) {
                response = new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi không cập nhật " + ex, new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi không cập nhật " + ex, null));
                ex.printStackTrace();
            }
        }
        //}
        return response;
    }

    public ResponseMessage updateEmail(Map<String, Object> bodyParam, Map<String, String> headerParam) {
        ResponseMessage response = null;
        AuthorizationResponseDTO dto = getAuthorFromToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập",
                            null));
        } else {
            dto = (AuthorizationResponseDTO) response.getData().getData();
            if (bodyParam == null || bodyParam.isEmpty()) {
                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            } else {
                String password = (String) bodyParam.get("password");
                String newEmail = (String) bodyParam.get("newEmail");

                String invalidData = new UserValidation().validateChangeEmail(dto, password, newEmail);
                if (invalidData != null) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), invalidData, new MessageContent(HttpStatus.BAD_REQUEST.value(), invalidData, null));
                } else {
                    User user = userService.findByUuid(dto.getUuid());
                    boolean checkPassword = false;
                    //Nếu đăng ký qua gg hoặc fb và chưa thiết lập mật khẩu => K check password
                    if ((dto.getSignupType() == Constant.USER_SIGNUP_FACEBOOK || dto.getSignupType() == Constant.USER_SIGNUP_GOOGLE) && dto.getSetPassword() == 0) {
                        checkPassword = true;
                    } else {
                        //Kiểm tra mật khẩu
                        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                        String dbPassword = user.getPassword();
                        checkPassword = passwordEncoder.matches(password, dbPassword);
                    }
                    if (checkPassword) {
                        User existUser = userService.findByEmail(newEmail);
                        //Check email exist
                        if (existUser != null && (StringUtil.isNullOrEmpty(user.getEmail()) || !user.getEmail().equals(newEmail))) {
                            response = new ResponseMessage(HttpStatus.CONFLICT.value(), "Đã tồn tại tài khoản trên hệ thống ứng với email " + newEmail, new MessageContent(HttpStatus.CONFLICT.value(), "Đã tồn tại tài khoản trên hệ thống ứng với email " + newEmail, null));
                        } else {
                            user.setEmail(newEmail);
                            user.setEmailVerify(0);
                            try {
                                userService.changeEmail(user);
                                response = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK.toString(), new MessageContent(user));
                            } catch (Exception ex) {
                                response = new ResponseMessage(HttpStatus.NOT_MODIFIED.value(), "Lỗi không cập nhật " + ex, new MessageContent(HttpStatus.NOT_MODIFIED.value(), "Lỗi không cập nhật " + ex, null));
                            }
                        }
                    } else {
                        // Report error 
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Mật khẩu không đúng", new MessageContent(HttpStatus.BAD_REQUEST.value(), "Mật khẩu không đúng", null));
                    }
                }
            }
        }
        return response;
    }

    public ResponseMessage updateMobile(Map<String, Object> bodyParam, Map<String, String> headerParam) {
        ResponseMessage response = null;
        AuthorizationResponseDTO dto = getAuthorFromToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập",
                            null));
        } else {
            dto = (AuthorizationResponseDTO) response.getData().getData();
            if (bodyParam == null || bodyParam.isEmpty()) {
                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            } else {
                String password = (String) bodyParam.get("password");
                String newMobile = (String) bodyParam.get("newMobile");

                String invalidData = new UserValidation().validateChangeMobile(dto, password, newMobile);
                if (invalidData != null) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), invalidData, new MessageContent(HttpStatus.BAD_REQUEST.value(), invalidData, null));
                } else {
                    User user = userService.findByUuid(dto.getUuid());
                    boolean checkPassword = false;
                    //Nếu đăng ký qua gg hoặc fb và chưa thiết lập mật khẩu => K check password
                    if ((dto.getSignupType() == Constant.USER_SIGNUP_FACEBOOK || dto.getSignupType() == Constant.USER_SIGNUP_GOOGLE) && dto.getSetPassword() == 0) {
                        checkPassword = true;
                    } else {
                        //Kiểm tra mật khẩu
                        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                        String dbPassword = user.getPassword();
                        checkPassword = passwordEncoder.matches(password, dbPassword);
                    }
                    if (checkPassword) {
                        if (newMobile.startsWith("+84")) {
                            newMobile = newMobile.replace("+84", "0");
                        } else if (newMobile.startsWith("84") && newMobile.length() == 11) {
                            newMobile = newMobile.replaceFirst("84", "0");
                        }
                        User existUser = userService.findByMobile(newMobile);
                        //Check mobile exist
                        if (existUser != null && (StringUtil.isNullOrEmpty(user.getMobile()) || !user.getMobile().equals(newMobile))) {
                            response = new ResponseMessage(HttpStatus.CONFLICT.value(), "Đã tồn tại tài khoản trên hệ thống ứng với số điện thoại " + newMobile, new MessageContent(HttpStatus.CONFLICT.value(), "Đã tồn tại tài khoản trên hệ thống ứng với số điện thoại " + newMobile, null));
                        } else {
                            if (existUser != null && existUser.getUuid().equals(user.getUuid())) {
                                user.setMobile(existUser.getMobile());
                            }
                            LOGGER.info("=========> new Mobile: {}, current user mobile: {}", newMobile, user.getMobile());
                            if (!newMobile.equals(user.getMobile()) || user.getMobileVerify() == 0) {

                                //Check Redis contains OTP of user
                                //OtpExpiredDTO otpExpired = getOtpExpiredTime(user.getUuid());
                                OtpExpiredDTO otpExpired = null;
                                //Nếu chưa gửi lần nào hoặc đã hết 15ph lại cho đổi hoặc trong 15ph đó chưa gửi quá 5 lần
                                if (otpExpired == null || otpExpired.getOtpExpiredTime() < System.currentTimeMillis() || otpExpired.getCountSms() < Constant.MAX_SMS_PER_EXPIRED) {
                                    user.setOtpMobile(newMobile);
                                    user.setMobileVerify(0);
                                    try {
                                        userService.changeOtpMobile(user);
                                        if (otpExpired == null || otpExpired.getOtpExpiredTime() < System.currentTimeMillis() || (user.getOtpTime() != null && user.getOtpTime().getTime() < System.currentTimeMillis())) {
                                            user.setOtpTime(new Timestamp(System.currentTimeMillis() + Constant.OTP_TIME_EXIPRED));
                                        }
                                        //Convert only here for UserOtpDTO
                                        UserOtpDTO userOtp = new UserOtpDTO(user);
                                        //
                                        response = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK.toString(), new MessageContent(userOtp));
                                    } catch (Exception ex) {
                                        response = new ResponseMessage(HttpStatus.NOT_MODIFIED.value(), "Lỗi cập nhật số điện thoại khi đăng nhập Facebook/Google >>> " + ex, new MessageContent(HttpStatus.NOT_MODIFIED.value(), "Lỗi cập nhật số điện thoại khi đăng nhập Facebook/Google >>> " + ex, null));
                                    }
                                    if (response.getStatus() == HttpStatus.OK.value()) {
                                        //Tao OTP & set OTP time
                                        String otp = RandomStringUtils.randomNumeric(6);
                                        if (userService.createOTP(user, otp)) {
                                            String userMobile = newMobile;
                                        }
                                    }
                                } else {
                                    String reason = "Chỉ được cập nhật số điện thoại tối đa " + Constant.MAX_SMS_PER_EXPIRED + " lần trong vòng " + (Constant.OTP_TIME_EXIPRED / (60 * 1000) + " phút ");
                                    response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), reason, new MessageContent(HttpStatus.FORBIDDEN.value(), reason, null));
                                }
                            } else {
                                response = new ResponseMessage(HttpStatus.CONFLICT.value(), "Số điện thoại bạn vừa nhập trùng với số hiện tại", new MessageContent(HttpStatus.CONFLICT.value(), "Số điện thoại bạn vừa nhập trùng với số hiện tại", null));
                            }
                        }
                    } else {
                        // Report error 
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Mật khẩu không đúng", new MessageContent(HttpStatus.BAD_REQUEST.value(), "Mật khẩu không đúng", null));
                    }
                }
            }
        }
        return response;
    }

    public ResponseMessage updatePassword(Map<String, Object> bodyParam, Map<String, String> headerParam) {
        ResponseMessage response = null;
        AuthorizationResponseDTO dto = getAuthorFromToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập",
                            null));
        } else {
//            dto = (AuthorizationResponseDTO) response.getData().getData();
            if (bodyParam == null || bodyParam.isEmpty()) {
                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            } else {
                String curentPassword = (String) bodyParam.get("curentPassword");
                String newPassword = (String) bodyParam.get("newPassword");
                String rePassword = (String) bodyParam.get("rePassword");

                String invalidData = new UserValidation().validateUpdatePassword(dto, curentPassword, newPassword, rePassword);
                if (invalidData != null) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), invalidData, new MessageContent(HttpStatus.BAD_REQUEST.value(), invalidData, null));
                } else {
                    User user = userService.findByUuid(dto.getUuid());
                    boolean checkPassword = false;
                    //Nếu đăng ký qua gg hoặc fb và chưa thiết lập mật khẩu => K check password
                    if ((dto.getSignupType() == Constant.USER_SIGNUP_FACEBOOK || dto.getSignupType() == Constant.USER_SIGNUP_GOOGLE) && dto.getSetPassword() == 0) {
                        checkPassword = true;
                    } else {
                        //Check currentPassword
                        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                        String dbPassword = user.getPassword();
                        checkPassword = passwordEncoder.matches(curentPassword, dbPassword);
                    }
                    if (checkPassword) {
                        // Encode new password and store it
                        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
                        user.setSetPassword(1);
                        try {
                            if (userService.changePassword(user)) {
                                // Xác thực thông tin người dùng Request lên, nếu không xảy ra exception tức là thông tin hợp lệ
                                Authentication authentication = null;
                                try {
                                    authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), newPassword));
                                } catch (AuthenticationException ex) {
                                    LOGGER.error("Error to set new authentication >>> " + ex);
                                }
                                // Set thông tin authentication mới vào Security Context
                                SecurityContextHolder.getContext().setAuthentication(authentication);

                                String accessJwt = tokenService.setAccessTokenUpdate(dto.getUuid());

                                String refreshJwt = tokenService.setRefreshTokenUpdate(dto.getUuid());
                                AuthorizationResponseDTO responseDTO = new AuthorizationResponseDTO(accessJwt, refreshJwt);
//                                response = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK.toString(), new MessageContent(bodyParam));
                                response = new ResponseMessage(new MessageContent(responseDTO));
                            } else {
                                response = new ResponseMessage(HttpStatus.NOT_MODIFIED.value(), HttpStatus.NOT_MODIFIED.getReasonPhrase(), new MessageContent(HttpStatus.NOT_MODIFIED.value(), HttpStatus.NOT_MODIFIED.getReasonPhrase(), null));
                            }
                        } catch (Exception ex) {
                            response = new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), null));
                        }
                    } else {
                        // Report error 
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Mật khẩu hiện tại không đúng", new MessageContent(HttpStatus.BAD_REQUEST.value(), "Mật khẩu hiện tại không đúng", null));
                    }
                }
            }
        }
        return response;
    }

    public ResponseMessage checkUserExist(String urlParam) {
        ResponseMessage response = null;
        if (StringUtil.isNullOrEmpty(urlParam)) {
            response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
        } else {
            Map<String, String> params = StringUtil.getUrlParamValues(urlParam);
            String type = params.get("type");
            String email = params.get("email");
            String mobile = params.get("mobile");

            if ("email".equalsIgnoreCase(type)) {
                if (!StringUtil.validateEmail(email)) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Email không được trống hoặc không đúng định dạng", new MessageContent(HttpStatus.BAD_REQUEST.value(), "Email không được trống hoặc không đúng định dạng", null));
                } else {
                    User user = userService.findByEmail(email);
                    if (user == null) {
                        response = new ResponseMessage(new MessageContent("Không tìm thấy tài khoản ứng với email " + email));
                    } else {
                        response = new ResponseMessage(HttpStatus.CONFLICT.value(), "Email đã được đăng ký bởi tài khoản khác", new MessageContent(HttpStatus.CONFLICT.value(), "Email đã được đăng ký bởi tài khoản khác", null));
                    }
                }
            } else if ("mobile".equalsIgnoreCase(type)) {
                if (!StringUtil.checkMobilePhoneNumberNew(mobile)) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Số điện thoại không được trống hoặc không đúng định dạng", new MessageContent(HttpStatus.BAD_REQUEST.value(), "Số điện thoại không được trống hoặc không đúng định dạng", null));
                } else {
                    User user = userService.findByMobile(mobile);
                    if (user == null) {
                        response = new ResponseMessage(new MessageContent("Không tìm thấy user ứng với mobile " + mobile));
                    } else {
                        response = new ResponseMessage(HttpStatus.CONFLICT.value(), "Số điện thoại này đã được xác thực bởi một tài khoản khác. Mỗi số điện thoại chỉ có thể xác thực cho một tài khoản", new MessageContent(HttpStatus.CONFLICT.value(), "Số điện thoại này đã được xác thực bởi một tài khoản khác. Mỗi số điện thoại chỉ có thể xác thực cho một tài khoản", null));
                    }
                }
            } else {
                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Type chỉ nhận giá trị bằng email hoặc mobile", new MessageContent(HttpStatus.BAD_REQUEST.value(), "Type chỉ nhận giá trị bằng email hoặc mobile", null));
            }
        }
        return response;
    }

    public ResponseMessage forgotPassword(String urlParam) {
        ResponseMessage response = null;
        if (StringUtil.isNullOrEmpty(urlParam)) {
            response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
        } else {
            Map<String, String> params = StringUtil.getUrlParamValues(urlParam);
            String email = params.get("email");
            String mobile = params.get("mobile");
            String type = params.get("type");
            String platform = params.get("platform");
            if (!StringUtil.isNullOrEmpty(mobile)) {
                if (mobile.startsWith("+84")) {
                    mobile = mobile.replace("+84", "0");
                } else if (mobile.startsWith("84") && mobile.length() == 11) {
                    mobile = mobile.replaceFirst("84", "0");
                }
            }

            if (StringUtil.isNullOrEmpty(platform) || "web".equalsIgnoreCase(platform) || "wap".equalsIgnoreCase(platform)) {
                response = processWebForgotPassword(type, email, mobile);
            } else if ("app".equalsIgnoreCase(platform)) {
                response = processAppForgotPassword(type, email, mobile);
            } else {
                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Platform chỉ nhận một trong các giá trị web/wap/app", new MessageContent(HttpStatus.BAD_REQUEST.value(), "Platform chỉ nhận một trong các giá trị web/wap/app", null));
            }
        }
        return response;
    }

    private ResponseMessage processWebForgotPassword(String type, String email, String mobile) {
//        ResponseMessage response = null;
//        if (!StringUtil.validateEmail(email)) {
//            response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Email không được trống hoặc không đúng định dạng", new MessageContent(HttpStatus.BAD_REQUEST.value(), "Email không được trống hoặc không đúng định dạng", null));
//        } else {
//            User user = userService.findByEmail(email);
//            if (user == null) {
//                response = new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Không tìm thấy tài khoản ứng với email " + email, new MessageContent(HttpStatus.NOT_FOUND.value(), "Không tìm thấy tài khoản ứng với email " + email, null));
//            } else {
//                //Code mới trả về link
//                String genCheckInfo = user.getUuid() + "&" + (System.currentTimeMillis() + ApplicationConfig.FORGOTPASS_EXPIRED_TIME * 60 * 1000);
//                String token = AES.encryptAESbase(genCheckInfo, Constant.AES_KEY);
//                String link = ApplicationConfig.FRONTEND_FORGOTPASS_URL + "?token=" + token;
//                MailContentDTO item = new MailContentDTO();
//                item.setType("one");
//                item.setFromName("CoLearn.vn");
//                item.setEmailTitle(String.format(Constant.MAIL_FORGOT_PW_TITLE, user.getEmail()));
//                item.setEmailContent(String.format(Constant.MAIL_FORGOT_PW_CONTENT_LINK, user.getFullName(), link, link, ApplicationConfig.FORGOTPASS_EXPIRED_TIME));
//                item.setEmailTo(email);
//                if (idThreadManager.sendEmail(item)) {
//                    response = new ResponseMessage(new MessageContent("Gửi email quên mật khẩu thành công"));
//                } else {
//                    response = new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), null));
//                }
//            }
//        }
        return null;
    }

    private ResponseMessage processAppForgotPassword(String type, String email, String mobile) {
//        ResponseMessage response = null;
//        if (StringUtil.isNullOrEmpty(type) || "mobile".equalsIgnoreCase(type)) {
//            if (!StringUtil.checkMobilePhoneNumberNew(mobile)) {
//                String invalid = "Email hoặc số điện thoại không đúng định dạng";
//                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), invalid, new MessageContent(HttpStatus.BAD_REQUEST.value(), invalid, null));
//            } else {
//                User user = userService.findByMobile(mobile);
//                if (user == null) {
//                    response = new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Không tìm thấy tài khoản ứng với số điện thoại " + mobile, new MessageContent(HttpStatus.NOT_FOUND.value(), "Không tìm thấy tài khoản ứng với số điện thoại " + mobile, null));
//                } else {
//                    //Check Redis contains OTP forgot password of user
//                    OtpExpiredDTO otpExpired = null;//getOtpForgotPasswordExpiredTime(user.getUuid());
//                    //Nếu chưa gửi lần nào hoặc đã hết 15ph lại cho đổi hoặc trong 15ph đó chưa gửi quá 5 lần
//                    if (otpExpired == null || otpExpired.getOtpExpiredTime() < System.currentTimeMillis() || otpExpired.getCountSms() < Constant.MAX_SMS_PER_EXPIRED) {
//                        //Tao OTP & set OTP Password time
//                        String otp = RandomStringUtils.randomNumeric(6);
//                        if (otpExpired == null || otpExpired.getOtpExpiredTime() < System.currentTimeMillis() || (user.getOtpPasswordTime() != null && user.getOtpPasswordTime().getTime() < System.currentTimeMillis())) {
//                            user.setOtpPasswordTime(new Timestamp(System.currentTimeMillis() + Constant.OTP_TIME_EXIPRED));
//                        }
//                        if (userService.createOTPPassword(user, otp)) {
//                            //Convert only here for UserOtpDTO
//                            UserOtpDTO userOtp = new UserOtpDTO(user);
//                            //
//                            response = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK.toString(), new MessageContent(userOtp));
//                        } else {
//                            response = new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi gửi OTP", new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi gửi OTP", null));
//                        }
//                    } else {
//                        String reason = "Chỉ được gửi OTP quên mật khẩu tối đa " + Constant.MAX_SMS_PER_EXPIRED + " lần trong vòng " + (Constant.OTP_TIME_EXIPRED / (60 * 1000) + " phút ");
//                        response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), reason, new MessageContent(HttpStatus.FORBIDDEN.value(), reason, null));
//                    }
//                }
//            }
//        } else if ("email".equalsIgnoreCase(type)) {
//            if (!StringUtil.validateEmail(email)) {
//                String invalid = "Email hoặc số điện thoại không đúng định dạng";
//                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), invalid, new MessageContent(HttpStatus.BAD_REQUEST.value(), invalid, null));
//            } else {
//                User user = userService.findByEmail(email);
//                if (user == null) {
//                    response = new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Không tìm thấy tài khoản ứng với email " + email, new MessageContent(HttpStatus.NOT_FOUND.value(), "Không tìm thấy tài khoản ứng với email " + email, null));
//                } else {
//                    //Check Redis contains OTP forgot password of user
//                    OtpExpiredDTO otpExpired = null;//getOtpForgotPasswordExpiredTime(user.getUuid());
//                    //Nếu chưa gửi lần nào hoặc đã hết 15ph lại cho đổi hoặc trong 15ph đó chưa gửi quá 5 lần
//                    if (otpExpired == null || otpExpired.getOtpExpiredTime() < System.currentTimeMillis() || otpExpired.getCountSms() < Constant.MAX_SMS_PER_EXPIRED) {
//                        //Tao OTP & set OTP Password time
//                        String otp = RandomStringUtils.randomNumeric(6);
//                        if (otpExpired == null || otpExpired.getOtpExpiredTime() < System.currentTimeMillis() || (user.getOtpPasswordTime() != null && user.getOtpPasswordTime().getTime() < System.currentTimeMillis())) {
//                            user.setOtpPasswordTime(new Timestamp(System.currentTimeMillis() + Constant.OTP_TIME_EXIPRED));
//                        }
//                        if (userService.createOTPPassword(user, otp)) {
//                            //Convert only here for UserOtpDTO
//                            UserOtpDTO userOtp = new UserOtpDTO(user);
//                            //Return response
//                            response = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK.toString(), new MessageContent(userOtp));
//
//                            //Send OTP via Email
//                            MailContentDTO item = new MailContentDTO();
//                            item.setType("one");
//                            item.setFromName("CoLearn.vn");
//                            item.setEmailTitle(String.format(Constant.MAIL_FORGOT_PW_TITLE, user.getEmail()));
//                            item.setEmailContent(String.format(Constant.MAIL_FORGOT_PW_CONTENT_OTP, user.getFullName(), otp, ApplicationConfig.FORGOTPASS_EXPIRED_TIME));
//                            item.setEmailTo(email);
//                            if (idThreadManager.sendEmail(item)) {
//                                LOGGER.info("Send OTP {} to email {} => OK", otp, user.getEmail());
//                                OtpExpiredDTO tmpOtpExpired = otpExpired;
//                                //Push 2 redis otpExpired
//                                if (tmpOtpExpired == null || tmpOtpExpired.getOtpExpiredTime() < System.currentTimeMillis()) {
//                                    tmpOtpExpired = new OtpExpiredDTO();
//                                    tmpOtpExpired.setUuid(user.getUuid());
//                                    tmpOtpExpired.setOtpExpiredTime(user.getOtpPasswordTime().getTime());
//                                    tmpOtpExpired.setCountSms(1);
//                                } else {
//                                    tmpOtpExpired.setCountSms(tmpOtpExpired.getCountSms() + 1);
//                                }
//                                //pushOtpForgotPasswordExpiredTime(user.getUuid(), tmpOtpExpired);
//                            } else {
//                                LOGGER.info("Send OTP {} to email {} => Fail", otp, user.getEmail());
//                            }
//                        } else {
//                            response = new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi gửi OTP", new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi gửi OTP", null));
//                        }
//                    } else {
//                        String reason = "Chỉ được gửi OTP quên mật khẩu tối đa " + Constant.MAX_SMS_PER_EXPIRED + " lần trong vòng " + (Constant.OTP_TIME_EXIPRED / (60 * 1000) + " phút ");
//                        response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), reason, new MessageContent(HttpStatus.FORBIDDEN.value(), reason, null));
//                    }
//                }
//            }
//        } else {
//            response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "type chỉ nhận một trong các giá trị email/mobile", new MessageContent(HttpStatus.BAD_REQUEST.value(), "type chỉ nhận một trong các giá trị email/mobile", null));
//        }
        // return response;
        return null;
    }

    public ResponseMessage updateStatus(Map<String, Object> bodyParam, Map<String, String> headerParam) {
        ResponseMessage response = null;
        AuthorizationResponseDTO dto = getAuthorFromToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập",
                            null));
        } else {
            dto = (AuthorizationResponseDTO) response.getData().getData();
            String uuid = (String) bodyParam.get("uuid");
            Integer status = (Integer) bodyParam.get("status");
            User user = new User();
            user.setUuid(uuid);
            user.setStatus(status);
            String invalidData = new UserValidation().validateUpdateStatus(user);
            if (invalidData != null) {
                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), invalidData,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), invalidData, invalidData));
            } else {
                User existUser = userService.findByUuid(uuid);
                if (existUser == null) {
                    response = new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Không tìm thấy tài khoản ứng với uuid " + uuid,
                            new MessageContent(HttpStatus.NOT_FOUND.value(), "Không tìm thấy tài khoản ứng với uuid " + uuid, null));
                } else {
                    Map<String, Object> subject = new HashMap<>();
                    Map<String, Object> attributes = new HashMap<>();
                    ResultCheckDto resultCheckDto = authorizeABAC(subject, attributes, dto.getUuid(), "/v1.0/user/status", "PUT");
                    LOGGER.info("Nhận kết quả check abac");
                    if (resultCheckDto != null) {
                        if (resultCheckDto.getStatus()) {
                            if (bodyParam == null || bodyParam.isEmpty()) {
                                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
                            } else {
                                user.setLastUpdate(new Timestamp(System.currentTimeMillis()));
                                try {
                                    userService.changeStatus(user);
                                    response = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK.toString(), new MessageContent(bodyParam));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    response = new ResponseMessage(HttpStatus.NOT_MODIFIED.value(), "Lỗi không cập nhật " + ex,
                                            new MessageContent(HttpStatus.NOT_MODIFIED.value(), "Lỗi không cập nhật " + ex, null));
                                }
                            }
                        } else {
                            response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền cập nhật trạng thái tài khoản",
                                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền cập nhật trạng thái tài khoản", null));
                        }
                    } else {
                        response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Module kiểm tra quyền không phản hồi",
                                new MessageContent(HttpStatus.FORBIDDEN.value(), "Module kiểm tra quyền không phản hồi", null));
                    }
                }
            }
        }
        return response;
    }

    public ResponseMessage updateActive(String requestPath, Map<String, String> headerParam, String pathParam) {
        ResponseMessage response = null;
        AuthorizationResponseDTO dto = getAuthorFromToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập",
                            null));
        }
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "PUT", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            User user = userService.findByUuid(pathParam);
            if (user == null) {
                response = new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Tài khoản không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Tài khoản không tồn tại", null));
            }
            if(user.getIsActive() == 0) {
                this.userService.updateActive(pathParam);
                response = new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Kích hoạt tài khoản thành công", null));
            } else if (user.getIsActive() == 1){
                if ("admin".equalsIgnoreCase(user.getUserName())) {
                    return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không thể vô hiệu hóa user admin của hệ thống",
                            new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không thể vô hiệu hóa user admin của hệ thống", null));
                } else if (dto.getUuid().equals(user.getUuid())) {
                    return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không thể vô hiệu hóa chính mình",
                            new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không thể vô hiệu hóa chính mình", null));
                }
                this.userService.updateInActive(pathParam);
                response = new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Vô hiệu hóa tài khoản thành công", null));
            }
        } else {
            response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
        return response;
    }

    public ResponseMessage findAvatarByUuid(Map<String, Object> bodyParam) {
        ResponseMessage response = null;
        List<String> uuidList = (List<String>) bodyParam.get("uuids");
        if (uuidList == null || uuidList.isEmpty()) {
            response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "uuids không được bỏ trống hoặc không đúng định dạng array",
                    new MessageContent(HttpStatus.BAD_REQUEST.value(), "uuids không được bỏ trống hoặc không đúng định dạng array", null));
        } else {
            try {
                List<User> userList = userService.findByUuidIn(uuidList);
                if (userList == null || userList.isEmpty()) {
                    LOGGER.info("userList null");
                    response = new ResponseMessage(HttpStatus.NO_CONTENT.value(), "Không tìm thấy avatar tài khoản ứng với list uuid",
                            new MessageContent(HttpStatus.NO_CONTENT.value(), "Không tìm thấy avatar tài khoản ứng với list uuid", null));
                } else {
                    LOGGER.info("userList size: " + userList.size());
                    List<UserAvatarDTO> userAvatarList = UserAvatarDTO.getAvatarList(userList);
                    response = new ResponseMessage(new MessageContent(userAvatarList));
                }
            } catch (Exception ex) {
                response = new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi không cập nhật " + ex,
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi không cập nhật " + ex, ex.toString()));
                ex.printStackTrace();
            }
        }
        return response;
    }

    public ResponseMessage updateSocialMobile(Map<String, Object> bodyParam, Map<String, String> headerParam) {
        ResponseMessage response = null;
        AuthorizationResponseDTO dto = getAuthorFromToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập",
                            null));
        } else {
            dto = (AuthorizationResponseDTO) response.getData().getData();
            if (bodyParam == null || bodyParam.isEmpty()) {
                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            } else {
                String mobile = (String) bodyParam.get("mobile");
                if (!StringUtil.isNullOrEmpty(mobile)) {
                    if (mobile.startsWith("+84")) {
                        mobile = mobile.replace("+84", "0");
                    } else if (mobile.startsWith("84") && mobile.length() == 11) {
                        mobile = mobile.replaceFirst("84", "0");
                    }
                }

                String invalidData = new UserValidation().validateUpdateSocialMobile(dto, mobile);
                if (invalidData != null) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), invalidData,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), invalidData, null));
                } else {
                    User user = userService.findByUuid(dto.getUuid());
                    if (mobile.startsWith("+84")) {
                        mobile = mobile.replace("+84", "0");
                    } else if (mobile.startsWith("84") && mobile.length() == 11) {
                        mobile = mobile.replaceFirst("84", "0");
                    }
                    User existUser = userService.findByMobile(mobile);
                    //Check mobile exist
                    if (existUser != null && (StringUtil.isNullOrEmpty(user.getMobile()) || !user.getMobile().equals(mobile))) {
                        response = new ResponseMessage(HttpStatus.CONFLICT.value(), "Đã tồn tại tài khoản trên hệ thống ứng với số điện thoại " + mobile,
                                new MessageContent(HttpStatus.CONFLICT.value(), "Đã tồn tại tài khoản trên hệ thống ứng với số điện thoại " + mobile, null));
                    } else {
                        //Check Redis contains OTP of user
                        OtpExpiredDTO otpExpired = null;//getOtpExpiredTime(user.getUuid());
                        //Nếu chưa gửi lần nào hoặc đã hết 15ph lại cho đổi hoặc trong 15ph đó chưa gửi quá 3 lần
                        if (otpExpired == null || otpExpired.getOtpExpiredTime() < System.currentTimeMillis() || otpExpired.getCountSms() < Constant.MAX_SMS_PER_EXPIRED) {
                            user.setOtpMobile(mobile);
                            user.setMobileVerify(0);
                            try {
                                userService.changeOtpMobile(user);
                                if (otpExpired == null || otpExpired.getOtpExpiredTime() < System.currentTimeMillis() || (user.getOtpTime() != null && user.getOtpTime().getTime() < System.currentTimeMillis())) {
                                    user.setOtpTime(new Timestamp(System.currentTimeMillis() + Constant.OTP_TIME_EXIPRED));
                                }
                                //Convert only here for UserOtpDTO
                                UserOtpDTO userOtp = new UserOtpDTO(user);
                                //
                                response = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK.toString(), new MessageContent(userOtp));
                            } catch (Exception ex) {
                                response = new ResponseMessage(HttpStatus.NOT_MODIFIED.value(), "Lỗi cập nhật số điện thoại khi đăng nhập Facebook/Google >>> " + ex, new MessageContent(HttpStatus.NOT_MODIFIED.value(), "Lỗi cập nhật số điện thoại khi đăng nhập Facebook/Google >>> " + ex, null));
                            }
                            if (response.getStatus() == HttpStatus.OK.value()) {
                                //Tao OTP & set OTP time
                                String otp = RandomStringUtils.randomNumeric(6);
                                if (userService.createOTP(user, otp)) {
                                    String userMobile = mobile;
                                }
                            }
                        } else {
                            String reason = "Chỉ được cập nhật số điện thoại tối đa " + Constant.MAX_SMS_PER_EXPIRED + " lần trong vòng " + (Constant.OTP_TIME_EXIPRED / (60 * 1000) + " phút ");
                            response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), reason, new MessageContent(HttpStatus.FORBIDDEN.value(), reason, null));
                        }
                    }
                }
            }
        }
        return response;
    }

    public ResponseMessage updateUserInternal(Map<String, Object> bodyParam, Map<String, String> headerParam, String pathParam) {
        ResponseMessage response = null;

        if (bodyParam == null || bodyParam.isEmpty()) {
            response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
        } else {
            String uuid = pathParam;
            String mobile = (String) bodyParam.get("mobile");
            String fullName = (String) bodyParam.get("fullName");
            String skype = (String) bodyParam.get("skype");
            String facebook = (String) bodyParam.get("facebook");
            String avatar = (String) bodyParam.get("avatar");
            String address = (String) bodyParam.get("address");
            String birthDay = (String) bodyParam.get("birthDay");
            Integer gender = (Integer) bodyParam.get("gender");

            User user = new User();
            user.setUuid(uuid);
            if (!StringUtil.isNullOrEmpty(mobile)) {
                if (mobile.startsWith("+84")) {
                    mobile = mobile.replace("+84", "0");
                } else if (mobile.startsWith("84") && mobile.length() == 11) {
                    mobile = mobile.replaceFirst("84", "0");
                }
            }
            user.setMobile(mobile);
            user.setFullName(fullName);
            user.setSkype(skype);
            user.setFacebook(facebook);
            user.setAvatar(avatar);
            user.setAddress(address);
            user.setBirthDay(birthDay);
            user.setGender(gender);

            String invalidData = new UserValidation().validateUpdateUser(user, null, null);
            if (invalidData != null) {
                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), invalidData,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), invalidData, null));
            } else {
                Timestamp now = new Timestamp(System.currentTimeMillis());
                user.setLastUpdate(now);
                //Check profile update
                if (!StringUtil.isNullOrEmpty(fullName) && !StringUtil.isNullOrEmpty(birthDay) && gender != null && !StringUtil.isNullOrEmpty(address)) {
                    user.setProfileUpdate(now);
                }
                //Check avatar update
                if (!StringUtil.isNullOrEmpty(avatar)) {
                    user.setAvatarUpdate(now);
                }
                try {
                    userService.update(user);
                    response = new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK.toString(), new MessageContent(user));
                } catch (Exception ex) {
                    response = new ResponseMessage(HttpStatus.NOT_MODIFIED.value(), "Lỗi không cập nhật " + ex,
                            new MessageContent(HttpStatus.NOT_MODIFIED.value(), "Lỗi không cập nhật " + ex, null));
                }
            }
        }
        return response;
    }

    public ResponseMessage checkToken(Map<String, Object> bodyParam, Map<String, String> headerParam) {
        ResponseMessage response = null;
        if (bodyParam == null || bodyParam.isEmpty()) {
            response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                    new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
        } else {
            String token = (String) bodyParam.get("token");
            if (StringUtil.isNullOrEmpty(token)) {
                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            } else {
                String decryptResult = AES.decryptAESbase(token, Constant.AES_KEY);
                if (StringUtil.isNullOrEmpty(decryptResult)) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Token không đúng",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "Token không đúng", null));
                } else {
                    String[] decryptInfo = decryptResult.split("&");
                    if (decryptInfo == null || decryptInfo.length < 2) {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Token không đúng",
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), "Token không đúng", null));
                    } else {
                        String userUuid = decryptInfo[0];
                        User user = userService.findByUuid(userUuid);

                        if (user == null || (user.getIsDelete() != null && (user.getStatus() == -1 || user.getIsDelete() == 1))) {
                            response = new ResponseMessage(HttpStatus.NOT_FOUND.value(), Constant.VALIDATION_DATA_NOT_FOUND,
                                    new MessageContent(HttpStatus.NOT_FOUND.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
                        } else {
                            long now = System.currentTimeMillis();
                            long expiredTime = Long.parseLong(decryptInfo[1]);
                            if (expiredTime < now) {
                                response = new ResponseMessage(HttpStatus.REQUEST_TIMEOUT.value(), "Link quên mật khẩu hết hạn",
                                        new MessageContent(HttpStatus.REQUEST_TIMEOUT.value(), "Link quên mật khẩu hết hạn", null));
                            } else {
                                response = new ResponseMessage(new MessageContent(bodyParam));
                            }
                        }
                    }
                }
            }
        }
        return response;
    }

    //Kieu link chua token
    public ResponseMessage updateForgotPassword(Map<String, Object> bodyParam, Map<String, String> headerParam) {
        ResponseMessage response = null;
        if (bodyParam == null || bodyParam.isEmpty()) {
            response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                    new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
        } else {
            String token = (String) bodyParam.get("token");
            String newPassword = (String) bodyParam.get("newPassword");
            String rePassword = (String) bodyParam.get("rePassword");

            String invalidData = new UserValidation().validateUpdateForgotPassword(token, newPassword, rePassword);
            if (invalidData != null) {
                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), invalidData,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), invalidData, null));
            } else {
                String decryptResult = AES.decryptAESbase(token, Constant.AES_KEY);
                if (StringUtil.isNullOrEmpty(decryptResult)) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Token không đúng",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "Token không đúng", null));
                } else {
                    String[] decryptInfo = decryptResult.split("&");
                    if (decryptInfo == null || decryptInfo.length < 2) {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Token không đúng",
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), "Token không đúng", null));
                    } else {
                        String userUuid = decryptInfo[0];
                        User user = userService.findByUuid(userUuid);

                        if (user == null || (user.getIsDelete() != null && (user.getStatus() == -1 || user.getIsDelete() == 1))) {
                            response = new ResponseMessage(HttpStatus.NOT_FOUND.value(), Constant.VALIDATION_DATA_NOT_FOUND,
                                    new MessageContent(HttpStatus.NOT_FOUND.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
                        } else {
                            long now = System.currentTimeMillis();
                            //15ph + 1ph cho ngoi nhin la 16ph
                            long expiredTime = Long.parseLong(decryptInfo[1]) + 60 * 1000;
                            if (expiredTime < now) {
                                response = new ResponseMessage(HttpStatus.REQUEST_TIMEOUT.value(), "Link quên mật khẩu hết hạn",
                                        new MessageContent(HttpStatus.REQUEST_TIMEOUT.value(), "Link quên mật khẩu hết hạn", null));
                            } else {
                                //Update password
                                user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
                                user.setSetPassword(1);
                                try {
                                    if (userService.changePassword(user)) {
                                        // Xác thực thông tin người dùng Request lên, nếu không xảy ra exception tức là thông tin hợp lệ
                                        Authentication authentication = null;
                                        try {
                                            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), newPassword));
                                        } catch (AuthenticationException ex) {
                                            LOGGER.error("Error to set new authentication >>> " + ex);
                                        }
                                        // Set thông tin authentication mới vào Security Context
                                        SecurityContextHolder.getContext().setAuthentication(authentication);
                                        response = new ResponseMessage(new MessageContent(bodyParam));
                                    } else {
                                        response = new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), null));
                                    }
                                } catch (Exception ex) {
                                    response = new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.toString(), null));
                                }
                            }
                        }
                    }
                }
            }
        }
        return response;
    }

    public ResponseMessage sendEmail(Map<String, Object> bodyParam, Map<String, String> headerParam) {
        ResponseMessage response = null;
        if (bodyParam == null || bodyParam.isEmpty()) {
            response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
        } else {
            String emailTo = (String) bodyParam.get("emailTo");
            String title = (String) bodyParam.get("title");
            String content = (String) bodyParam.get("content");
            String sign = (String) bodyParam.get("sign");
            LOGGER.info("emailTo: {}, title: {}, content: {}, sign: {}", emailTo, title, content, sign);
            String invalidData = new UserValidation().validateSendEmail(emailTo, title, content, sign);
            if (invalidData != null) {
                LOGGER.info("invalidData: {}", invalidData);
                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), invalidData, new MessageContent(HttpStatus.BAD_REQUEST.value(), invalidData, null));
            } else {
                MailContentDTO item = new MailContentDTO();
                item.setType("one");
                item.setFromName("CoLearn.vn");
                item.setEmailTitle(title);
                item.setEmailContent(content);
                item.setEmailTo(emailTo);
                if (idThreadManager.sendEmail(item)) {
                    LOGGER.info("Send email ok");
                    response = new ResponseMessage(new MessageContent("Gửi email thành công"));
                } else {
                    LOGGER.info("Send email INTERNAL_SERVER_ERROR");
                    response = new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), null));
                }
            }
        }
        return response;
    }

    public ResponseMessage listUserUnit(Map<String, Object> bodyParam) {
        ResponseMessage response = null;
        List<String> units = (List<String>) bodyParam.get("units");
        List<User> userList = userService.getUserByUnits(units);
        if (userList == null || userList.isEmpty()) {
            response = new ResponseMessage(new MessageContent(new ArrayList<>()));
        } else {
            List<String> users = userList.stream().map((item) -> item.getUuid()).collect(Collectors.toList());
            response = new ResponseMessage(new MessageContent(users));
        }

        return response;
    }

    private String getLastNLogLines(File file, int nLines) {
        StringBuilder s = new StringBuilder();
        try {
            Process p = Runtime.getRuntime().exec("tail -" + nLines + " " + file);
            java.io.BufferedReader input = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
            String line = null;
            //Here we first read the next line into the variable
            //line and then check for the EOF condition, which
            //is the return value of null
            while ((line = input.readLine()) != null) {
                s.append(line + '\n');
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return s.toString();
    }

    public ResponseMessage testRestTemplateInterceptor() {
        String urlRequest = "http://103.21.151.158:8689/v1.0/dbm/management/users?page=1&size=20";
        String result = restTemplate.getForObject(urlRequest, String.class);
        LOGGER.info("testRestTemplateInterceptor get all user result: {}", result);
        ResponseMessage response = new ResponseMessage(new MessageContent(result));
        return response;
    }

    public ResponseMessage testRestTemplateInterceptor2() {
        String urlRequest = "http://103.21.151.158:8689/v1.0/dbm/management/cameras";
        String result = restTemplate.getForObject(urlRequest, String.class);
        LOGGER.info("testRestTemplateInterceptor2 get all camera result: {}", result);
        ResponseMessage response = new ResponseMessage(new MessageContent(result));
        return response;
    }

    public ResponseMessage testRestTemplateInterceptor3() {
        boolean result = tokenService.removeAccessToken();
        LOGGER.info("testRestTemplateInterceptor3 remove cache BearerToken: {}", result);
        ResponseMessage response = new ResponseMessage(new MessageContent(result));
        return response;
    }

    public ResponseMessage getUserListForInternalService() {
        List<UserReceiverDTO> dtoList = null;
        List<User> userList = userService.findAll();
        if (userList != null && !userList.isEmpty()) {
            dtoList = new ArrayList<>();
            for (User user : userList) {
                dtoList.add(new UserReceiverDTO(user));
            }
        }
        return new ResponseMessage(new MessageContent(dtoList));
    }

    public ResponseMessage getListUserSelectbox(String urlParam, String requestPath) {

        AuthorizationResponseDTO dto = GetUrlParam(urlParam);
        ResponseMessage response = null;
        if (dto == null) {
            return new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Lỗi lấy dữ liệu người dùng từ Urlparam", new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Lỗi lấy dữ liệu người dùng từ Urlparam", null));
        } else {
            //Check ABAC quyền xem traffic track
            LOGGER.info("Check abac");
            ResultCheckDto resultCheckDto = authorizeABAC(null, null, dto.getUuid(), requestPath, "LIST");
            LOGGER.info("Nhận kết quả check abac");
            if (resultCheckDto != null && resultCheckDto.getStatus()) {
                List<UserReceiverDTO> dtoList = null;
                List<User> userList = userService.findAll();
                if (userList != null && !userList.isEmpty()) {
                    dtoList = new ArrayList<>();
                    for (User user : userList) {
                        dtoList.add(new UserReceiverDTO(user));
                    }
                }
                return new ResponseMessage(new MessageContent(dtoList));
            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thêm/sửa đối tượng theo dõi", new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thêm/sửa đối tượng theo dõi", null));
            }
        }
        return response;

    }

    public ResponseMessage findUserStage(String stage) {
        ResponseMessage response;
        List<User> users = userService.getUserByStage(stage);
        List<String> userList = new ArrayList<>();
        if (users != null && !users.isEmpty()) {
            userList = users.stream().map((item) -> item.getUuid()).collect(Collectors.toList());
        }
        response = new ResponseMessage(new MessageContent(userList));
        return response;
    }

    public ResponseMessage findById(String id) {
        ResponseMessage response = null;
        User user = userService.findByUuid(id);
        if (user == null) {
            response = new ResponseMessage(HttpStatus.NOT_FOUND.value(), Constant.VALIDATION_DATA_NOT_FOUND, new MessageContent(HttpStatus.NOT_FOUND.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
        } else {
            UserDetailDTO detailDTO = new UserDetailDTO(user);
            response = new ResponseMessage(new MessageContent(detailDTO));
        }
        return response;
    }

    public ResponseMessage findUserUnitId(Map<String, String> headerParam, @PathVariable String id) {
        ResponseMessage response;
        AuthorizationResponseDTO dto = getAuthorFromToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {

            List<User> userList = userService.findByGroup(id);
            response = new ResponseMessage(new MessageContent(userList));
        }
        return response;
    }

    public ResponseMessage getUserBySite(Map<String, String> headerParam, String requestPath, String method, String paramPath) {
        ResponseMessage response;
        AuthorizationResponseDTO dto = getAuthorFromToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            //New
            List<User> userList = userService.getUserBySiteId(paramPath);
            response = new ResponseMessage(new MessageContent(userList));
        }
        return response;
    }

    public ResponseMessage getUserBySiteList(Map<String, String> headerParam, String requestPath, String method, String paramPath) {
        ResponseMessage response;
        List<User> userList = userService.getUserBySiteId(paramPath);
        if (userList == null || userList.isEmpty()) {
            List<String> strings = new ArrayList<>();
            return response = new ResponseMessage(new MessageContent(strings));
        }
        List<String> result = userList.stream().map((item) -> item.getUuid()).collect(Collectors.toList());
        response = new ResponseMessage(new MessageContent(result));
        return response;
    }

    public ResponseMessage getListUserInternal(String urlParam) {
        ResponseMessage response = null;
        if (!StringUtil.isNullOrEmpty(urlParam)) {
            Map<String, String> params = StringUtil.getUrlParamValues(urlParam);
            String userIds = params.get("userIds");
            List<String> listIdUser = Arrays.asList(userIds.split(","));
            List<User> listUser = userService.findByUuidIn(listIdUser);
            response = new ResponseMessage(new MessageContent(HttpStatus.OK.value(), HttpStatus.OK.toString(), listUser));
        }
        return response;
    }
}
