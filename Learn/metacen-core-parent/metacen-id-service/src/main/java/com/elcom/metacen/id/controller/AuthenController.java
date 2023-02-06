package com.elcom.metacen.id.controller;

import com.elcom.metacen.id.auth.CustomUserDetails;
import com.elcom.metacen.id.auth.jwt.JwtTokenProvider;
import com.elcom.metacen.id.constant.Constant;
import com.elcom.metacen.id.messaging.rabbitmq.RabbitMQClient;
import com.elcom.metacen.id.model.User;
import com.elcom.metacen.id.model.dto.AuthorizationResponseDTO;
import com.elcom.metacen.id.service.AuthService;
import com.elcom.metacen.id.service.UserService;
import com.elcom.metacen.id.thread.IdThreadManager;
import com.elcom.metacen.id.utils.JWTutils;
import com.elcom.metacen.id.validation.UserValidation;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.utils.DateUtil;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

/**
 * @author Admin
 */
@Controller
public class AuthenController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenController.class);

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;


    //Login
    public ResponseMessage userLogin(String requestPath, Map<String, String> headerParam, Map<String, Object> bodyParam) {
        ResponseMessage response = null;
        if (bodyParam == null || bodyParam.isEmpty()) {
            response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                    new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
        } else {
            String email = (String) bodyParam.get("email");
            String username = (String) bodyParam.get("username");
            String userInfo = (String) bodyParam.get("userInfo");
            String password = (String) bodyParam.get("password");
            String loginIp = (String) bodyParam.get("loginIp");
            if (StringUtil.isNullOrEmpty(userInfo)) {
                userInfo = email;
            }
            if (StringUtil.isNullOrEmpty(userInfo)) {
                userInfo = username;
            }

            String invalidData = new UserValidation().validateLogin(userInfo, password);
            if (invalidData != null) {
                response = new ResponseMessage(HttpStatus.BAD_REQUEST.value(), invalidData,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), invalidData, null));
            } else {
                // Check exist account with email or mobile or user_name
                User existUser = userService.findByEmailOrMobileOrUserName(userInfo);
                if (existUser == null) {
                    invalidData = "Tài khoản không tồn tại";
                    return new ResponseMessage(HttpStatus.NOT_FOUND.value(), invalidData,
                            new MessageContent(HttpStatus.NOT_FOUND.value(), invalidData, null));
                } else {
                    // Xác thực thông tin người dùng Request lên, nếu không xảy ra exception tức là thông tin hợp lệ
                    Authentication authentication = null;
                    try {
                        authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userInfo, password));
                    } catch (AuthenticationException ex) {
                        LOGGER.error(ex.toString());
                        invalidData = "Tài khoản hoặc mật khẩu không đúng";
                        return new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), invalidData,
                                new MessageContent(HttpStatus.UNAUTHORIZED.value(), invalidData, null));
                    }
                    // Set thông tin authentication vào Security Context
                    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                    if (userDetails.getUser().getStatus() == User.STATUS_LOCK) {
                        response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), Constant.VALIDATION_ACCOUNT_LOCKED,
                                new MessageContent(HttpStatus.UNAUTHORIZED.value(), Constant.VALIDATION_ACCOUNT_LOCKED, null));
                    } else {
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        //Set last login
                        userService.updateLastLogin(userDetails.getUser().getUuid(), loginIp);
                        // Trả về jwt cho người dùng.
                        String accessJwt = tokenProvider.generateToken(userDetails);
                        String refreshJwt = JWTutils.createToken(userDetails.getUser().getUuid());
                        //LoginResponse loginResponse = new LoginResponse(accessJwt, refreshJwt);
                        AuthorizationResponseDTO responseDTO = new AuthorizationResponseDTO(userDetails, accessJwt, refreshJwt);
                        response = new ResponseMessage(new MessageContent(responseDTO));
                    }
                }
            }
        }
        return response;
    }

    //Get New AccessToken And refreshToken
    public ResponseMessage refreshToken(Map<String, String> headerParam) {
        ResponseMessage response = null;
        response = getAuthorFromRefreshToken(headerParam);
        if (response.getData().getStatus() == 200) {
            String uuid = (String) response.getData().getData();
            String accessJwt = tokenProvider.generateToken1(uuid);
            String refreshJwt = JWTutils.createToken(uuid);
            //LoginResponse loginResponse = new LoginResponse(accessJwt, refreshJwt);
            AuthorizationResponseDTO responseDTO = new AuthorizationResponseDTO(accessJwt, refreshJwt);
            return new ResponseMessage(new MessageContent(responseDTO));
        } else
            return response;

    }

    //Authentication
    public ResponseMessage authorized(String requestPath, Map<String, String> headerParam) {
        ResponseMessage response = null;
        if (headerParam == null || headerParam.isEmpty()) {
            response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                    new MessageContent(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), null));
        } else {
            String bearerToken = headerParam.get("authorization");
            // Kiểm tra xem header Authorization có chứa thông tin jwt không
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                try {
                    long start = System.currentTimeMillis();
                    String jwt = bearerToken.substring(7);
                    LOGGER.info("jwt => " + jwt);
                    String uuid = tokenProvider.getUuidFromJWT(jwt);
                    long end = System.currentTimeMillis();
                    LOGGER.info("Parse token in: {} ms", (end - start), jwt);
                    UserDetails userDetails = authService.loadUserByUuid(uuid);
                    end = System.currentTimeMillis();
                    LOGGER.info("Get UserDetails in: {} ms", (end - start), jwt);
                    if (userDetails != null) {
                        User user = ((CustomUserDetails) userDetails).getUser();
                        if (user.getStatus() == User.STATUS_LOCK) {
                            response = new ResponseMessage(HttpStatus.UNAUTHORIZED.value(), Constant.VALIDATION_ACCOUNT_LOCKED,
                                    new MessageContent(HttpStatus.UNAUTHORIZED.value(), Constant.VALIDATION_ACCOUNT_LOCKED, null));
                        } else {
                            // Login hang ngay => Call score service
                            if (user.getLastLogin() == null || !DateUtil.isSameDay(user.getLastLogin(), new Date())) {
                                user.setLastLogin(new Timestamp(System.currentTimeMillis()));
                            }
                            //
                            UsernamePasswordAuthenticationToken authentication
                                    = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            end = System.currentTimeMillis();
                            LOGGER.info("Save authentication for Spring security in: {} ms", (end - start), jwt);
                            AuthorizationResponseDTO responseDTO = new AuthorizationResponseDTO((CustomUserDetails) authentication.getPrincipal(), null, null);
                            end = System.currentTimeMillis();
                            LOGGER.info("Authen JWT Token in: {}ms for {}", (end - start), jwt);
                            response = new ResponseMessage(new MessageContent(responseDTO));
                        }
                    } else {
                        response = new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(),
                                new MessageContent(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), null));
                    }
                } catch (Exception ex) {
                    LOGGER.error("failed on set user authentication", ex);
                    response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                            new MessageContent(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), null));
                }
            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                        new MessageContent(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), null));
            }
        }
        return response;
    }
}
