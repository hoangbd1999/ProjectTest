package com.elcom.abac.controller;

import com.elcom.abac.dto.AuthorizationResponseDTO;
import com.elcom.abac.dto.ResultCheckDto;
import com.elcom.abac.model.RelationResources;
import com.elcom.abac.model.Role;
import com.elcom.abac.service.RelationResourcesService;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import javax.persistence.Access;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Controller
public class RelationResourceController extends BaseController{

    @Autowired
    private RelationResourcesService relationResourcesService;

    public ResponseMessage findAllRelationResource(Map<String, String> headerMap) throws ExecutionException, InterruptedException, TimeoutException {
        ResponseMessage response = null;

        AuthorizationResponseDTO dto = authenToken(headerMap);
        if (dto == null) {
            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập",
                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> bodyParamCheck = new HashMap<>();
            bodyParamCheck.put("api","/v1.0/abac/role");
            bodyParamCheck.put("method","LIST");
            bodyParamCheck.put("uuid",dto.getUuid());
            ResultCheckDto resultCheckDto = authorizeABAC(bodyParamCheck,null);
            if(resultCheckDto.getStatus()){
                List<RelationResources> relationResources = relationResourcesService.findAllRelationResources();
                response = new ResponseMessage(new MessageContent(relationResources));
            }else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện xem tài nguyên phụ thuộc",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện xem tài nguyên phụ thuộc",
                                null));
            }
        }
        return response;
    }
}
