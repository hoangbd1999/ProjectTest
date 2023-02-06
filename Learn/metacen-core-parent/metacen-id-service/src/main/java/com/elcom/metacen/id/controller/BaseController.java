package com.elcom.metacen.id.controller;

import com.elcom.metacen.constant.ResourcePath;
import com.elcom.metacen.id.auth.CustomUserDetails;
import com.elcom.metacen.id.auth.jwt.JwtTokenProvider;
import com.elcom.metacen.id.auth.jwt.TokenParse;
import com.elcom.metacen.id.checkPolicy.ResultCheckDto;
import com.elcom.metacen.id.messaging.rabbitmq.RabbitMQClient;
import com.elcom.metacen.id.messaging.rabbitmq.RabbitMQProperties;
import com.elcom.metacen.id.model.Unit;
import com.elcom.metacen.id.model.User;
import com.elcom.metacen.id.model.dto.ABACResponseDTO;
import com.elcom.metacen.id.model.dto.AuthorizationResponseDTO;
import com.elcom.metacen.id.model.dto.DeleteDataPublishMessage;
import com.elcom.metacen.id.service.AuthService;
import com.elcom.metacen.id.utils.JWTutils;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.RequestMessage;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.utils.StringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Admin
 */
public class BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseController.class);

    private static byte[] intToBytesBigEndian(final int data) {
        return new byte[]{(byte) ((data >> 24) & 0xff), (byte) ((data >> 16) & 0xff),
            (byte) ((data >> 8) & 0xff), (byte) ((data >> 0) & 0xff),};
    }

    @Autowired
    private RabbitMQClient rabbitMQClient;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private JWTutils jwTutils;

    @Autowired
    private AuthService authService;

//    @Value("${abac.topic.request}")
//    String requestTopicAbac;

//    @Value("${kafka.topic.requestreply-topic}")
//    String requestReplyTopic;

    public AuthorizationResponseDTO authenToken(Map<String, String> headerMap) {
        //Authen -> call rpc authen headerMap
        RequestMessage userRpcRequest = new RequestMessage();
        userRpcRequest.setRequestMethod("POST");
        userRpcRequest.setRequestPath(RabbitMQProperties.USER_RPC_AUTHEN_URL);
        userRpcRequest.setVersion(ResourcePath.VERSION);
        userRpcRequest.setBodyParam(null);
        userRpcRequest.setUrlParam(null);
        userRpcRequest.setHeaderParam(headerMap);
        String result = rabbitMQClient.callRpcService(RabbitMQProperties.USER_RPC_EXCHANGE,
                RabbitMQProperties.USER_RPC_QUEUE, RabbitMQProperties.USER_RPC_KEY, userRpcRequest.toJsonString());
        LOGGER.info("authenToken - result: " + result);
        if (result != null) {
            ObjectMapper mapper = new ObjectMapper();
            //DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //mapper.setDateFormat(df);
            ResponseMessage response = null;
            try {
                response = mapper.readValue(result, ResponseMessage.class);
            } catch (JsonProcessingException ex) {
                LOGGER.info("Lỗi parse json khi gọi user service verify: " + ex.toString());
                return null;
            }

            if (response != null && response.getStatus() == HttpStatus.OK.value()) {
                try {
                    //Process
                    MessageContent content = response.getData();
                    Object data = content.getData();
                    if (data != null) {
                        AuthorizationResponseDTO dto = null;
                        if (data.getClass() == LinkedHashMap.class) {
                            dto = new AuthorizationResponseDTO((Map<String, Object>) data);
                        } else if (data.getClass() == AuthorizationResponseDTO.class) {
                            dto = (AuthorizationResponseDTO) data;
                        }
                        if (dto != null && !StringUtil.isNullOrEmpty(dto.getUuid())) {
                            return dto;
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.info("Lỗi giải mã AuthorizationResponseDTO khi gọi user service verify: " + ex.toString());
                    return null;
                }
            } else {
                //Forbidden
                return null;
            }
        } else {
            //Forbidden
            return null;
        }
        return null;
    }

    public ResultCheckDto authorizeABAC(Map<String, Object> subject, Map<String, Object> attributes, String uuid, String requestPath, String method) {
        Map<String, Object> bodyParam = new HashMap<>();
        bodyParam.put("api", ResourcePath.VERSION + requestPath);
        bodyParam.put("method", method);
        bodyParam.put("subject", subject);
        bodyParam.put("attributes", attributes);
        bodyParam.put("uuid", uuid);
        RequestMessage abacRpcRequest = new RequestMessage();
        abacRpcRequest.setRequestMethod("POST");
        abacRpcRequest.setRequestPath(RabbitMQProperties.ABAC_RPC_AUTHOR_URL);
        abacRpcRequest.setBodyParam(bodyParam);
        abacRpcRequest.setUrlParam(null);
        abacRpcRequest.setHeaderParam(null);
        String result = rabbitMQClient.callRpcService(RabbitMQProperties.ABAC_RPC_EXCHANGE,
                RabbitMQProperties.ABAC_RPC_QUEUE, RabbitMQProperties.ABAC_RPC_KEY, abacRpcRequest.toJsonString());
        LOGGER.info("authorizeABAC - result: " + result);
        if (result != null) {
            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            ResponseMessage resultResponse = null;
            try {
                resultResponse = mapper.readValue(result, ResponseMessage.class);
                if (resultResponse != null && resultResponse.getStatus() == HttpStatus.OK.value() && resultResponse.getData() != null) {
                    //OK
                    JsonNode jsonNode = mapper.readTree(result);
                    ResultCheckDto resultCheckDto = mapper.treeToValue(jsonNode.get("data").get("data"), ResultCheckDto.class);
                    return resultCheckDto;
                }
                return null;
            } catch (Exception ex) {
                LOGGER.info("Lỗi parse json khi gọi kiểm tra quyền từ rbac service: " + ex.toString());
                return null;
            }
        } else {
            return null;
        }
    }

    public ABACResponseDTO authorizeABAC(Map<String, Object> bodyParam, String requestMethod,
            String userUuid, String apiPath) {
        Map<String, Object> bodyParamSend = new HashMap<>();
        if (bodyParam != null && !bodyParam.isEmpty()) {
            bodyParamSend.putAll(bodyParam);
        }
        bodyParamSend.put("uuid", userUuid);
        if (apiPath != null && !apiPath.startsWith(ResourcePath.VERSION)) {
            bodyParamSend.put("api", ResourcePath.VERSION + apiPath);
        } else {
            bodyParamSend.put("api", apiPath);
        }
        bodyParamSend.put("method", requestMethod);
        RequestMessage abacRpcRequest = new RequestMessage();
        abacRpcRequest.setRequestMethod("POST");
        abacRpcRequest.setRequestPath(RabbitMQProperties.ABAC_RPC_AUTHOR_URL);
        abacRpcRequest.setBodyParam(bodyParamSend);
        abacRpcRequest.setUrlParam(null);
        abacRpcRequest.setHeaderParam(null);

        LOGGER.info("REQUEST: {}", abacRpcRequest.toJsonString());
        String result = rabbitMQClient.callRpcService(RabbitMQProperties.ABAC_RPC_EXCHANGE,
                RabbitMQProperties.ABAC_RPC_QUEUE, RabbitMQProperties.ABAC_RPC_KEY,
                abacRpcRequest.toJsonString());
        LOGGER.info("RESULT: {}", result);
        if (result != null) {
            ObjectMapper mapper = new ObjectMapper();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            mapper.setDateFormat(df);
            ResponseMessage resultResponse = null;
            try {
                resultResponse = mapper.readValue(result, ResponseMessage.class);
                if (resultResponse != null && resultResponse.getStatus() == HttpStatus.OK.value() && resultResponse.getData() != null) {
                    JsonNode jsonNode = mapper.readTree(result);
                    ABACResponseDTO resultCheckDto = mapper.treeToValue(jsonNode.get("data").get("data"), ABACResponseDTO.class);
                    return resultCheckDto;
                }
                return null;
            } catch (JsonProcessingException | IllegalArgumentException ex) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Get AuthorizationResponseDTO from urlParam
     *
     * @param urlParam
     * @return AuthorizationResponseDTO
     */
    public AuthorizationResponseDTO GetUrlParam(String urlParam) {
        AuthorizationResponseDTO dto;
        Map<String, String> params = StringUtil.getUrlParamValues(urlParam);
        String dtoUuid = params.get("dto");
        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(df);
        try {
            dto = mapper.readValue(dtoUuid, AuthorizationResponseDTO.class);
            return dto;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }

    protected Claims getClaims(String idToken) throws InvalidKeySpecException, NoSuchAlgorithmException {
        PublicKey publicKey = createPublicKeyApple();
        try {
            return Jwts.parser().setSigningKey(publicKey).parseClaimsJws(idToken).getBody();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    private PublicKey createPublicKeyApple() throws InvalidKeySpecException, NoSuchAlgorithmException {
        try {
            BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode("iGaLqP6y-SJCCBq5Hv6pGDbG_SQ11MNjH7rWHcCFYz4hGwHC4lcSurTlV8u3avoVNM8jXevG1Iu1SY11qInqUvjJur--hghr1b56OPJu6H1iKulSxGjEIyDP6c5BdE1uwprYyr4IO9th8fOwCPygjLFrh44XEGbDIFeImwvBAGOhmMB2AD1n1KviyNsH0bEB7phQtiLk-ILjv1bORSRl8AK677-1T8isGfHKXGZ_ZGtStDe7Lu0Ihp8zoUt59kx2o9uWpROkzF56ypresiIl4WprClRCjz8x6cPZXU2qNWhu71TQvUFwvIvbkE1oYaJMb0jcOTmBRZA2QuYw-zHLwQ"));
            BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode("AQAB"));

            return KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent));
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }
// change AuthorizationResponseDTO to ResponseMessage
//    public ResponseMessage getAuthorFromToken(Map<String, String> headerParam) {
//        if (headerParam == null || (!headerParam.containsKey("authorization")
//                && !headerParam.containsKey("Authorization"))) {
//            return new ResponseMessage(new MessageContent(HttpStatus.UNAUTHORIZED.value(),"failed authentication",null));
//        }
//        String bearerToken = headerParam.get("authorization");
//        // Kiểm tra xem header Authorization có chứa thông tin jwt không
//        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
//            try {
//                String jwt = bearerToken.substring(7);
//                TokenParse tokenParse = tokenProvider.getUuidFromJWT(jwt);
//                if(tokenParse.getCodeStatus()==HttpStatus.OK.value()) {
//                    String uuid = tokenParse.getData();
//                    LOGGER.info("Lấy uuid");
//                    UserDetails userDetails = authService.loadUserByUuid(uuid);
//                    if (userDetails != null) {
//                        User user = ((CustomUserDetails) userDetails).getUser();
//                        if (user.getStatus() == User.STATUS_LOCK) {
//                            new ResponseMessage(new MessageContent(HttpStatus.UNAUTHORIZED.value(),HttpStatus.UNAUTHORIZED.toString(), null));
//                        } else {
//                            AuthorizationResponseDTO responseDTO = new AuthorizationResponseDTO((CustomUserDetails) userDetails, null, null);
//                            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(),HttpStatus.OK.toString(), responseDTO)) ;
//                        }
//                    }
//                } else {
//                    return new ResponseMessage(new MessageContent(tokenParse.getCodeStatus(),tokenParse.getMessage(),null));
//                }
//            } catch (Exception ex) {
//                LOGGER.error("failed on set user authentication", ex);
//                return new ResponseMessage(new MessageContent(HttpStatus.UNAUTHORIZED.value(),"failed on set user authentication",null));
//            }
//        }
//        return new ResponseMessage(new MessageContent(HttpStatus.UNAUTHORIZED.value(),"failed Bearer authentication",null));
//    }

    // AuthorFromRefreshToken
    public ResponseMessage getAuthorFromRefreshToken(Map<String, String> headerParam) {
        if (headerParam == null || (!headerParam.containsKey("authorization")
                && !headerParam.containsKey("Authorization"))) {
            return new ResponseMessage(new MessageContent(HttpStatus.UNAUTHORIZED.value(), "failed authentication", null));
        }
        String bearerToken = headerParam.get("authorization");
        // Kiểm tra xem header Authorization có chứa thông tin jwt không
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            try {
                String jwt = bearerToken.substring(7);
                TokenParse tokenParse = jwTutils.getContentInToken(jwt);
                return new ResponseMessage(new MessageContent(tokenParse.getCodeStatus(), tokenParse.getMessage(), tokenParse.getData()));

            } catch (Exception ex) {
                LOGGER.error("failed on set user authentication", ex);
                return new ResponseMessage(new MessageContent(HttpStatus.UNAUTHORIZED.value(), "failed on set user authentication", null));
            }
        }
        return new ResponseMessage(new MessageContent(HttpStatus.UNAUTHORIZED.value(), "failed Bearer authentication", null));
    }

    public AuthorizationResponseDTO getAuthorFromToken(Map<String, String> headerParam) {
        if (headerParam == null || (!headerParam.containsKey("authorization")
                && !headerParam.containsKey("Authorization"))) {
            return null;
        }
        String bearerToken = headerParam.get("authorization");
        // Kiểm tra xem header Authorization có chứa thông tin jwt không
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            try {
                String jwt = bearerToken.substring(7);
                String uuid = tokenProvider.getUuidFromJWT(jwt);
                UserDetails userDetails = authService.loadUserByUuid(uuid);
                if (userDetails != null) {
                    User user = ((CustomUserDetails) userDetails).getUser();
                    if (user.getStatus() == User.STATUS_LOCK) {
                        return null;
                    } else {
                        UsernamePasswordAuthenticationToken authentication
                                = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        AuthorizationResponseDTO responseDTO = new AuthorizationResponseDTO((CustomUserDetails) authentication.getPrincipal(), null, null);
                        return responseDTO;
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("failed on set user authentication", ex);
                return null;
            }
        }
        return null;
    }

    public void publishDeletedUnit(Unit unit) throws JsonProcessingException {
        DeleteDataPublishMessage message = DeleteDataPublishMessage.builder()
                .dataType("UNIT")
                .dataId(unit.getUuid())
                .data(new ArrayList<>(List.of(unit.getUuid())))
                .build();
        rabbitMQClient.callPublishService(RabbitMQProperties.DELETE_DATA_PUBLISH_EXCHANGE, RabbitMQProperties.DELETE_DATA_PUBLISH_KEY, message.toJsonString());
    }

    public void publishDeletedListUnit(List<Unit> listUnit) throws JsonProcessingException {
        List<String> listUnitIds = listUnit.stream().map(unit -> unit.getUuid()).collect(Collectors.toList());
        DeleteDataPublishMessage message = DeleteDataPublishMessage.builder()
                .dataType("UNIT")
                .dataId(String.join(",", listUnitIds))
                .data(listUnitIds)
                .build();
        rabbitMQClient.callPublishService(RabbitMQProperties.DELETE_DATA_PUBLISH_EXCHANGE,  RabbitMQProperties.DELETE_DATA_PUBLISH_KEY, message.toJsonString());
    }
}
