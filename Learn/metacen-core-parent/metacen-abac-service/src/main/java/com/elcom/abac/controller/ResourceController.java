package com.elcom.abac.controller;

import com.elcom.abac.constant.Constant;
import com.elcom.abac.dto.AuthorizationResponseDTO;
import com.elcom.abac.dto.ResultCheckDto;
import com.elcom.abac.dto.RoleCodeUuidRedis;
import com.elcom.abac.model.Resource;
import com.elcom.abac.repository.RedisRepository;
import com.elcom.abac.service.*;
import com.elcom.abac.service.impl.CheckAuthenticationService;
import com.elcom.abac.service.impl.ResourceServiceImpl;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Controller
public class ResourceController {

    @Autowired
    private RoleUserService roleGroupService;

    @Autowired
    private BaseController baseController;

    @Autowired
    private ResourceServiceImpl resourceService;

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private CheckAuthenticationService checkAuthenticationService;

    public ResponseMessage createResource( Map<String, String> headerParam, Map<String, Object> bodyParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api","/v1.0/abac/resource");
            bodyParamCheck.put("method","POST");
            bodyParamCheck.put("uuid",dto.getUuid());
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck,null);
            if(resultCheckDto.getStatus()){
                String code = (String) bodyParam.get("code");
                if(code==null || code.isEmpty()){
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Code khong the trong",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "Code khong the trong", null));
                    return response;
                }
                Integer status = (Integer) bodyParam.get("status");
                if(status==null){
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "status khong the trong",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "status khong the trong", null));
                    return response;
                }
                String name = (String) bodyParam.get("name");
                if(name==null || name.isEmpty()){
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "name khong the trong",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "name khong the trong", null));
                    return response;
                }
                String urlpatterns = (String) bodyParam.get("urlpatterns");
                if(urlpatterns==null || urlpatterns.isEmpty()){
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "urlpatterns khong the trong",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "urlpatterns khong the trong", null));
                    return response;
                }
                Integer urlpatterns_length = (Integer) bodyParam.get("urlpatterns_length");
                if(urlpatterns_length==null || urlpatterns_length ==0){
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "urlpatterns_length khong the trong",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "urlpatterns_length khong the trong", null));
                    return response;
                }
                String createPolicyType = (String) bodyParam.get("createPolicyType");
                if(createPolicyType==null || createPolicyType.isEmpty()){
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "createPolicyType khong the trong",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "createPolicyType khong the trong", null));
                    return response;
                }
                String updatePolicyType = (String) bodyParam.get("updatePolicyType");
                if(updatePolicyType==null || updatePolicyType.isEmpty()){
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "updatePolicyType khong the trong",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "updatePolicyType khong the trong", null));
                    return response;
                }
                String deletePolicyType = (String) bodyParam.get("deletePolicyType");
                if(deletePolicyType==null || deletePolicyType.isEmpty()){
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "deletePolicyType khong the trong",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "deletePolicyType khong the trong", null));
                    return response;
                }
                String detailPolicyType = (String) bodyParam.get("detailPolicyType");
                if(detailPolicyType==null || detailPolicyType.isEmpty()){
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "detailPolicyType khong the trong",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "detailPolicyType khong the trong", null));
                    return response;
                }
                String listPolicyType = (String) bodyParam.get("listPolicyType");
                if(listPolicyType==null || listPolicyType.isEmpty()){
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "listPolicyType khong the trong",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "listPolicyType khong the trong", null));
                    return response;
                }
                String description = (String) bodyParam.get("description");
                String pipRpcExchange = (String) bodyParam.get("pipRpcExchange");
                String pipRpcQueue = (String) bodyParam.get("pipRpcQueue");
                String pipRpcKey = (String) bodyParam.get("pipRpcKey");
                String pipRpcPath = (String) bodyParam.get("pipRpcPath");
                Resource resource = new Resource();
                resource.setCode(code);
                resource.setDescription(description);
                resource.setName(name);
                resource.setUrlpatterns(urlpatterns);
                resource.setUrlpatternsLength(urlpatterns_length);
                resource.setCreatePolicyType(createPolicyType);
                resource.setUpdatePolicyType(updatePolicyType);
                resource.setDetailPolicyType(detailPolicyType);
                resource.setDeletePolicyType(deletePolicyType);
                resource.setListPolicyType(listPolicyType);
                resource.setPipRpcExchange(pipRpcExchange);
                resource.setPipRpcKey(pipRpcKey);
                resource.setPipRpcPath(pipRpcPath);
                resource.setPipRpcQueue(pipRpcQueue);
                resource.setStatus(status);
                resource = resourceService.saveResource(resource);
                if(resource== null) {
                    response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Lỗi parse",
                            new MessageContent(HttpStatus.FORBIDDEN.value(), "Lỗi parse",
                                    null));
                } else {
                    response = new ResponseMessage(new MessageContent("Tao resource thanh cong"));
                }
            }else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền them vao method",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền them vao method",
                                null));
            }
        }
        return response;
    }

    //Test Abac
    public ResponseMessage getResource(Map<String, String> headerParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api","/v1.0/abac/policy");
            bodyParamCheck.put("method","DETAIL");
            bodyParamCheck.put("uuid",dto.getUuid());
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck,null);
            if(resultCheckDto.getStatus()){
                RoleCodeUuidRedis roleCodeUuidRedis = redisRepository.findRoleCodeRedis(dto.getUuid());
                List<String> resourceCode = policyService.findResourceCode(roleCodeUuidRedis.getRoleCode());
                List<Resource> resources = resourceService.findByCodeIn(resourceCode);
//                List<String> listIds = authenticationService.findMethodByUuidGroupByMethod(dto.getUuid());
//                List<Method> methodList = methodService.findByIdIn(listIds);
                response = new ResponseMessage(new MessageContent(resources));
            }else {
                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "Khong co quyen ",
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), "Khong co quyen", "Không có quyền"));
                return response;
            }
        }
        return response;
    }

    public ResponseMessage getResourceInRole(Map<String, String> headerParam, String urlParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api","/v1.0/abac/resource");
            bodyParamCheck.put("method","LIST");
            bodyParamCheck.put("uuid",dto.getUuid());
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck,null);
            Map<String, String> mapParam = StringUtil.getUrlParamValues(urlParam);
            String param =mapParam.get("role");
            if(resultCheckDto.getStatus()){
//                Integer roleId = Integer.parseInt(param);
                List<String> resourceCodes = policyService.findBySubjectValueGroupBy(param);
                List<Resource> resources = resourceService.findByCodeIn(resourceCodes);
//                List<Long> groupIds = roleGroupService.findGroup(roleId);
                response = new ResponseMessage(new MessageContent(resources));
            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Không có quyền",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Không có quyền",
                                null));
            }
        }
        return response;
    }

    public ResponseMessage getAllResource( Map<String, String> headerParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api","/v1.0/abac/resource");
            bodyParamCheck.put("method","LIST");
            bodyParamCheck.put("uuid",dto.getUuid());
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck,null);
            if(resultCheckDto.getStatus()){
//            List<Method> methodList = methodService.findAll();
                List<Resource> resources = resourceService.findAllForUser();
                response = new ResponseMessage(new MessageContent(resources));
            }else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền ",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền ",
                                null));
            }
        }
        return response;
    }

    public ResponseMessage deleteResource(Map<String, String> headerParam,String pathParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null){
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api","/v1.0/abac/policy");
            // ĐỂ ko sử dụng policy DELETE chỉ check role
            bodyParamCheck.put("method","POST");
            bodyParamCheck.put("uuid",dto.getUuid());
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck,null);
            if(resultCheckDto.getStatus()){
                if (pathParam == null || pathParam.isEmpty()) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
                } else {
//               methodService.deleteByMethodId(pathParam);
                Integer id = Integer.parseInt(pathParam);
                resourceService.deleteResource(id);
                response = new ResponseMessage( new MessageContent("Xóa method thành công"));
                }
            }
            else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xóa method",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xóa method",
                                null));
            }
        }
        return response;
    }
}
