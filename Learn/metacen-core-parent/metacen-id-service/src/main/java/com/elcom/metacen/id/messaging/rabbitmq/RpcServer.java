package com.elcom.metacen.id.messaging.rabbitmq;

import com.elcom.metacen.constant.ResourcePath;
import com.elcom.metacen.id.controller.AuthenController;
import com.elcom.metacen.id.controller.UnitController;
import com.elcom.metacen.id.controller.UserController;
import com.elcom.metacen.id.exception.ValidationException;
import com.elcom.metacen.message.RequestMessage;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.utils.StringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 *
 * @author Admin
 */
public class RpcServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    @Autowired
    private AuthenController authenController;

    @Autowired
    private UserController userController;

    @Autowired
    private UnitController unitController;

    @RabbitListener(queues = "${user.rpc.queue}",containerFactory = "rabbitListenerContainerFactory")
    public String processService(String json) throws ValidationException {
        try {
            LOGGER.info(" [-->] Server received request for " + json);
            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            RequestMessage request = mapper.readValue(json, RequestMessage.class);

            //Process here
            ResponseMessage response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null);
            if (request != null) {
                String requestPath = request.getRequestPath().replace(request.getVersion() != null
                        ? request.getVersion() : ResourcePath.VERSION, "");
                String urlParam = request.getUrlParam();
                String pathParam = request.getPathParam();
                Map<String, Object> bodyParam = request.getBodyParam();
                Map<String, String> headerParam = request.getHeaderParam();
                //GatewayDebugUtil.debug(requestPath, urlParam, pathParam, bodyParam, headerParam);
                //LOGGER.info(" [-->] Server received requestPath =========>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + requestPath);

                switch (request.getRequestMethod()) {
                    case "GET":
                        if ("/user".equalsIgnoreCase(requestPath) && pathParam != null && pathParam.length() > 0) // Get details
                        {
                            response = userController.getDetailUser(requestPath, request.getRequestMethod(), pathParam, headerParam);
                        } else if ("/user".equalsIgnoreCase(requestPath)) // Details via token
                        {
                            response = userController.getDetailUser(headerParam);
                        } else if ("/user/exist".equalsIgnoreCase(requestPath)) // Check exist email or mobile
                        {
                            response = userController.checkUserExist(urlParam);
                        } else if ("/user/group/internal".equalsIgnoreCase(requestPath)) // Forgot password
                        {
                            response = userController.getGroup(pathParam);
                        }
                        else if ("/user/forgotPassword".equalsIgnoreCase(requestPath)) // Forgot password
                        {
                            response = userController.forgotPassword(urlParam);
                        } else if ("/user/cms".equalsIgnoreCase(requestPath)) // CMS user management
                        {
                            response = userController.getAllUser(headerParam, requestPath, request.getRequestMethod(), urlParam);
                        }
                        else if ("/user/unit/all".equalsIgnoreCase(requestPath)) // CMS user management
                            if (!StringUtil.isNullOrEmpty(pathParam)) {
                                response = unitController.getUnitById(headerParam, requestPath, request.getRequestMethod(), pathParam, bodyParam);
                            }
                            else{
                                response = unitController.getGroupsList(headerParam, requestPath, request.getRequestMethod(), urlParam, bodyParam);
                            }
                        else if ("/user/site".equalsIgnoreCase(requestPath)) {
                            if (!StringUtil.isNullOrEmpty(pathParam)) {
                                response = userController.getUserBySite(headerParam, requestPath, request.getRequestMethod(), pathParam);
                            }
                        }

                        else if ("/user/site/list".equalsIgnoreCase(requestPath)) {
                            if (!StringUtil.isNullOrEmpty(pathParam)) {
                                response = userController.getUserBySiteList(headerParam, requestPath, request.getRequestMethod(), pathParam);
                            }
                        }
                        else if ("/user/internal".equalsIgnoreCase(requestPath)) {
                            if (!StringUtil.isNullOrEmpty(pathParam)) {
                                response = userController.findById(pathParam);
                            }else if (!StringUtil.isNullOrEmpty(urlParam)) {
                                response = userController.getListUserInternal(urlParam);
                            } else {
                                response = userController.getUserListForInternalService();
                            }
                        }
                        else if ("/user/internal/stage".equalsIgnoreCase(requestPath)) {
                            if (!StringUtil.isNullOrEmpty(pathParam)) {
                                response = userController.findById(pathParam);
                            }
                        }
                        else if ("/user/unit/all-user".equalsIgnoreCase(requestPath)) {
                            if (!StringUtil.isNullOrEmpty(pathParam)) {
                                response = userController.findUserUnitId(headerParam,pathParam);
                            }
                        } else if ("/user/internal/stage/url".equalsIgnoreCase(requestPath)) {
                            if (!StringUtil.isNullOrEmpty(pathParam)) {
                                response = userController.findUserStage(pathParam);
                            }
                        }
                        else if ("/user/unit/event".equalsIgnoreCase(requestPath)) {
                            response = unitController.getGroupsByEventList(headerParam, requestPath, request.getRequestMethod(), urlParam, bodyParam);
                        }
                        else if ("/user/unit/internal".equalsIgnoreCase(requestPath)) {
                            response = unitController.getUnitByIdInternal(pathParam);
                        }
                        else if ("/user/unit/manager".equalsIgnoreCase(requestPath)) {
                            response = unitController.getManagerList(headerParam, requestPath, request.getRequestMethod(), urlParam, bodyParam);
                        }  else if ("/user/all/internal".equalsIgnoreCase(requestPath)) {
                            response = userController.getAllUserInternal();
                        } else if ("/group/all/internal".equalsIgnoreCase(requestPath)) {
                            response = unitController.getAllGroupInternal();
                        }
                        break;
                    case "POST":
                        if ("/user".equalsIgnoreCase(requestPath)) // Insert/update
                        {
                            response = userController.createUser(requestPath, request.getRequestMethod(), headerParam, bodyParam,urlParam);
                        }  else if ("/user/login".equalsIgnoreCase(requestPath)) //Login
                        {
                            response = authenController.userLogin(requestPath, headerParam, bodyParam);
                        }  else if ("/user/refreshToken".equalsIgnoreCase(requestPath)) //Details via token
                        {
                            response = authenController.refreshToken(headerParam);
                        }
                        else if ("/user/authentication".equalsIgnoreCase(requestPath)) //authentication
                        {
                            response = authenController.authorized(requestPath, headerParam);
                        } else if ("/user/uuidLst".equalsIgnoreCase(requestPath)) // List user by list uuid
                        {
                            response = userController.findByUuid(headerParam, bodyParam);
                        } else if ("/user/uuidAvatarLst".equalsIgnoreCase(requestPath)) // List user avatar by list uuid
                        {
                            response = userController.findAvatarByUuid(bodyParam);
                        }
                        else if ("/user/forgotPassword/checkToken".equalsIgnoreCase(requestPath)) // Check token
                        {
                            response = userController.checkToken(bodyParam, headerParam);
                        } else if ("/user/sendEmail".equalsIgnoreCase(requestPath)) // Check token
                        {
                            response = userController.sendEmail(bodyParam, headerParam);
                        }  else if ("/user/listUnit/internal".equalsIgnoreCase(requestPath)) // Check token
                        {
                            response = userController.listUserUnit(bodyParam);
                        } else if ("/group/all/internal/report".equalsIgnoreCase(requestPath)) {
                            response = unitController.getGroupsByList(bodyParam);
                        } else if("/user/unit".equalsIgnoreCase(requestPath)) // Insert/update
                        {
                            response = unitController.createUnit(headerParam, bodyParam, requestPath, request.getRequestMethod());
                        }
                        break;
                    case "PUT":
                        if ("/user/internal".equalsIgnoreCase(requestPath) && !StringUtil.isNullOrEmpty(pathParam))//Update via service
                        {
                            response = userController.updateUserInternal(bodyParam, headerParam, pathParam);
                        } else if ("/user".equalsIgnoreCase(requestPath))//Update JWT
                        {
                            response = userController.updateUser(bodyParam, headerParam, pathParam, request.getRequestMethod(), requestPath,urlParam);
                        } else if ("/user/email".equalsIgnoreCase(requestPath))//Change email
                        {
                            response = userController.updateEmail(bodyParam, headerParam);
                        } else if ("/user/mobile".equalsIgnoreCase(requestPath))//Change mobile
                        {
                            response = userController.updateMobile(bodyParam, headerParam);
                        } else if ("/user/password".equalsIgnoreCase(requestPath))//Change password
                        {
                            response = userController.updatePassword(bodyParam, headerParam);
                        } else if ("/user/status".equalsIgnoreCase(requestPath))//Change status
                        {
                            response = userController.updateStatus(bodyParam, headerParam);
                        } else if ("/user/social/mobile".equalsIgnoreCase(requestPath)) // Update mobile khi register qua social & send sms xac thuc
                        {
                            response = userController.updateSocialMobile(bodyParam, headerParam);
                        } else if ("/user/forgotPassword".equalsIgnoreCase(requestPath))//Change password from forgot password
                        {
                            response = userController.updateForgotPassword(bodyParam, headerParam);
                        }
                        if ("/user/unit".equalsIgnoreCase(requestPath)) // Insert/update
                        {
                            response = unitController.updateGroup(requestPath,headerParam, bodyParam, request.getRequestMethod(), pathParam);
                        }
                        else if ("/user/active".equalsIgnoreCase(requestPath))//active
                        {
                            response = userController.updateActive(requestPath, headerParam, pathParam);
                        }
                        break;
                    case "PATCH":
                        break;
                    case "DELETE":
                        if ("/user".equalsIgnoreCase(requestPath) && pathParam != null && pathParam.length() > 0) // Delete by id
                        {
                            response = userController.deleteUser(requestPath, request.getRequestMethod(), pathParam, headerParam,urlParam);
                        } else if ("/user".equalsIgnoreCase(requestPath)) // Delete by list id
                        {
                            response = userController.deleteUser(requestPath, request.getRequestMethod(), bodyParam, headerParam,urlParam);
                        }
                        else if ("/user/unit".equalsIgnoreCase(requestPath)) // Delete by list id
                        {
                            response = unitController.deleteUnit(requestPath,headerParam, bodyParam, request.getRequestMethod(), pathParam);
                        }
                        else if ("/user/unit/multi-delete".equalsIgnoreCase(requestPath)) // Delete by list id
                        {
                            response = unitController.deleteByListId(headerParam,requestPath, request.getRequestMethod(), pathParam, bodyParam);
                        }
                        break;
                    default:
                        break;
                }
            }
            LOGGER.info(" [<--] Server returned {}", (response != null ? response.toJsonString() : "null"));
            return response != null ? response.toJsonString() : null;
        } catch (Exception ex) {
            LOGGER.error("Error to processService >>> {}", ExceptionUtils.getStackTrace(ex));
            ex.printStackTrace();
        }
        return null;
    }
}
