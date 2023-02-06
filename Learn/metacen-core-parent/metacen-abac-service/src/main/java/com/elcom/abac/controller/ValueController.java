package com.elcom.abac.controller;

import com.elcom.abac.dto.AuthorizationResponseDTO;
import com.elcom.abac.dto.ResultCheckDto;
import com.elcom.abac.model.RoleEvent;
import com.elcom.abac.model.RoleNotify;
import com.elcom.abac.model.RoleState;
import com.elcom.abac.service.ValueService;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Controller
public class ValueController {

    @Autowired
    private ValueService valueService;

    @Autowired
    private BaseController baseController;

    public ResponseMessage getRoleState(Map<String, String> headerParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api","/v1.0/abac/policy");
            bodyParamCheck.put("method","LIST");
            bodyParamCheck.put("uuid",dto.getUuid());
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck,null);
            if(resultCheckDto.getStatus()) {
                    List<RoleState> roleStates = valueService.findRoleState();
                    response = new ResponseMessage(new MessageContent(roleStates));
            }else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem dữ liệu value state",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem dữ liệu value state",
                                null));
            }
        }
        return response;
    }

    public ResponseMessage getRoleEvent(Map<String, String> headerParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api","/v1.0/abac/policy");
            bodyParamCheck.put("method","LIST");
            bodyParamCheck.put("uuid",dto.getUuid());
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck,null);
            if(resultCheckDto.getStatus()) {
                    List<RoleEvent> roleEvents = valueService.findRoleEvent();
                    response = new ResponseMessage(new MessageContent(roleEvents));
            }else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem dữ liệu value event",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem dữ liệu value event",
                                null));
            }
        }
        return response;
    }

    public ResponseMessage getRoleNotify(Map<String, String> headerParam) throws ExecutionException, InterruptedException, TimeoutException {
        AuthorizationResponseDTO dto = baseController.authenToken(headerParam);
        ResponseMessage response;
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api","/v1.0/abac/policy");
            bodyParamCheck.put("method","LIST");
            bodyParamCheck.put("uuid",dto.getUuid());
            ResultCheckDto resultCheckDto = baseController.authorizeABAC(bodyParamCheck,null);
            if(resultCheckDto.getStatus()) {
                List<RoleNotify> roleNotifies = valueService.findRoleNotify();
                response = new ResponseMessage(new MessageContent(roleNotifies));
            }else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem dữ liệu value state",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem dữ liệu value state",
                                null));
            }
        }
        return response;
    }

}
