package com.elcom.abac.controller;

import com.elcom.abac.constant.Constant;
import com.elcom.abac.dto.AuthorizationResponseDTO;
import com.elcom.abac.dto.ResultCheckDto;
import com.elcom.abac.dto.RoleCodeUuidRedis;
import com.elcom.abac.model.Policy;
import com.elcom.abac.model.Role;
import com.elcom.abac.repository.RedisRepository;
import com.elcom.abac.service.*;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Controller
public class RoleController {

    @Autowired
    private BaseController baseController;

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleUserService roleGroupService;

    @Autowired
    private RedisRepository redisRepository;


    public ResponseMessage createRole( Map<String, String> headerParam, Map<String, Object> bodyParam,String urlParam) throws ExecutionException, InterruptedException, TimeoutException {
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
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("roleCode", "fake");
            bodyParamCheck.put("attributes", attributes);
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck,null);
            if(resultCheckDto.getStatus()){
                String roleCode = (String) bodyParam.get("roleCode");
                if(roleCode == null){
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "RoleCode không thể trống",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "RoleCode không thể trống",
                                    null));
                    return response;
                }
                String roleName = (String) bodyParam.get("roleName");
                if(roleName == null){
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "roleName không thể trống",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "roleName không thể trống",
                                    null));
                    return response;
                }
                String description = (String) bodyParam.get("description");
                if(description == null){
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "description không thể trống",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "description không thể trống",
                                    null));
                    return response;
                }
                Integer admin = (Integer) bodyParam.get("isAdmin");
                Integer parentId = (Integer) bodyParam.get("parentId");
                Role role = new Role(roleName,description,admin,roleCode);
                role.setParentId(parentId);

                role = roleService.saveRole(role);
                if(role == null){
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Đã tồn tại mã quyền '" +roleCode+"' trong hệ thống",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "Đã tồn tại mã quyền '" +roleCode+"' trong hệ thống",
                                    null));
                }else
                    response = new ResponseMessage( new MessageContent(role));
            }else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thêm vào dữ liệu quyền",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thêm vào dữ liệu quyền",
                                null));
            }
        }
        return response;
    }

    public ResponseMessage getRole(Map<String, String> headerParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            if(true){
                RoleCodeUuidRedis roleCodeUuidRedis = redisRepository.findRoleCodeRedis(dto.getUuid());
                List<String> roleCode = roleCodeUuidRedis.getRoleCode();
                List<Role> roles = roleService.findByRoleCodeIn(roleCode);
                response = new ResponseMessage(new MessageContent(roles));
            }else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện xem quyền",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện xem quyền",
                                null));
            }
        }
        return response;
    }

    public ResponseMessage getRoleListUserInternal(String urlParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.GetUrlParam(urlParam);
        ResponseMessage response = null;
        if (dto == null) {
            return new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Lỗi lấy dữ liệu người dùng từ Urlparam",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Lỗi lấy dữ liệu người dùng từ Urlparam", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api", "/v1.0/abac/role/list-user");
            bodyParamCheck.put("method", "LIST");
            bodyParamCheck.put("uuid", dto.getUuid());
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck, null);
            if (resultCheckDto.getStatus()) {
                List<String> uuids = roleGroupService.findAllUuid();
                List<Role> roleList = roleService.findAllRole();
                Map<String, List<Role>> roleUuids = new HashMap<>();
                List<RoleCodeUuidRedis> roleCodeUuidRediss = redisRepository.findRedisRoleCode();
                if (!uuids.isEmpty() && uuids != null && roleCodeUuidRediss != null) {
                    for (RoleCodeUuidRedis rolecodeUuid : roleCodeUuidRediss
                    ) {
                        List<String> roleCode = rolecodeUuid.getRoleCode();
                        List<Role> roles = roleList.stream().filter(camera -> roleCode.contains(camera.getRoleCode())).collect(Collectors.toList());
                        roleUuids.put(rolecodeUuid.getUuid(), roles);
                    }
                    response = new ResponseMessage(new MessageContent(roleUuids));
                } else {
                    response = new ResponseMessage(new MessageContent(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.toString(), null));
                }
            }else {
                return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Ko co quyen",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "ko co quyen", null));
            }
        }

        return response;
    }

    public ResponseMessage getRoleById(Map<String, String> headerParam, String param) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api","/v1.0/abac/role");
            bodyParamCheck.put("method","DETAIL");
            bodyParamCheck.put("uuid",dto.getUuid());
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck,null);
            if(resultCheckDto.getStatus()){
                if (param == null || param.isEmpty()) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "param không thể trống",null));
                } else {
                    Integer id = Integer.parseInt(param);
                    if (id == null || id.equals(0L)) {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                                new MessageContent(HttpStatus.BAD_REQUEST.value(),"id không hợp lệ",null));
                    } else {
                        Optional<Role> role = roleService.findById(id);
                        if(role.isPresent()){
                            response = new ResponseMessage(new MessageContent(role.get()));
                        }else {
                            response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_DATA_NOT_FOUND,
                                 new MessageContent(HttpStatus.BAD_REQUEST.value(),"Khong tim thay ban ghi",null));
                        }
                     }
                }
            }else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện xem quyền",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện xem quyền",
                                null));
            }
        }
        return response;
    }

    public ResponseMessage getAllRole(Map<String, String> headerParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api","/v1.0/abac/role");
            bodyParamCheck.put("method","LIST");
            bodyParamCheck.put("uuid",dto.getUuid());
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck,null);
            if(resultCheckDto.getStatus()){
                List<Role> roles = roleService.findAllRole();
                response = new ResponseMessage(new MessageContent(roles));
            }else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện xem quyền",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện xem quyền",
                                null));
            }
        }
        return response;
    }

    public ResponseMessage getSearch(Map<String, String> headerParam, String urlParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, String> params = StringUtil.getUrlParamValues(urlParam);
            String search = params.get("search");
            Integer page = params.get("page") != null ? Integer.valueOf(params.get("page")) : 1;
            Integer size = params.get("size") != null ? Integer.valueOf(params.get("size")) : 20;
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api","/v1.0/abac/role");
            bodyParamCheck.put("method","LIST");
            bodyParamCheck.put("uuid",dto.getUuid());
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck,null);
            if(resultCheckDto.getStatus()){
                Page<Role> roles;
                if (StringUtil.isNullOrEmpty(search)) {
                    roles = roleService.getAll(PageRequest.of(page-1, size));
                } else {
                    roles = roleService.searchBy(search, PageRequest.of(page-1, size));
                }

                response = new ResponseMessage(new MessageContent(roles.getContent(), roles.getTotalElements()));
            }else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện xem quyền",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện xem quyền",
                                null));
            }
        }
        return response;
    }

    public ResponseMessage getRoleOfUser(Map<String, String> headerParam,String param) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api","/v1.0/abac/role");
            bodyParamCheck.put("method","LIST");
            bodyParamCheck.put("uuid",dto.getUuid());
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck,null);
            if(resultCheckDto.getStatus()){
                RoleCodeUuidRedis roleCodeUuidRedis = redisRepository.findRoleCodeRedis(param);
                List<String> roleCode = roleCodeUuidRedis.getRoleCode();
                List<Role> roles = roleService.findByRoleCodeIn(roleCode);
                response = new ResponseMessage(new MessageContent(roles));
            }else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện xem quyền của người khác",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện xem quyền của người khác",
                                null));
            }
        }
        return response;
    }

    public ResponseMessage getRoleOfUserInternal(String param){
        ResponseMessage response;
        RoleCodeUuidRedis roleCodeUuidRedis = redisRepository.findRoleCodeRedis(param);
        List<String> roleCode = roleCodeUuidRedis.getRoleCode();
        response = new ResponseMessage(new MessageContent(roleCode));
        return response;
    }

    public ResponseMessage updateRole(Map<String, String> headerParam, Map<String, Object> bodyParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api","/v1.0/abac/policy");
            bodyParamCheck.put("method","PUT");
            bodyParamCheck.put("uuid",dto.getUuid());
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("roleCode", bodyParam.get("roleCode"));
            bodyParamCheck.put("attributes", attributes);
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck,null);
            if(resultCheckDto.getStatus()){
                Integer id = (Integer) bodyParam.get("id");
                if(id == null || id ==0){
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "id không thể trống",
                                    null));
                    return response;
                }
                String roleCode = (String) bodyParam.get("roleCode");
                if(roleCode == null){
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "RoleCode không thể trống",
                                    null));
                    return response;
                }
                String roleName = (String) bodyParam.get("roleName");
                if(roleName == null){
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "roleName không thể trống",
                                    null));
                    return response;
                }
                String description = (String) bodyParam.get("description");
                if(description == null){
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "description không thể trống",
                                    null));
                    return response;
                }
                Optional<Role> roleDb = roleService.findByRoleCode(roleCode);
                if(roleDb.isPresent()){
                    if(id!=roleDb.get().getId()){
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Đã tồn tại mã quyền '" +roleCode+"' trong hệ thống",
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), "Đã tồn tại mã quyền '" +roleCode+"' trong hệ thống",
                                        null));
                        return response;
                    }
                }
                Integer admin = (Integer) bodyParam.get("isAdmin");
                Integer parentId = (Integer) bodyParam.get("parentId");
                Role role = new Role(id,roleName, description, admin,roleCode);
                role.setParentId(parentId);
                role = roleService.updateRole(role);
                if(role == null){
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_DATA_NOT_FOUND,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "Không tìm thấy id bản ghi ",
                                    null));
                }else response = new ResponseMessage(new MessageContent(role));
            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thêm vào dữ liệu check quyền",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thêm vào dữ liệu check quyền",
                                null));
            }
        }
        return response;
    }

    public ResponseMessage deleteId( Map<String, String> headerParam,String pathParam) throws ExecutionException, InterruptedException, TimeoutException {
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
                    Optional<Role> role = roleService.findById(id);
                    if (role.isPresent()) {
                        Map<String, Object> bodyParamCheck = new HashMap<>();
                        bodyParamCheck.put("api","/v1.0/abac/role");
                        bodyParamCheck.put("method","DELETE");
                        bodyParamCheck.put("uuid",dto.getUuid());
                        Map<String,Object> attributes = new HashMap<>();
                        attributes.put("roleCode",role.get().getRoleCode());
                        bodyParamCheck.put("attributes",attributes);
                        ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck,null);
                        if (resultCheckDto.getStatus()) {

                            Boolean result = roleService.deleteRoleById(role.get());
                            if (result) {
                                response = new ResponseMessage(new MessageContent("Xóa role thành công"));
                            } else {
                                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Không tìm thấy bản ghi cần xóa",
                                        new MessageContent(HttpStatus.BAD_REQUEST.value(), "Không tìm thấy bản ghi cần xóa", null));
                            }
                        } else {
                            response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xóa dữ liệu nhóm quyền",
                                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xóa dữ liệu nhóm quyền",
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

    public ResponseMessage deleteMulti( Map<String, String> headerParam,Map<String,Object> bodyParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response =  new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Lỗi xảy ra",
                new MessageContent(HttpStatus.BAD_REQUEST.value(), "Lỗi xảy ra", null));;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            List<Integer> ids = (List<Integer>) bodyParam.get("ids");
            if (ids == null || ids.isEmpty()) {
                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "ids truyền không đúng định dạng",
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), "ids truyền không đúng định dạng", null));
            } else {
                for (Integer id: ids
                     ) {
                    if (id == null || id.equals(0L)) {
                        response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                                new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
                    } else {
                        Optional<Role> role = roleService.findById(id);
                        if (role.isPresent()) {
                            Map<String, Object> bodyParamCheck = new HashMap<>();
                            bodyParamCheck.put("api","/v1.0/abac/role");
                            bodyParamCheck.put("method","DELETE");
                            bodyParamCheck.put("uuid",dto.getUuid());
                            Map<String,Object> attributes = new HashMap<>();
                            attributes.put("roleCode",role.get().getRoleCode());
                            bodyParamCheck.put("attributes",attributes);
                            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck,null);
                            if (resultCheckDto.getStatus()) {

                                Boolean result = roleService.deleteRoleById(role.get());
                                if (result) {
                                    response = new ResponseMessage(new MessageContent("Xóa role thành công"));
                                } else {
                                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Không tìm thấy bản ghi cần xóa",
                                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "Không tìm thấy bản ghi cần xóa", null));
                                }
                            } else {
                                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xóa dữ liệu nhóm quyền",
                                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xóa dữ liệu nhóm quyền",
                                                null));
                            }
                        }else {
                            response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Không tìm thấy bản ghi cần xóa",
                                    new MessageContent(HttpStatus.BAD_REQUEST.value(), "Không tìm thấy bản ghi cần xóa", null));
                        }
                    }

                }

            }
        }
        return response;
    }

}
