package com.elcom.abac.controller;

import com.elcom.abac.constant.Constant;
import com.elcom.abac.dto.AuthorizationResponseDTO;
import com.elcom.abac.dto.ResultCheckDto;
import com.elcom.abac.model.Role;
import com.elcom.abac.model.RoleUser;
import com.elcom.abac.repository.RedisRepository;
import com.elcom.abac.service.*;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Controller
public class RoleUserController {

    @Autowired
    private BaseController baseController;

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleUserService roleUserService;

    @Autowired
    private RedisRepository redisRepository;



    public ResponseMessage createRoleUser(Map<String, String> headerParam, Map<String, Object> bodyParam, String urlParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.GetUrlParam(urlParam);
        ResponseMessage response = null;
        if (dto == null) {
            return new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Lỗi lấy dữ liệu người dùng từ Urlparam",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Lỗi lấy dữ liệu người dùng từ Urlparam", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api","/v1.0/abac/user");
            bodyParamCheck.put("method","POST");
            bodyParamCheck.put("uuid",dto.getUuid());
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("roleCode", bodyParam.get("roleCode"));
            bodyParamCheck.put("attributes", attributes);
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck,null);
               if(resultCheckDto.getStatus()) {
                   String uuidUser = (String) bodyParam.get("uuidUser");
                   if(uuidUser==null) {
                       response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                               new MessageContent(HttpStatus.BAD_REQUEST.value(),"UuidUser không thể trống", null));
                       return response;
                   }
                   String roleCode = (String) bodyParam.get("roleCode");
                   if(roleCode==null) {
                       response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                               new MessageContent(HttpStatus.BAD_REQUEST.value(),"RoleCode không thể trống", null));
                       return response;
                   }
                   Optional<RoleUser> roleUser = roleUserService.findByUuidUserAndRoleCode(uuidUser,roleCode);
                   if(roleUser.isPresent()) {
                       response = new ResponseMessage(new MessageContent(roleUser.get()));
                   }else {
                       Optional<Role> role = roleService.findByRoleCode(roleCode);
                       if(role.isPresent()){
                           RoleUser roleUserSave = new RoleUser();
                            roleUserSave.setRoleCode(roleCode);
                            roleUserSave.setUuidUser(uuidUser);
                            roleUserSave = roleUserService.save(roleUserSave);
                            response = new ResponseMessage(new MessageContent(roleUserSave));
                       }else {
                           response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                                   new MessageContent(HttpStatus.BAD_REQUEST.value(),"Role không tồn tại", null));
                       }
               }

            }else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thêm vào dữ liệu check quyền",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thêm vào dữ liệu check quyền",
                                null));
            }
        }
        return response;
    }

    public ResponseMessage createRoleUserGateway(Map<String, String> headerParam, Map<String, Object> bodyParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api","/v1.0/abac/user");
            bodyParamCheck.put("method","POST");
            bodyParamCheck.put("uuid",dto.getUuid());
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("roleCode", bodyParam.get("roleCode"));
            bodyParamCheck.put("attributes", attributes);
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck,null);
            if(resultCheckDto.getStatus()) {
                String uuidUser = (String) bodyParam.get("uuidUser");
                if(uuidUser==null) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(),"UuidUser không thể trống", null));
                    return response;
                }
                String roleCode = (String) bodyParam.get("roleCode");
                if(roleCode==null) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(),"UuidUser không thể trống", null));
                    return response;
                }
                Optional<RoleUser> roleUser = roleUserService.findByUuidUserAndRoleCode(uuidUser,roleCode);
                if(roleUser.isPresent()) {
                    response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Uuid đã tồn tại trong nhóm role",
                            new MessageContent(HttpStatus.FORBIDDEN.value(), "Uuid đã tồn tại trong nhóm role",
                                    null));
                }else {
                    Optional<Role> role = roleService.findByRoleCode(roleCode);
                    if(role.isPresent()){
                        RoleUser roleUserSave = new RoleUser();
                        roleUserSave.setRoleCode(roleCode);
                        roleUserSave.setUuidUser(uuidUser);
                        roleUserSave = roleUserService.save(roleUserSave);
                        response = new ResponseMessage(new MessageContent(roleUserSave));
                    }else {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                                new MessageContent(HttpStatus.BAD_REQUEST.value(),"Role không tồn tại", null));
                    }
                }

            }else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thêm vào dữ liệu check quyền",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thêm vào dữ liệu check quyền",
                                null));
            }
        }
        return response;
    }

    public ResponseMessage updateRoleUser(Map<String, String> headerParam, Map<String, Object> bodyParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api","/v1.0/abac/user");
            bodyParamCheck.put("method","PUT");
            bodyParamCheck.put("uuid",dto.getUuid());
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("roleCode", bodyParam.get("roleCode"));
            bodyParamCheck.put("attributes", attributes);
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck,null);
            if(resultCheckDto.getStatus()) {
                String uuidUser = (String) bodyParam.get("uuidUser");
                if(uuidUser==null) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(),"UuidUser không thể trống", null));
                    return response;
                }
                String roleCode = (String) bodyParam.get("roleCode");
                if(roleCode==null) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(),"roleCode không thể trống", null));
                    return response;
                }
                String newRole = (String) bodyParam.get("newRole");
                if(newRole==null) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(),"newRole không thể trống", null));
                    return response;
                }
                Optional<RoleUser> roleUserTemp = roleUserService.findByUuidUserAndRoleCode(uuidUser,roleCode);
                Integer id;
                if(roleUserTemp.isPresent()){
                    id = roleUserTemp.get().getId();
                }else {
                    return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Không tìm được bản ghi cần update",
                            new MessageContent(HttpStatus.FORBIDDEN.value(), "Không tìm được bản ghi cần update",
                                    null));
                }
                Optional<RoleUser> roleUser = roleUserService.findByUuidUserAndRoleCode(uuidUser,newRole);
                if(roleUser.isPresent()) {
//                    response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Uuid đã tồn tại trong nhóm newRole",
//                            new MessageContent(HttpStatus.FORBIDDEN.value(), "Uuid đã tồn tại trong nhóm newRole",
//                                    null));
                    response = new ResponseMessage(new MessageContent(roleUser.get()));
                }else {
                    Optional<Role> role = roleService.findByRoleCode(newRole);
                    if(role.isPresent()){
                        RoleUser roleUserSave = new RoleUser();
                        roleUserSave.setId(id);
                        roleUserSave.setRoleCode(newRole);
                        roleUserSave.setUuidUser(uuidUser);
                        roleUserSave = roleUserService.update(roleUserSave);
                        if(roleUserSave == null){
                            response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Không tìm thấy bản ghi",
                                    new MessageContent(HttpStatus.BAD_REQUEST.value(),"Không tìm thấy bản ghi", null));
                        }else
                        response = new ResponseMessage(new MessageContent(roleUserSave));
                    }else {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                                new MessageContent(HttpStatus.BAD_REQUEST.value(),"Role không tồn tại", null));
                    }
                }

            }else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thêm vào dữ liệu check quyền",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thêm vào dữ liệu check quyền",
                                null));
            }
        }
        return response;
    }

    public ResponseMessage getListByUser(Map<String, String> headerParam,String param) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api","/v1.0/abac/policy");
            bodyParamCheck.put("method","POST");
            bodyParamCheck.put("uuid",dto.getUuid());
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck,null);
            if(resultCheckDto.getStatus()) {
                if (param == null || param.isEmpty()) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
                } else {
                    List<RoleUser> roleUsers = roleUserService.findByUuidUser(param);
                    if(roleUsers.isEmpty()){
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_DATA_NOT_FOUND,
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
                    }else {
                        response = new ResponseMessage(new MessageContent(roleUsers));
                    }
                }
            }else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem dữ liệu quyền người dùng",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem dữ liệu quyền người dùng",
                                null));
            }
        }
        return response;
    }

    public ResponseMessage getListUser(Map<String, String> headerParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api","/v1.0/abac/policy");
            bodyParamCheck.put("method","POST");
            bodyParamCheck.put("uuid",dto.getUuid());
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck,null);
            if(resultCheckDto.getStatus()) {
                List<String> uuids = roleUserService.findAllUuid();
                response = new ResponseMessage(new MessageContent(uuids));

            }else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem dữ liệu quyền người dùng",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem dữ liệu quyền người dùng",
                                null));
            }
        }
        return response;
    }

    public ResponseMessage getListAdmin() throws ExecutionException, InterruptedException, TimeoutException {
        ResponseMessage response;
        Map<String,String> roleAdmin = redisRepository.findAdmin().getRoleCodeAdmin();
        List roleCode = new ArrayList();
        roleAdmin.forEach((k, v) ->
        {
            roleCode.add(k);
        });
        List<String> roleUsers = roleUserService.findRoleCode(roleCode);
        response = new ResponseMessage(new MessageContent(roleUsers));
        return response;
    }

    public ResponseMessage getListRoleCode(Map<String, Object> bodyParam)  {
        ResponseMessage response;
        List<String> roleCode = (List<String>) bodyParam.get("roleCodes");
        List<String> roleUsers = roleUserService.findRoleCode(roleCode);
        response = new ResponseMessage(new MessageContent(roleUsers));
        return response;
    }



    public ResponseMessage deleteRoleUser(Map<String, String> headerParam, String pathParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            if (pathParam == null || pathParam.isEmpty()) {
                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            } else {
                Integer id = Integer.parseInt(pathParam);
                if (id == null || id.equals(0L)) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
                } else {
                    Optional<RoleUser> roleUser = roleUserService.findById(id);
                    if (roleUser.isPresent()) {
                        Map<String, Object> bodyParamCheck = new HashMap<>();
                        bodyParamCheck.put("api","/v1.0/abac/user");
                        bodyParamCheck.put("method","DELETE");
                        bodyParamCheck.put("uuid",dto.getUuid());
                        Map<String,Object> attributes = new HashMap<>();
                        attributes.put("roleCode",roleUser.get().getRoleCode());
                        bodyParamCheck.put("attributes",attributes);
                        ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck,null);
                        if (resultCheckDto.getStatus()) {

                            Boolean result = roleUserService.deleteRoleUser(roleUser.get());
                            if (result) {
                                response = new ResponseMessage(new MessageContent("Xóa role thành công"));
                            } else {
                                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Không tìm thấy bản ghi cần xóa",
                                        new MessageContent(HttpStatus.BAD_REQUEST.value(), "Không tìm thấy bản ghi cần xóa", null));
                            }
                        } else {
                            response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xóa dữ liệu quyền người dùng",
                                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xóa dữ liệu quyền người dùng",
                                            null));
                        }
                    }else {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Không tìm thấy bản ghi cần xóa",
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), "Không tìm thấy bản ghi cần xóa", null));
                    }
                }
            }
        }
        return response;
    }


    public ResponseMessage deleteUser(Map<String, String> headerParam,Map<String, Object> bodyParam, String urlParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.GetUrlParam(urlParam);
        ResponseMessage response = null;
        if (dto == null) {
            return new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Lỗi lấy dữ liệu người dùng từ Urlparam",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Lỗi lấy dữ liệu người dùng từ Urlparam", null));
        } else {
            List<String> uuids = (List<String>) bodyParam.get("uuids");
            if (uuids == null || uuids.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "uuids không thể trống",
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), "uuids không thể trống", null));
            }
            List<RoleUser> roleUsers = new ArrayList<>();
            for (String pathParam : uuids) {
                Optional<RoleUser> roleUser = roleUserService.findByUser(pathParam);
                if (roleUser.isPresent()) {
                    Map<String, Object> bodyParamCheck = new HashMap<>();
                    bodyParamCheck.put("api", "/v1.0/abac/user");
                    bodyParamCheck.put("method", "DELETE");
                    bodyParamCheck.put("uuid", dto.getUuid());
                    Map<String, Object> attributes = new HashMap<>();
                    attributes.put("roleCode", roleUser.get().getRoleCode());
                    bodyParamCheck.put("attributes", attributes);
                    ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck, null);
                    if (resultCheckDto.getStatus()) {
                        roleUsers.add(roleUser.get());
                    } else {
                        return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xóa dữ liệu quyền người dùng hoặc vi phạm chính sách xóa người dùng",
                                new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xóa dữ liệu quyền người dùng hoặc vi phạm chính sách xóa người dùng",
                                        null));
                    }
                } else {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Không tìm thấy bản ghi cần xóa " + pathParam,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "Không tìm thấy bản ghi cần xóa " + pathParam, null));
                }
            }
            for (RoleUser roleUser : roleUsers
            ) {
                Boolean result = roleUserService.deleteRoleUser(roleUser);
                if (result) {
                    response = new ResponseMessage(new MessageContent("Xóa role thành công"));
                } else {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Không tìm thấy bản ghi cần xóa",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "Không tìm thấy bản ghi cần xóa", null));
                }

            }
        }
        return response;
    }
}
