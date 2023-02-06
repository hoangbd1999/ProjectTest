/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.menumanagement.controller;

import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.menumanagement.constant.Constant;
import com.elcom.metacen.menumanagement.dto.AuthorizationResponseDTO;
import com.elcom.metacen.menumanagement.dto.UpsertRoleMenuRequest;
import com.elcom.metacen.menumanagement.service.MenuManagementService;
import com.elcom.metacen.utils.StringUtil;
import com.elcom.metacen.menumanagement.dto.ABACResponseDTO;
import com.elcom.metacen.menumanagement.dto.GetRoleResponse;
import com.elcom.metacen.menumanagement.model.RoleMenu;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

/**
 *
 * @author Admin
 */
@Controller
public class MenuManagementController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MenuManagementController.class);

    @Autowired
    private MenuManagementService menuManagementService;

    public ResponseMessage findAllMenu(String requestUrl, String method, String pathParam,
            Map<String, String> headerMap) throws ExecutionException, InterruptedException {
        ResponseMessage response = null;

        AuthorizationResponseDTO dto = authenToken(headerMap);
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> body = new HashMap<String, Object>();
            ABACResponseDTO abacStatus = authorizeABAC(body, "LIST", dto.getUuid(), requestUrl);

            if (abacStatus.getStatus()) {
                response = new ResponseMessage(new MessageContent(menuManagementService.findAllMenu()));

            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem menu",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem menu", null));
            }
        }
        return response;
    }

    public ResponseMessage findAllRelationResource(String requestUrl, String method, String pathParam,
            Map<String, String> headerMap) throws ExecutionException, InterruptedException {
        ResponseMessage response = null;

        AuthorizationResponseDTO dto = authenToken(headerMap);
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> body = new HashMap<String, Object>();
            ABACResponseDTO abacStatus = authorizeABAC(body, "LIST", dto.getUuid(), requestUrl);

            if (abacStatus.getStatus()) {
                response = new ResponseMessage(new MessageContent(menuManagementService.findAllRelationResourceses()));

            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem menu",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem menu", null));
            }
        }
        return response;
    }

    public ResponseMessage findRoleMenuByUser(String requestUrl, String method, String pathParam,
            Map<String, String> headerMap)
            throws ExecutionException, InterruptedException {
        ResponseMessage response = null;

        Map<String, Object> body = new HashMap<String, Object>();
        // lấy listRole 
        List<String> listRoleResponses = getListRoleString(pathParam);
        List<RoleMenu> listRoleMenu = new ArrayList<>();
        List<Integer> listRoleCodeString = new ArrayList<>();
        for (String role : listRoleResponses) {
            for (RoleMenu roleMenu : menuManagementService.findByRoleCode(role)) {
                if (!listRoleCodeString.contains(roleMenu.getMenuId().getId())) {
                    listRoleMenu.add(roleMenu);
                    listRoleCodeString.add(roleMenu.getMenuId().getId());
                }
            }

            response = new ResponseMessage(new MessageContent(listRoleMenu));
        }

        return response;
    }

    // get list role menu tu header
    public ResponseMessage findRoleMenu(String requestUrl, String method, String pathParam,
            Map<String, String> headerMap) throws ExecutionException, InterruptedException {
        ResponseMessage response = null;

        AuthorizationResponseDTO dto = authenToken(headerMap);
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {

            // lấy listRole 
            List<GetRoleResponse> listRoleResponses = getListRole(headerMap);
            List<RoleMenu> listRoleMenu = new ArrayList<>();
            List<Integer> listRoleCodeString = new ArrayList<>();
            for (GetRoleResponse role : listRoleResponses) {
                for (RoleMenu roleMenu : menuManagementService.findByRoleCode(role.getRoleCode())) {
                    if (!listRoleCodeString.contains(roleMenu.getMenuId().getId())) {
                        listRoleMenu.add(roleMenu);
                        listRoleCodeString.add(roleMenu.getMenuId().getId());
                    }
                }

            }

            response = new ResponseMessage(new MessageContent(listRoleMenu));

        }
        return response;
    }

    public ResponseMessage findRoleMenuByRoleCode(Map<String, String> headerParam, String requestPath, String requestMethod, String urlParam) throws ExecutionException, InterruptedException {
        ResponseMessage response = null;

        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> body = new HashMap<String, Object>();
            ABACResponseDTO abacStatus = authorizeABAC(body, "LIST", dto.getUuid(), requestPath);

            if (abacStatus != null && abacStatus.getStatus()) {

                Map<String, String> params = StringUtil.getUrlParamValues(urlParam);
                String roleCode = params.get("role");
                response = new ResponseMessage(new MessageContent(menuManagementService.findByRoleCode(roleCode)));

            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem quyền menu ",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem quyền menu ", null));
            }
        }
        return response;
    }

    public ResponseMessage createRoleMenu(String requestPath, Map<String, String> headerParam,
            Map<String, Object> bodyParam, String requestMethod, String pathParam, String urlParam) throws ExecutionException, InterruptedException, JsonProcessingException, IOException {
        ResponseMessage response = null;

        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> body = new HashMap<String, Object>();
            ABACResponseDTO abacStatus = authorizeABAC(body, "POST", dto.getUuid(), requestPath);

            if (abacStatus.getStatus()) {
                String roleCode = (String) bodyParam.get("roleCode");
                if (roleCode == null) {
                    response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), "roleCode không được để trống ",
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), "roleCode không được để trống ", null));
                } else {
                    ObjectMapper mapper = new ObjectMapper();
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    mapper.setDateFormat(df);
                    List<RoleMenu> listRoleMenu = mapper.convertValue(
                            bodyParam.get("menuList"),
                            new TypeReference<List<RoleMenu>>() {
                    });

                    Date now = new Date();
                    for (RoleMenu roleMenu : listRoleMenu) {
                        roleMenu.setRoleCode(roleCode);
                        roleMenu.setCreatedAt(now);
                    }
                    menuManagementService.deleteAllRoleMenu(menuManagementService.findByRoleCode(roleCode));
                    menuManagementService.saveAllRoleMenu(listRoleMenu);

                    response = new ResponseMessage(new MessageContent(menuManagementService.findByRoleCode(roleCode)));
                }

            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền chỉnh sửa quyền menu ",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền chỉnh sửa quyền menu ", null));
            }
        }
        return response;
    }

}
